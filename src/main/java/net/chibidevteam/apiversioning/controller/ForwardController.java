package net.chibidevteam.apiversioning.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import net.chibidevteam.apiversioning.annotation.ValidApiVersion;

@RestController
@Validated
public class ForwardController {

    @Value("${api_versioning.path_varname}")
    private final String apiPathVarname = "";

    @RequestMapping("${api_versioning.api_path}/**")
    @ResponseBody
    public String forward(@ValidApiVersion @PathVariable(apiPathVarname) String apiVersion) {
        return "hello " + apiVersion;
    }

}
