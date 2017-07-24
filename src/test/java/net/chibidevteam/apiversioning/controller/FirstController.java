package net.chibidevteam.apiversioning.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.chibidevteam.apiversioning.annotation.ApiRequestMapping;
import net.chibidevteam.apiversioning.annotation.ApiVersion;

@RestController
@ApiVersion(">1.5")
@ApiRequestMapping(FirstController.BASE_PATH)
public class FirstController {

    public static final String BASE_PATH            = "/first";
    public static final String RESPONSE_EXAMPLE     = "Hello World";
    public static final String RESPONSE_NEW_EXAMPLE = "Hello in version higher than 2.5!";
    public static final String RESPONSE_EXAMPLE_V3  = "Hello in version 3!";
    public static final String RESPONSE_TEST        = "Hello Test";

    @ApiVersion("<2.5")
    @ApiRequestMapping("/")
    public String example() {
        return "Hello World";
    }

    @ApiVersion({ ">2.5", "!3" })
    @ApiRequestMapping
    public String newExample() {
        return "Hello in version higher than 2.5!";
    }

    @ApiVersion("3")
    @RequestMapping
    public String exampleV3() {
        return "Hello in version 3!";
    }

    @RequestMapping("/test")
    public String getTest() {
        return "Hello Test";
    }
}
