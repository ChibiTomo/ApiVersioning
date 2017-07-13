package net.chibidevteam.apiversioning.util.helper;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.ArrayUtils;

import net.chibidevteam.apiversioning.config.ApiVersioningConfiguration;
import net.chibidevteam.apiversioning.util.ApiVersionCondition;

public class ApiPathHelper {

    private ApiPathHelper() {
    }

    private static String getApiPath(boolean useVersionVar) {
        String apiPath = ApiVersioningConfiguration.getApiPath();

        boolean hasLeadingSlash = apiPath.startsWith("/");
        boolean hasTrailingSlash = apiPath.endsWith("/");

        if (!useVersionVar) {
            apiPath = apiPath.replaceAll(ApiVersioningConfiguration.getVersionPathVariableEscaped(), "")
                    .replaceAll("/+", "/");
        }
        if (hasLeadingSlash) {
            apiPath = prependSlash(apiPath);
        } else {
            apiPath = removeLeadingSlash(apiPath);
        }
        if (hasTrailingSlash) {
            apiPath = appendSlash(apiPath);
        } else {
            apiPath = removeTrailingSlash(apiPath);
        }
        return apiPath;
    }

    public static String[] getPaths(ApiVersionCondition apiVersionCondition, boolean useApiPath,
            boolean useVersionVar) {
        if (!useApiPath) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }

        String[] result = { getApiPath(useVersionVar) };
        if (apiVersionCondition == null) {
            return result;
        }

        Set<String> set = new TreeSet<>();
        Set<String> handledVersions = apiVersionCondition.getVersions();

        String needle = ApiVersioningConfiguration.getVersionPathVariableEscaped();
        for (String path : result) {
            for (String version : handledVersions) {
                StringBuilder sb = new StringBuilder(ApiVersioningConfiguration.getVersionPathPrefix());
                sb.append(version);
                set.add(path.replaceAll(needle, sb.toString()));
            }
            if (apiVersionCondition.doSupportLast()) {
                set.add(getLastVersionPath(path));
            }
        }
        return set.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    private static String getLastVersionPath(String path) {
        String needle = ApiVersioningConfiguration.getVersionPathVariableEscaped();
        String v = path.replaceAll(needle, "");
        return v.replaceAll("/+", "/");
    }

    private static String removeTrailingSlash(String path) {
        String result = path.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private static String removeLeadingSlash(String path) {
        String result = path.trim();
        while (result.startsWith("/")) {
            result = result.substring(1);
        }
        return result;
    }

    private static String prependSlash(String path) {
        String result = path.trim();
        if (!result.startsWith("/")) {
            result = "/" + result;
        }
        return result;
    }

    private static String appendSlash(String path) {
        String result = path.trim();
        if (!result.endsWith("/")) {
            result += "/";
        }
        return result;
    }
}
