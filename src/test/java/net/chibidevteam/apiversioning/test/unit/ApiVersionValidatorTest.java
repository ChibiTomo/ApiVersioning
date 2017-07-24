package net.chibidevteam.apiversioning.test.unit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.chibidevteam.apiversioning.annotation.ApiVersionValidator;
import net.chibidevteam.apiversioning.exceptions.NoSupportedVersionException;
import net.chibidevteam.apiversioning.util.Utils;

public class ApiVersionValidatorTest {

    private ApiVersionValidator validator;

    @Before
    public void resetDefault() throws NoSupportedVersionException {
        Utils.resetDefaults();

        Utils.applyConfig();
        validator = new ApiVersionValidator();
        validator.initialize(null);
    }

    // "0", "1.7", "1.8", "2.5", "3", "4.0"
    @Test
    public void isValid() {

        Assert.assertFalse(validator.isValid("1", null));
        Assert.assertFalse(validator.isValid(getVersion("12"), null));
        Assert.assertFalse(validator.isValid("", null));
        Assert.assertFalse(validator.isValid(getVersion(""), null));

        Assert.assertTrue(validator.isValid(getVersion("1.7"), null));
        Assert.assertTrue(validator.isValid(getVersion("1.7"), null));
        Assert.assertTrue(validator.isValid(getVersion("1.7.0"), null));
        Assert.assertTrue(validator.isValid(getVersion("1.7-alpha"), null));

        Assert.assertTrue(validator.isValid(getVersion("3.0"), null));
        Assert.assertTrue(validator.isValid(getVersion("4"), null));
    }

    private String getVersion(String string) {
        return Utils.versionPathPrefix + string;
    }

}
