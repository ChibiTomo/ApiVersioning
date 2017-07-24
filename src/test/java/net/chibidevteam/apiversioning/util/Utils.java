package net.chibidevteam.apiversioning.util;

import net.chibidevteam.apiversioning.config.ApiVersioningConfiguration;
import net.chibidevteam.apiversioning.exceptions.NoSupportedVersionException;

public class Utils {

    public static String   versionPathPrefix;
    public static String   pathVarname;
    public static String   versionRegex;
    public static String   noCaptureVersionRegex;
    public static String   basePath;
    public static String   apiPath;

    public static String[] supportedVersions;

    public static void resetDefaults() throws NoSupportedVersionException {
        versionPathPrefix = "v";
        pathVarname = "apiVersion";
        versionRegex = "(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?(.*)";
        noCaptureVersionRegex = "(?:\\d+)(?:\\.(?:\\d+))?(?:\\.(?:\\d+))?(?:.*)";
        basePath = "/api";
        apiPath = basePath + "/{" + pathVarname + "}";
        supportedVersions = new String[] { "0", "1.7", "1.8", "2.5", "3", "4.0" };
    }

    public static void applyConfig() throws NoSupportedVersionException {
        ApiVersioningConfiguration cfg = new ApiVersioningConfiguration(versionPathPrefix, pathVarname,
                supportedVersions, versionRegex, noCaptureVersionRegex, basePath, apiPath);

        versionPathPrefix = ApiVersioningConfiguration.getVersionPathPrefix();
        pathVarname = ApiVersioningConfiguration.getPathVarname();
        versionRegex = cfg.getVersionRegex();
        noCaptureVersionRegex = cfg.getNoCaptureVersionRegex();
        basePath = ApiVersioningConfiguration.getBasePath();
        apiPath = ApiVersioningConfiguration.getApiPath();

        supportedVersions = ApiVersioningConfiguration.getSupportedVersions();
    }
}
