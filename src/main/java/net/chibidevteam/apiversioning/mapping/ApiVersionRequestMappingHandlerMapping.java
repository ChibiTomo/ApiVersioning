package net.chibidevteam.apiversioning.mapping;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.condition.HeadersRequestCondition;
import org.springframework.web.servlet.mvc.condition.ParamsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo.BuilderConfiguration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import net.chibidevteam.apiversioning.annotation.ApiRequestMapping;
import net.chibidevteam.apiversioning.annotation.ApiVersion;
import net.chibidevteam.apiversioning.config.ApiVersioningConfiguration;
import net.chibidevteam.apiversioning.util.ApiVersionCondition;
import net.chibidevteam.apiversioning.util.RequestMappingWrapper;
import net.chibidevteam.apiversioning.util.helper.ApiPathHelper;

public class ApiVersionRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {

        RequestMappingInfo info = createMappingForMethod(method, handlerType);
        if (info == null) {
            return null;
        }

        ApiVersionCondition apiVersionCondition = createApiVersionCondition(method, handlerType);

        boolean useApiPath = doUseApiPath(method, handlerType);
        return getRequestMappingInfo(apiVersionCondition, info, useApiPath);
    }

    private ApiVersionCondition createApiVersionCondition(Method method, Class<?> handlerType) {
        ApiVersionCondition methodApiVersionCondition = getApiVersionCondition(method);
        if (logger.isTraceEnabled()) {
            logger.trace(handlerType.getName() + "::" + method.getName() + " accepts following versions: "
                    + methodApiVersionCondition);
        }
        ApiVersionCondition handlerTypeApiVersionCondition = getApiVersionCondition(handlerType);
        if (logger.isTraceEnabled()) {
            logger.trace(handlerType.getName() + " accepts following versions: " + handlerTypeApiVersionCondition);
        }

        ApiVersionCondition apiVersionCondition;
        if (methodApiVersionCondition != null && handlerTypeApiVersionCondition != null) {
            apiVersionCondition = methodApiVersionCondition.restrictWith(handlerTypeApiVersionCondition);
        } else if (methodApiVersionCondition == null) {
            apiVersionCondition = handlerTypeApiVersionCondition;
        } else {
            // handlerTypeApiVersionCondition is always null here
            apiVersionCondition = methodApiVersionCondition;
        }
        return apiVersionCondition;
    }

    private boolean doUseApiPath(Method method, Class<?> handlerType) {
        ApiRequestMapping apiRequestMappingM = AnnotatedElementUtils.findMergedAnnotation(method,
                ApiRequestMapping.class);
        ApiRequestMapping apiRequestMappingT = AnnotatedElementUtils.findMergedAnnotation(handlerType,
                ApiRequestMapping.class);
        return apiRequestMappingM != null || apiRequestMappingT != null;
    }

    private RequestMappingInfo createMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo info = createRequestMappingInfoFromMapper(method);
        if (info != null) {
            RequestMappingInfo typeInfo = createRequestMappingInfoFromMapper(handlerType);
            if (typeInfo != null) {
                info = typeInfo.combine(info);
            }
        }
        return info;
    }

    private RequestMappingInfo createRequestMappingInfoFromMapper(AnnotatedElement element) {
        RequestCondition<?> condition = element instanceof Class ? getCustomTypeCondition((Class<?>) element)
                : getCustomMethodCondition((Method) element);

        ApiRequestMapping apiRequestMapping = AnnotatedElementUtils.findMergedAnnotation(element,
                ApiRequestMapping.class);
        if (apiRequestMapping != null) {
            return createRequestMappingInfo(new RequestMappingWrapper(apiRequestMapping), condition);
        }

        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping.class);
        if (requestMapping != null) {
            return createRequestMappingInfo(new RequestMappingWrapper(requestMapping), condition);
        }

        return null;
    }

    private RequestMappingInfo createRequestMappingInfo(RequestMappingWrapper requestMapper,
            RequestCondition<?> customCondition) {

        return RequestMappingInfo.paths(resolveEmbeddedValuesInPatterns(requestMapper.getPaths()))
                .methods(requestMapper.getMethods()).params(requestMapper.getParams())
                .headers(requestMapper.getHeaders()).consumes(requestMapper.getConsumes())
                .produces(requestMapper.getProduces()).mappingName(requestMapper.getName())
                .customCondition(customCondition).options(getBuilderConfig()).build();
    }

    private BuilderConfiguration getBuilderConfig() {
        BuilderConfiguration builderconfig = new RequestMappingInfo.BuilderConfiguration();
        builderconfig.setUrlPathHelper(getUrlPathHelper());
        builderconfig.setPathMatcher(getPathMatcher());
        builderconfig.setSuffixPatternMatch(useSuffixPatternMatch());
        builderconfig.setTrailingSlashMatch(useTrailingSlashMatch());
        builderconfig.setRegisteredSuffixPatternMatch(useRegisteredSuffixPatternMatch());
        builderconfig.setContentNegotiationManager(getContentNegotiationManager());
        return builderconfig;
    }

    private RequestMappingInfo getRequestMappingInfo(ApiVersionCondition apiVersionCondition, RequestMappingInfo info,
            boolean useApiPath) {
        RequestMappingInfo result = info;
        if (apiVersionCondition == null) {
            return result;
        }
        // result = requestParamApiVersionInfo(apiVersionCondition, useApiPath).combine(info);
        // result = requestHeaderApiVersionInfo(apiVersionCondition, useApiPath).combine(info);

        // Do not combine for path.
        result = requestPathApiVersionInfo(apiVersionCondition, useApiPath, true).combine(info);
        return result;
    }

    private ApiVersionCondition getApiVersionCondition(Method method) {
        ApiVersion methodAnnotation = AnnotationUtils.findAnnotation(method, ApiVersion.class);
        return getApiVersionCondition(methodAnnotation);
    }

    private ApiVersionCondition getApiVersionCondition(Class<?> handlerType) {
        ApiVersion typeAnnotation = AnnotationUtils.findAnnotation(handlerType, ApiVersion.class);
        return getApiVersionCondition(typeAnnotation);
    }

    private ApiVersionCondition getApiVersionCondition(ApiVersion annotation) {
        if (annotation == null) {
            return null;
        }
        String[] values = annotation.value();
        if (logger.isTraceEnabled()) {
            logger.trace("Creating condition for versions [" + StringUtils.join(values, ", ")
                    + "], with supportted versions "
                    + StringUtils.join(ApiVersioningConfiguration.getSupportedVersions(), ", "));
        }
        return new ApiVersionCondition(values, ApiVersioningConfiguration.getSupportedVersions());
    }

    private RequestMappingInfo requestParamApiVersionInfo(ApiVersionCondition apiVersionCondition, boolean useApiPath) {
        return new RequestMappingInfo(null, null,
                new ParamsRequestCondition(
                        Stream.of(apiVersionCondition.getVersions()).map(val -> "v=" + val).toArray(String[]::new)),
                null, null, null, null);
    }

    private RequestMappingInfo requestHeaderApiVersionInfo(ApiVersionCondition apiVersionCondition,
            boolean useApiPath) {
        return new RequestMappingInfo(null, null, null,
                new HeadersRequestCondition(
                        Stream.of(apiVersionCondition.getVersions()).map(val -> "v=" + val).toArray(String[]::new)),
                null, null, null);
    }

    private RequestMappingInfo requestPathApiVersionInfo(ApiVersionCondition apiVersionCondition, boolean useApiPath,
            boolean useVersionVar) {
        PatternsRequestCondition patternCondition = getApiPatternCondition(apiVersionCondition, useApiPath,
                useVersionVar);
        return new RequestMappingInfo(patternCondition, null, null, null, null, null, null);
    }

    private PatternsRequestCondition getApiPatternCondition(ApiVersionCondition apiVersionCondition, boolean useApiPath,
            boolean useVersionVar) {
        if (apiVersionCondition == null || !useApiPath) {
            return null;
        }
        return new PatternsRequestCondition(ApiPathHelper.getPaths(apiVersionCondition, useApiPath, useVersionVar));
    }
}
