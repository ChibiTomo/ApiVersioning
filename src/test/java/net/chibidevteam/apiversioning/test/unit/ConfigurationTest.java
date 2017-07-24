package net.chibidevteam.apiversioning.test.unit;

import static net.chibidevteam.apiversioning.config.ApiVersioningConfiguration.COMPATIBLE_VERSION_PREFIXES;
import static net.chibidevteam.apiversioning.config.ApiVersioningConfiguration.EXACT_VERSION_PREFIXES;
import static net.chibidevteam.apiversioning.config.ApiVersioningConfiguration.EXCLUDE_VERSION_PREFIXES;
import static net.chibidevteam.apiversioning.config.ApiVersioningConfiguration.INFERIOR_VERSION_PREFIXES;
import static net.chibidevteam.apiversioning.config.ApiVersioningConfiguration.REGEX_CHAR_LIST_CLOSE;
import static net.chibidevteam.apiversioning.config.ApiVersioningConfiguration.REGEX_CHAR_LIST_OPEN;
import static net.chibidevteam.apiversioning.config.ApiVersioningConfiguration.SUPERIOR_VERSION_PREFIXES;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.chibidevteam.apiversioning.config.ApiVersioningConfiguration;
import net.chibidevteam.apiversioning.exceptions.NoSupportedVersionException;
import net.chibidevteam.apiversioning.util.Utils;

public class ConfigurationTest {

    @Before
    public void resetDefault() throws NoSupportedVersionException {
        Utils.resetDefaults();
    }

    @Test(expected = NoSupportedVersionException.class)
    public void AllNull() throws NoSupportedVersionException {
        Utils.versionPathPrefix = null;
        Utils.pathVarname = null;
        Utils.versionRegex = null;
        Utils.noCaptureVersionRegex = null;
        Utils.basePath = null;
        Utils.apiPath = null;
        Utils.supportedVersions = null;
        assertAllValid();
    }

    @Test
    public void AllNullButSupportedVersion() throws NoSupportedVersionException {
        Utils.versionPathPrefix = null;
        Utils.pathVarname = null;
        Utils.versionRegex = null;
        Utils.noCaptureVersionRegex = null;
        Utils.basePath = null;
        Utils.apiPath = null;
        Utils.supportedVersions = new String[] { "0", "1.7", "1.8", "2.5", "3", "4.0" };
        assertAllValid();
    }

    @Test(expected = NoSupportedVersionException.class)
    public void noSupportedVersion() throws NoSupportedVersionException {
        Utils.supportedVersions = new String[] {};
        assertAllValid();
    }

    @Test
    public void configDefault() throws NoSupportedVersionException {
        assertAllValid();
    }

    @Test
    public void configCustom01() throws NoSupportedVersionException {
        Utils.versionPathPrefix = "somePrefix36";
        Utils.pathVarname = "myApiVersionVarname";
        Utils.versionRegex = "(\\d+)(?:\\.(\\d+))?(.*)";
        Utils.noCaptureVersionRegex = "\\d+(?:\\.\\d+)?.*";
        Utils.basePath = "/my/api/base/path";
        Utils.apiPath = Utils.basePath + "/{" + Utils.pathVarname + "}/{" + Utils.pathVarname + "}/and/custom/path";
        assertAllValid();
    }

    private void assertAllValid() throws NoSupportedVersionException {
        Utils.applyConfig();

        Assert.assertEquals(Utils.versionPathPrefix, ApiVersioningConfiguration.getVersionPathPrefix());
        Assert.assertEquals(Utils.pathVarname, ApiVersioningConfiguration.getPathVarname());
        Assert.assertArrayEquals(Utils.supportedVersions, ApiVersioningConfiguration.getSupportedVersions());
        Assert.assertEquals(Utils.basePath, ApiVersioningConfiguration.getBasePath());
        Assert.assertEquals(Utils.apiPath, ApiVersioningConfiguration.getApiPath());
        Assert.assertEquals(Utils.versionPathPrefix, ApiVersioningConfiguration.getVersionPathPrefix());

        Assert.assertEquals("^.?" + Utils.versionRegex + "$", ApiVersioningConfiguration.getConfVersionRegex());
        Assert.assertEquals("^" + regChars(SUPERIOR_VERSION_PREFIXES) + Utils.versionRegex + "$",
                ApiVersioningConfiguration.getSuperiorVersionRegex());
        Assert.assertEquals("^" + regChars(INFERIOR_VERSION_PREFIXES) + Utils.versionRegex + "$",
                ApiVersioningConfiguration.getInferiorVersionRegex());
        Assert.assertEquals("^" + regChars(COMPATIBLE_VERSION_PREFIXES) + Utils.versionRegex + "$",
                ApiVersioningConfiguration.getCompatibleVersionRegex());
        Assert.assertEquals("^" + regChars(EXACT_VERSION_PREFIXES) + "?" + Utils.versionRegex + "$",
                ApiVersioningConfiguration.getExactVersionRegex());
        Assert.assertEquals("^" + regChars(EXCLUDE_VERSION_PREFIXES) + Utils.versionRegex + "$",
                ApiVersioningConfiguration.getExcludeVersionRegex());
        Assert.assertEquals("^" + Utils.versionPathPrefix + Utils.noCaptureVersionRegex + "$",
                ApiVersioningConfiguration.getPathVersionRegex());

        Assert.assertEquals("{" + Utils.pathVarname + "}", ApiVersioningConfiguration.getVersionPathVariable());
        Assert.assertEquals(
                "{" + Utils.pathVarname + ":^" + Utils.versionPathPrefix + Utils.noCaptureVersionRegex + "$}",
                ApiVersioningConfiguration.getVersionPathVariableWithRegex());
    }

    private String regChars(String chars) {
        return REGEX_CHAR_LIST_OPEN + chars + REGEX_CHAR_LIST_CLOSE;
    }

}
