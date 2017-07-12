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
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo.BuilderConfiguration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import net.chibidevteam.apiversioning.annotation.ApiRequestMapping;
import net.chibidevteam.apiversioning.annotation.ApiVersion;
import net.chibidevteam.apiversioning.annotation.ApiVersionCondition;
import net.chibidevteam.apiversioning.annotation.RequestMappingCombiner;
import net.chibidevteam.apiversioning.config.ApiVersioningConfiguration;

public class ApiVersionRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo info = createMappingForMethod(method, handlerType, null);

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

        if (logger.isTraceEnabled()) {
            logger.trace(handlerType.getName() + "::" + method.getName() + " restricted by " + handlerType.getName()
                    + " accepts following versions: " + apiVersionCondition);
        }

        return getRequestMappingInfo(apiVersionCondition, info, method, handlerType);
    }

    private RequestMappingInfo createMappingForMethod(Method method, Class<?> handlerType,
            ApiVersionCondition apiVersionCondition) {
        RequestMappingInfo info = createRequestMappingInfo(method, apiVersionCondition);

        if (info != null) {
            if (logger.isTraceEnabled()) {
                logger.trace(
                        "Method " + handlerType.getName() + "::" + method.getName() + " can be mapped to: " + info);
            }
            RequestMappingInfo typeInfo = createRequestMappingInfo(handlerType, apiVersionCondition);
            if (typeInfo != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Type " + handlerType.getName() + " constrains mapping to: " + info);
                }
                info = combine(typeInfo, info);
                if (logger.isTraceEnabled()) {
                    logger.trace(
                            "Combined " + handlerType.getName() + "::" + method.getName() + " mapping to: " + info);
                }
            }
        } else {
            logger.trace("Method " + handlerType.getName() + "::" + method.getName() + " will not be mapped");
        }
        return info;
    }

    private RequestMappingInfo combine(RequestMappingInfo typeInfo, RequestMappingInfo methodInfo) {
        if (typeInfo == null && methodInfo == null) {
            return null;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("About to combine " + typeInfo + " and " + methodInfo);
        }
        return buildRequestMappingInfo(new RequestMappingCombiner(methodInfo, typeInfo), null, null);
    }

    private RequestMappingInfo getRequestMappingInfo(ApiVersionCondition apiVersionCondition, RequestMappingInfo info,
            Method method, Class<?> handlerType) {
        RequestMappingInfo result = info;
        if (apiVersionCondition == null) {
            return result;
        }
        // result = requestParamApiVersionInfo(apiVersionCondition).combine(info);
        // result = requestHeaderApiVersionInfo(apiVersionCondition).combine(info);

        // Do not combine for path, we recreate all.
        result = createMappingForMethod(method, handlerType, apiVersionCondition);
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

    private RequestMappingInfo requestParamApiVersionInfo(ApiVersionCondition apiVersionCondition) {
        return new RequestMappingInfo(null, null,
                new ParamsRequestCondition(
                        Stream.of(apiVersionCondition.getVersions()).map(val -> "v=" + val).toArray(String[]::new)),
                null, null, null, null);
    }

    private RequestMappingInfo requestHeaderApiVersionInfo(ApiVersionCondition apiVersionCondition) {
        return new RequestMappingInfo(null, null, null,
                new HeadersRequestCondition(
                        Stream.of(apiVersionCondition.getVersions()).map(val -> "v=" + val).toArray(String[]::new)),
                null, null, null);
    }

    private RequestMappingInfo createRequestMappingInfo(AnnotatedElement element,
            ApiVersionCondition apiVersionCondition) {
        RequestMappingCombiner requestMapper = getRequestMapper(element);
        RequestCondition<?> condition = element instanceof Class ? getCustomTypeCondition((Class<?>) element)
                : getCustomMethodCondition((Method) element);
        return requestMapper != null ? buildRequestMappingInfo(requestMapper, condition, apiVersionCondition) : null;
    }

    private RequestMappingCombiner getRequestMapper(AnnotatedElement element) {
        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping.class);
        ApiRequestMapping apiRequestMapping = AnnotatedElementUtils.findMergedAnnotation(element,
                ApiRequestMapping.class);

        if (logger.isTraceEnabled()) {
            logger.info("Element has RequestMapping: " + requestMapping);
            logger.info("Element has ApiRequestMapping: " + apiRequestMapping);
        }

        if (requestMapping == null && apiRequestMapping == null) {
            return null;
        }
        return new RequestMappingCombiner(apiRequestMapping, requestMapping);
    }

    private RequestMappingInfo buildRequestMappingInfo(RequestMappingCombiner requestMapper,
            RequestCondition<?> customCondition, ApiVersionCondition apiVersionCondition) {

        BuilderConfiguration builderconfig = new RequestMappingInfo.BuilderConfiguration();
        builderconfig.setUrlPathHelper(getUrlPathHelper());
        builderconfig.setPathMatcher(getPathMatcher());
        builderconfig.setSuffixPatternMatch(useSuffixPatternMatch());
        builderconfig.setTrailingSlashMatch(useTrailingSlashMatch());
        builderconfig.setRegisteredSuffixPatternMatch(useRegisteredSuffixPatternMatch());
        builderconfig.setContentNegotiationManager(getContentNegotiationManager());

        String[] paths = requestMapper.getPaths(apiVersionCondition);

        if (logger.isTraceEnabled()) {
            logger.info("About to create RequestMappingInfo for paths: " + StringUtils.join(paths, ", "));
        }
        return RequestMappingInfo.paths(resolveEmbeddedValuesInPatterns(paths)).methods(requestMapper.getMethods())
                .params(requestMapper.getParams()).headers(requestMapper.getHeaders())
                .consumes(requestMapper.getConsumes()).produces(requestMapper.getProduces())
                .mappingName(requestMapper.getName())
                .customCondition(customCondition != null ? customCondition : requestMapper.getCondition())
                .options(builderconfig).build();
    }
}
