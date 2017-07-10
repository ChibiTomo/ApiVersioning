package net.chibidevteam.apiversioning.annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import net.chibidevteam.apiversioning.helper.VersionHelper;

public class ApiVersionValidator implements ConstraintValidator<ValidApiVersion, String> {

    @Autowired
    VersionHelper    helper;

    private String[] allowedVersions;

    @Override
    public void initialize(ValidApiVersion annotation) {
        allowedVersions = annotation.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (!VersionHelper.isVersion(value)) {
            return false;
        }
        return VersionHelper.match(value, allowedVersions, true);
    }

}
