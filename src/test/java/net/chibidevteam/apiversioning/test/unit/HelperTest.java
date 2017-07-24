package net.chibidevteam.apiversioning.test.unit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.chibidevteam.apiversioning.exceptions.NoSupportedVersionException;
import net.chibidevteam.apiversioning.util.ApiVersionCondition;
import net.chibidevteam.apiversioning.util.Utils;
import net.chibidevteam.apiversioning.util.helper.ApiPathHelper;

public class HelperTest {

    @Before
    public void resetDefault() throws NoSupportedVersionException {
        Utils.resetDefaults();

        Utils.applyConfig();
    }

    @Test
    public void getPaths01() throws NoSupportedVersionException {
        String[] expecteds = new String[] {};
        boolean useVersionVar = true;
        boolean useApiPath = true;

        String[] versions = new String[] {};
        String[] supportedVersions = new String[] {};

        Utils.applyConfig();
        String[] paths = getPaths(versions, supportedVersions, useApiPath, useVersionVar);

        Assert.assertArrayEquals(expecteds, paths);
    }

    private String[] getPaths(String[] versions, String[] supportedVersions, boolean useApiPath,
            boolean useVersionVar) {
        ApiVersionCondition apiVersionCondition = new ApiVersionCondition(versions, supportedVersions);
        return ApiPathHelper.getPaths(apiVersionCondition, useApiPath, useVersionVar);
    }

}
