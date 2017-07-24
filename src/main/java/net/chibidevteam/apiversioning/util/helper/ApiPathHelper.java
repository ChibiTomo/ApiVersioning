package net.chibidevteam.apiversioning.util.helper;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import net.chibidevteam.apiversioning.config.ApiVersioningConfiguration;
import net.chibidevteam.apiversioning.util.ApiVersionCondition;

public class ApiPathHelper {

    private ApiPathHelper() {
    }

    private static String getApiPath(boolean useVersionVar) {
        String apiPath = ApiVersioningConfiguration.getApiPath();

        boolean hadLeadingSlash = apiPath.startsWith("/");
        boolean hadTrailingSlash = apiPath.endsWith("/");

        if (!useVersionVar) {
            apiPath = removeVersionPathVariable(apiPath);
        }
        if (hadLeadingSlash) {
            apiPath = prependSlash(apiPath);
        } else {
            apiPath = removeLeadingSlash(apiPath);
        }
        if (hadTrailingSlash) {
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

        for (String path : result) {
            for (String version : handledVersions) {
                StringBuilder sb = new StringBuilder(ApiVersioningConfiguration.getVersionPathPrefix());
                sb.append(version);
                set.add(replaceVersionPathVariable(path, sb.toString()));
            }
            if (apiVersionCondition.doSupportLast()) {
                set.add(removeVersionPathVariable(path));
            }
        }
        return set.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    public static String translatePath(String path, String version, boolean useApiPath) {
        StringBuilder sb = new StringBuilder();
        if (useApiPath) {
            sb.append(getApiPath(true));
        }
        sb.append(prependSlash(path));

        if (StringUtils.isEmpty(version)) {
            return removeVersionPathVariable(sb.toString());
        }
        String v = version;
        if (!v.startsWith(ApiVersioningConfiguration.getVersionPathPrefix())) {
            v = ApiVersioningConfiguration.getVersionPathPrefix() + v;
        }
        return replaceVersionPathVariable(sb.toString(), v);
    }

    private static String removeVersionPathVariable(String path) {
        return replaceVersionPathVariable(path, "");
    }

    private static String replaceVersionPathVariable(String path, String rplc) {
        String needle = ApiVersioningConfiguration.getVersionPathVariable();
        String needle2 = ApiVersioningConfiguration.getVersionPathVariableWithRegex();
        String p = path.replaceAll(Pattern.quote(needle), Matcher.quoteReplacement(rplc))
                .replaceAll(Pattern.quote(needle2), Matcher.quoteReplacement(rplc));
        return removeDuplicatedSlash(p);
    }

    private static String removeDuplicatedSlash(String path) {
        return path.replaceAll("/+", "/");
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
