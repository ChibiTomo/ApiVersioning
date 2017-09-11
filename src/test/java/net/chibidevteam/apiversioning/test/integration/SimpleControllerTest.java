package net.chibidevteam.apiversioning.test.integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import net.chibidevteam.apiversioning.config.TestConfig;
import net.chibidevteam.apiversioning.controller.FirstController;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
@WebAppConfiguration
public class SimpleControllerTest extends AbstractControllerTest {
    // Version supported: 0,1.7,1.8,2.5,3,4.0

    @Test
    public void fallback404() throws Exception {
        expectApi(FirstController.BASE_PATH, "1.7", HttpStatus.NOT_FOUND.value(), "");
        expectApi(FirstController.BASE_PATH, "0", HttpStatus.NOT_FOUND.value(), "");
    }

    // Exception to handle is ConstraintViolationException
    @Test
    public void fallback500() throws Exception {
        expectApi(FirstController.BASE_PATH, "12", true);
    }

    @Test
    public void ok() throws Exception {
        expectApi(FirstController.BASE_PATH + "/", "1.7", HttpStatus.OK.value(), FirstController.RESPONSE_EXAMPLE);
        expectApi(FirstController.BASE_PATH + "/", "1.8", HttpStatus.OK.value(), FirstController.RESPONSE_EXAMPLE);

        expectApi(FirstController.BASE_PATH, "2.5", HttpStatus.OK.value(), FirstController.RESPONSE_NEW_EXAMPLE);
        expectApi(FirstController.BASE_PATH, "4", HttpStatus.OK.value(), FirstController.RESPONSE_NEW_EXAMPLE);
        expectApi(FirstController.BASE_PATH, HttpStatus.OK.value(), FirstController.RESPONSE_NEW_EXAMPLE);

        expectApi(FirstController.BASE_PATH, "3", HttpStatus.OK.value(), FirstController.RESPONSE_EXAMPLE_V3);

        expectApi(FirstController.BASE_PATH + "/test", "1.7", HttpStatus.OK.value(), FirstController.RESPONSE_TEST);
        expectApi(FirstController.BASE_PATH + "/test", "1.8", HttpStatus.OK.value(), FirstController.RESPONSE_TEST);
        expectApi(FirstController.BASE_PATH + "/test", "2.5", HttpStatus.OK.value(), FirstController.RESPONSE_TEST);
        expectApi(FirstController.BASE_PATH + "/test", "3", HttpStatus.OK.value(), FirstController.RESPONSE_TEST);
        expectApi(FirstController.BASE_PATH + "/test", "4", HttpStatus.OK.value(), FirstController.RESPONSE_TEST);
        expectApi(FirstController.BASE_PATH + "/test", HttpStatus.OK.value(), FirstController.RESPONSE_TEST);
    }
}
