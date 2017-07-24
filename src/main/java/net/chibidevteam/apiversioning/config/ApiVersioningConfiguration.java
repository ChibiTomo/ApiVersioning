package net.chibidevteam.apiversioning.config;

import java.util.Comparator;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import net.chibidevteam.apiversioning.exceptions.NoSupportedVersionException;

@PropertySource(value = { "classpath:/default-apiversioning.properties",
        "classpath:/" + ApiVersioningConfiguration.PROPERTY_FILE }, ignoreResourceNotFound = true)
@Configuration
@ComponentScan({ "net.chibidevteam.apiversioning.config", "net.chibidevteam.apiversioning.controller" })
public class ApiVersioningConfiguration {

    public static final String              PROPERTY_FILE                    = "apiversioning.properties";
    public static final String              DEFAULT_VERSION_REGEX            = "(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?(.*)";
    public static final String              DEFAULT_NO_CAPTURE_VERSION_REGEX = "(?:\\d+)(?:\\.(?:\\d+))?(?:\\.(?:\\d+))?(?:.*)";

    public static final Comparator<Integer> INTEGER_COMPARATOR               = Comparator
            .nullsFirst(Integer::compareTo);

    public static final int                 PRIME_NBR                        = 31;

    public static final String              REGEX_BEGIN_CHAR                 = "^";
    public static final String              REGEX_END_CHAR                   = "$";
    public static final String              REGEX_CHAR_LIST_OPEN             = "[";
    public static final String              REGEX_CHAR_LIST_CLOSE            = "]";

    public static final String              SUPERIOR_VERSION_PREFIXES        = ">";
    public static final String              INFERIOR_VERSION_PREFIXES        = "<";
    public static final String              COMPATIBLE_VERSION_PREFIXES      = "\\^";
    public static final String              EXACT_VERSION_PREFIXES           = "v=";
    public static final String              EXCLUDE_VERSION_PREFIXES         = "!";

    private static String                   versionPathPrefix;

    private static String                   basePath;
    private static String                   pathVarname;
    private static String                   apiPath;

    private static String[]                 supportedVersions;

    private static String                   confVersionRegex;
    private static String                   pathVersionRegex;
    private static String                   superiorVersionRegex;
    private static String                   inferiorVersionRegex;
    private static String                   compatibleVersionRegex;
    private static String                   exactVersionRegex;
    private static String                   excludeVersionRegex;
    private static Pattern                  versionPattern;

    private static String                   versionPathVariable;
    private static String                   versionPathVariableWithRegex;

    private String                          versionRegex;
    private String                          noCaptureVersionRegex;

    public ApiVersioningConfiguration(@Value("${net.chibidevteam.apiversioning.path.prefix}") String versionPathPrefix,
            @Value("${net.chibidevteam.apiversioning.path.varname}") String pathVarname,
            @Value("${net.chibidevteam.apiversioning.versions.supported}") String[] supportedVersions,
            @Value("${net.chibidevteam.apiversioning.versions.regex}") String versionRegex,
            @Value("${net.chibidevteam.apiversioning.versions.noCaptureRegex}") String noCaptureRegex,
            @Value("${net.chibidevteam.apiversioning.path.base}") String basePath,
            @Value("${net.chibidevteam.apiversioning.path.api}") String apiPath) throws NoSupportedVersionException {

        ApiVersioningConfiguration.versionPathPrefix = versionPathPrefix;
        ApiVersioningConfiguration.pathVarname = pathVarname;
        ApiVersioningConfiguration.supportedVersions = supportedVersions;
        ApiVersioningConfiguration.basePath = basePath;
        ApiVersioningConfiguration.apiPath = apiPath;

        if (ArrayUtils.isEmpty(ApiVersioningConfiguration.supportedVersions)) {
            throw new NoSupportedVersionException();
        }

        String vr = StringUtils.isEmpty(versionRegex) ? DEFAULT_VERSION_REGEX : versionRegex;
        this.versionRegex = vr;
        if (!vr.startsWith(REGEX_BEGIN_CHAR)) {
            vr = REGEX_BEGIN_CHAR + vr;
        }
        if (!vr.endsWith(REGEX_END_CHAR)) {
            vr += REGEX_END_CHAR;
        }

        String noCaptureVr = StringUtils.isEmpty(noCaptureRegex) ? DEFAULT_NO_CAPTURE_VERSION_REGEX : noCaptureRegex;
        this.noCaptureVersionRegex = noCaptureVr;
        if (!noCaptureVr.startsWith(REGEX_BEGIN_CHAR)) {
            noCaptureVr = REGEX_BEGIN_CHAR + noCaptureVr;
        }
        if (!noCaptureVr.endsWith(REGEX_END_CHAR)) {
            noCaptureVr += REGEX_END_CHAR;
        }

        confVersionRegex = vr.replace(REGEX_BEGIN_CHAR, REGEX_BEGIN_CHAR + ".?");
        pathVersionRegex = noCaptureVr.replace(REGEX_BEGIN_CHAR, REGEX_BEGIN_CHAR + versionPathPrefix);
        superiorVersionRegex = vr.replace(REGEX_BEGIN_CHAR,
                REGEX_BEGIN_CHAR + REGEX_CHAR_LIST_OPEN + SUPERIOR_VERSION_PREFIXES + REGEX_CHAR_LIST_CLOSE);
        inferiorVersionRegex = vr.replace(REGEX_BEGIN_CHAR,
                REGEX_BEGIN_CHAR + REGEX_CHAR_LIST_OPEN + INFERIOR_VERSION_PREFIXES + REGEX_CHAR_LIST_CLOSE);
        compatibleVersionRegex = vr.replace(REGEX_BEGIN_CHAR,
                REGEX_BEGIN_CHAR + REGEX_CHAR_LIST_OPEN + COMPATIBLE_VERSION_PREFIXES + REGEX_CHAR_LIST_CLOSE);
        exactVersionRegex = vr.replace(REGEX_BEGIN_CHAR,
                REGEX_BEGIN_CHAR + REGEX_CHAR_LIST_OPEN + EXACT_VERSION_PREFIXES + REGEX_CHAR_LIST_CLOSE + "?");
        excludeVersionRegex = vr.replace(REGEX_BEGIN_CHAR,
                REGEX_BEGIN_CHAR + REGEX_CHAR_LIST_OPEN + EXCLUDE_VERSION_PREFIXES + REGEX_CHAR_LIST_CLOSE);

        versionPattern = Pattern.compile(ApiVersioningConfiguration.getConfVersionRegex());
        versionPathVariable = "{" + getPathVarname() + "}";
        versionPathVariableWithRegex = "{" + getPathVarname() + ":" + getPathVersionRegex() + "}";
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

    public static String getBasePath() {
        return basePath;
    }

    public static String getApiPath() {
        return apiPath;
    }

    public static String getVersionPathVariable() {
        return versionPathVariable;
    }

    public static String getVersionPathVariableWithRegex() {
        return versionPathVariableWithRegex;
    }

    public String getVersionRegex() {
        return versionRegex;
    }

    public String getNoCaptureVersionRegex() {
        return noCaptureVersionRegex;
    }

}
