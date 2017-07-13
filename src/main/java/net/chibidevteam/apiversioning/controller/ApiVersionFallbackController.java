package net.chibidevteam.apiversioning.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.chibidevteam.apiversioning.annotation.ValidApiVersion;

@RestController
@Validated
public class ApiVersionFallbackController {

    @Value("${net.chibidevteam.apiversioning.path.varname}")
    private final String apiPathVarname = "";

    @RequestMapping("${net.chibidevteam.apiversioning.path.api}/**")
    @ResponseStatus(code = HttpStatus.NOT_IMPLEMENTED, reason = "This endpoint is not implemented for the given version")
    public void pathFallback(@ValidApiVersion @PathVariable(apiPathVarname) String apiVersion) {
        // It only makes difference between not supported versions and non implemented endpoints.
        // It always leads to a HTTP error
    }

}
