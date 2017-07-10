package net.chibidevteam.apiversioning.config;

import java.util.Comparator;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@PropertySource(value = "classpath:/apiversioning.properties", ignoreResourceNotFound = true)
@Component
public class ApiVersioningConfiguration {

    public static final Comparator<Integer> INTEGER_COMPARATOR          = Comparator.nullsFirst(Integer::compareTo);

    public static final int                 PRIME_NBR                   = 31;

    private static final String             REGEX_BEGIN_CHAR            = "^";
    private static final String             REGEX_END_CHAR              = "$";
    private static final String             REGEX_CHAR_LIST_OPEN        = "[";
    private static final String             REGEX_CHAR_LIST_CLOSE       = "]";
    /**
     * Pattern which validate the version format
     */
    private static final String             VERSION_REGEX               = "^(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?(.*)$";
    public static final String              SUPERIOR_VERSION_PREFIXES   = ">";
    public static final String              INFERIOR_VERSION_PREFIXES   = "<";
    public static final String              COMPATIBLE_VERSION_PREFIXES = "\\^";
    public static final String              EXACT_VERSION_PREFIXES      = "v=";
    public static final String              EXCLUDE_VERSION_PREFIXES    = "!";

    private static String                   versionPathPrefix;

    private static String                   pathVarname;

    private static String[]                 supportedVersions;

    private static String                   confVersionRegex;
    private static String                   pathVersionRegex;
    private static String                   superiorVersionRegex;
    private static String                   inferiorVersionRegex;
    private static String                   compatibleVersionRegex;
    private static String                   exactVersionRegex;
    private static String                   excludeVersionRegex;
    private static Pattern                  versionPattern;

    public ApiVersioningConfiguration(@Value("${api_versioning.api_version_path_prefix}") String versionPathPrefix,
            @Value("${api_versioning.path_varname}") String pathVarname,
            @Value("${api_versioning.supported_versions}") String[] supportedVersions,
            @Value("${api_versioning.version_regex:" + VERSION_REGEX + "}") String versionRegex) {
        ApiVersioningConfiguration.versionPathPrefix = versionPathPrefix;
        ApiVersioningConfiguration.pathVarname = pathVarname;
        ApiVersioningConfiguration.supportedVersions = supportedVersions;

        String vr = versionRegex;
        if (!vr.startsWith(REGEX_BEGIN_CHAR)) {
            vr = REGEX_BEGIN_CHAR + vr;
        }
        if (vr.endsWith(REGEX_END_CHAR)) {
            vr += REGEX_END_CHAR;
        }
        ApiVersioningConfiguration.confVersionRegex = vr.replace(REGEX_BEGIN_CHAR, REGEX_BEGIN_CHAR + ".?");
        ApiVersioningConfiguration.pathVersionRegex = vr.replace(REGEX_BEGIN_CHAR,
                REGEX_BEGIN_CHAR + versionPathPrefix);
        ApiVersioningConfiguration.superiorVersionRegex = vr.replace(REGEX_BEGIN_CHAR,
                REGEX_BEGIN_CHAR + REGEX_CHAR_LIST_OPEN + SUPERIOR_VERSION_PREFIXES + REGEX_CHAR_LIST_CLOSE);
        ApiVersioningConfiguration.inferiorVersionRegex = vr.replace(REGEX_BEGIN_CHAR,
                REGEX_BEGIN_CHAR + REGEX_CHAR_LIST_OPEN + INFERIOR_VERSION_PREFIXES + REGEX_CHAR_LIST_CLOSE);
        ApiVersioningConfiguration.compatibleVersionRegex = vr.replace(REGEX_BEGIN_CHAR,
                REGEX_BEGIN_CHAR + REGEX_CHAR_LIST_OPEN + COMPATIBLE_VERSION_PREFIXES + REGEX_CHAR_LIST_CLOSE);
        ApiVersioningConfiguration.exactVersionRegex = vr.replace(REGEX_BEGIN_CHAR,
                REGEX_BEGIN_CHAR + REGEX_CHAR_LIST_OPEN + EXACT_VERSION_PREFIXES + REGEX_CHAR_LIST_CLOSE + "?");
        ApiVersioningConfiguration.excludeVersionRegex = vr.replace(REGEX_BEGIN_CHAR,
                REGEX_BEGIN_CHAR + REGEX_CHAR_LIST_OPEN + EXCLUDE_VERSION_PREFIXES + REGEX_CHAR_LIST_CLOSE);

        ApiVersioningConfiguration.versionPattern = Pattern.compile(ApiVersioningConfiguration.getConfVersionRegex());
    }

    public static String getPathVarname() {
        return pathVarname;
    }

    public static String[] getSupportedVersions() {
        return supportedVersions;
    }

    public static String getVersionPathPrefix() {
        return versionPathPrefix;
    }

    public static String getConfVersionRegex() {
        return confVersionRegex;
    }

    public static String getPathVersionRegex() {
        return pathVersionRegex;
    }

    public static String getSuperiorVersionRegex() {
        return superiorVersionRegex;
    }

    public static String getInferiorVersionRegex() {
        return inferiorVersionRegex;
    }

    public static String getCompatibleVersionRegex() {
        return compatibleVersionRegex;
    }

    public static String getExactVersionRegex() {
        return exactVersionRegex;
    }

    public static String getExcludeVersionRegex() {
        return excludeVersionRegex;
    }

    public static Pattern getVersionPattern() {
        return versionPattern;
    }

}
