package net.chibidevteam.apiversioning.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import net.chibidevteam.apiversioning.mapping.ApiVersionRequestMappingHandlerMapping;

@Configuration
public class WebMvcConfigSupport extends DelegatingWebMvcConfiguration {

    private static final Log LOGGER = LogFactory.getLog(WebMvcConfigSupport.class);

    @Override
    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        LOGGER.debug("Loading ApiVersion Request Mapper");
        return new ApiVersionRequestMappingHandlerMapping();
    }
}
