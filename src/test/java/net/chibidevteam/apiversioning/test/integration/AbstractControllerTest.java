package net.chibidevteam.apiversioning.test.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import net.chibidevteam.apiversioning.util.Utils;
import net.chibidevteam.apiversioning.util.helper.ApiPathHelper;

public abstract class AbstractControllerTest {

    private final Log                          logger    = LogFactory.getLog(getClass());

    protected static final Map<String, Object> EMPTY_MAP = new HashMap<>();

    @Autowired
    private WebApplicationContext              context;

    protected MockMvc                          mockMvc;

    @Before
    public void setUp() throws Exception {
        Utils.resetDefaults();
        Utils.applyConfig();

        mockMvc = MockMvcBuilders //
                .webAppContextSetup(context) //
                .build();
    }

    protected void expectApi(String path, int status, String body) throws Exception {
        expectApi(path, "", status, body);
    }

    protected void expectApi(String path, String version, int status, String body) throws Exception {
        expect(apiUri(path, version), status, body);
    }

    protected void expect(String path, int status, String body) throws Exception {
        ResultActions resultActions = request(path);
        MvcResult result = resultActions.andReturn();

        logger.info("Expected status: " + status + ", Actual status: " + result.getResponse().getStatus());
        logger.info(
                "Expected body: '" + body + "', Actual status: '" + result.getResponse().getContentAsString() + "'");
        Assert.assertEquals(body, result.getResponse().getContentAsString());
        resultActions.andExpect(status().is(status));
    }

    private ResultActions request(String path) throws Exception {
        logger.info("Performing request on: " + path);
        return mockMvc.perform(get(path));
    }

    private String apiUri(String path, String version) {
        return buildUri(path, version, true);
    }

    private String buildUri(String path, String version, boolean useApiPath) {
        return ApiPathHelper.translatePath(path, version, useApiPath);
    }

}
