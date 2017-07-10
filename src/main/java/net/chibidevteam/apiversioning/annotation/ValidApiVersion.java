package net.chibidevteam.apiversioning.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = ApiVersionValidator.class)
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidApiVersion {

    // SupportedVersions value() default SupportedVersions.ALL;
    String[] value() default {};

    String message() default "{net.chibidevteam.apiversioning.ValidApiVersion.notsupported}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
