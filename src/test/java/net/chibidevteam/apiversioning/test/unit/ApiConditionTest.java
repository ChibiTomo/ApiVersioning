package net.chibidevteam.apiversioning.test.unit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.chibidevteam.apiversioning.config.ApiVersioningConfiguration;
import net.chibidevteam.apiversioning.exceptions.NoSupportedVersionException;
import net.chibidevteam.apiversioning.util.ApiVersionCondition;

public class ApiConditionTest {

    String[]         versions;
    private String[] supportedVersions;

    @Before
    public void resetDefault() throws NoSupportedVersionException {
        String versionPathPrefix = "v";
        String pathVarname = "apiVersion";
        String versionRegex = "(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?(.*)";
        String noCaptureVersionRegex = "(?:\\d+)(?:\\.(?:\\d+))?(?:\\.(?:\\d+))?(?:.*)";
        String basePath = "/api";
        String apiPath = basePath + "/{" + pathVarname + "}";
        supportedVersions = new String[] { "0", "1.7", "1.8", "2.5", "3", "4.0" };
        new ApiVersioningConfiguration(versionPathPrefix, pathVarname, supportedVersions, versionRegex,
                noCaptureVersionRegex, basePath, apiPath);
    }

    @Test
    public void noSupportedVersions() {
        String[] expecteds = new String[] {};

        versions = new String[] {};
        supportedVersions = new String[] {};
        ApiVersionCondition apiVersionCondition = new ApiVersionCondition(versions, supportedVersions);

        Assert.assertFalse(apiVersionCondition.doSupportLast());
        Assert.assertArrayEquals(supportedVersions, apiVersionCondition.getSupportedVersions().toArray());
        Assert.assertArrayEquals(expecteds, apiVersionCondition.getVersions().toArray());
    }

    @Test
    // Supported versions are "0", "1.7", "1.8", "2.5", "3", "4.0"
    public void supportAllVersions() {
        String[] expecteds = supportedVersions;

        versions = new String[] {};
        ApiVersionCondition apiVersionCondition = new ApiVersionCondition(versions, supportedVersions);

        Assert.assertTrue(apiVersionCondition.doSupportLast());
        Assert.assertArrayEquals(supportedVersions, apiVersionCondition.getSupportedVersions().toArray());
        Assert.assertArrayEquals(expecteds, apiVersionCondition.getVersions().toArray());
    }

    @Test
    // Supported versions are "0", "1.7", "1.8", "2.5", "3", "4.0"
    public void getVersions01() {
        String[] expecteds = new String[] {};

        versions = new String[] { "^3", "1" };
        ApiVersionCondition apiVersionCondition = new ApiVersionCondition(versions, supportedVersions);

        Assert.assertFalse(apiVersionCondition.doSupportLast());
        Assert.assertArrayEquals(supportedVersions, apiVersionCondition.getSupportedVersions().toArray());
        Assert.assertArrayEquals(expecteds, apiVersionCondition.getVersions().toArray());
    }

    @Test
    // Supported versions are "0", "1.7", "1.8", "2.5", "3", "4.0"
    public void getVersions02() {
        String[] expecteds = new String[] { "2.5" };

        versions = new String[] { ">1.5", "^2" };
        ApiVersionCondition apiVersionCondition = new ApiVersionCondition(versions, supportedVersions);

        Assert.assertFalse(apiVersionCondition.doSupportLast());
        Assert.assertArrayEquals(supportedVersions, apiVersionCondition.getSupportedVersions().toArray());
        Assert.assertArrayEquals(expecteds, apiVersionCondition.getVersions().toArray());
    }

    @Test
    // Supported versions are "0", "1.7", "1.8", "2.5", "3", "4.0"
    public void restrict01() {
        String[] expecteds = new String[] { "1.7", "1.8" };

        ApiVersionCondition cd1 = new ApiVersionCondition(new String[] { ">1.5" }, supportedVersions);
        ApiVersionCondition cd2 = new ApiVersionCondition(new String[] { "<2.5" }, supportedVersions);

        ApiVersionCondition apiVersionCondition = cd2.restrictWith(cd1);

        Assert.assertFalse(apiVersionCondition.doSupportLast());
        Assert.assertArrayEquals(supportedVersions, apiVersionCondition.getSupportedVersions().toArray());
        Assert.assertArrayEquals(expecteds, apiVersionCondition.getVersions().toArray());
    }

    @Test
    // Supported versions are "0", "1.7", "1.8", "2.5", "3", "4.0"
    public void restrict02() {
        String[] expecteds = new String[] { "2.5", "3" };

        ApiVersionCondition cd1 = new ApiVersionCondition(new String[] { "^2", "^3" }, supportedVersions);
        ApiVersionCondition cd2 = new ApiVersionCondition(new String[] { ">2.5" }, supportedVersions);

        ApiVersionCondition apiVersionCondition = cd2.restrictWith(cd1);

        Assert.assertFalse(apiVersionCondition.doSupportLast());
        Assert.assertArrayEquals(supportedVersions, apiVersionCondition.getSupportedVersions().toArray());
        Assert.assertArrayEquals(expecteds, apiVersionCondition.getVersions().toArray());
    }

}
