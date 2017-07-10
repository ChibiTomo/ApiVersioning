package net.chibidevteam.apiversioning.annotation;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.TreeSet;
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

import net.chibidevteam.apiversioning.config.ApiVersioningConfiguration;

public class ApiVersionRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo info = super.getMappingForMethod(method, handlerType);

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

    private RequestMappingInfo getRequestMappingInfo(ApiVersionCondition apiVersionCondition, RequestMappingInfo info,
            Method method, Class<?> handlerType) {
        RequestMappingInfo result = info;
        if (apiVersionCondition == null) {
            return result;
        }
        // result = requestParamApiVersionInfo(apiVersionCondition).combine(info);
        // result = requestHeaderApiVersionInfo(apiVersionCondition).combine(info);

        // Do not combine for path, we recreate all.
        result = pathVariableApiVersionInfo(apiVersionCondition, method, handlerType);
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

    private RequestMappingInfo pathVariableApiVersionInfo(ApiVersionCondition apiVersionCondition, Method method,
            Class<?> handlerType) {
        RequestMappingInfo info = createRequestMappingInfo(method, apiVersionCondition);
        if (info != null) {
            RequestMappingInfo typeInfo = createRequestMappingInfo(handlerType, apiVersionCondition);
            if (typeInfo != null) {
                info = typeInfo.combine(info);
            }
        }
        return info;
    }

    private RequestMappingInfo buildApiVersionPatternRequestCondition(RequestMapping requestMapping,
            RequestCondition<?> customCondition, ApiVersionCondition apiVersionCondition) {

        BuilderConfiguration builderconfig = new RequestMappingInfo.BuilderConfiguration();
        builderconfig.setUrlPathHelper(getUrlPathHelper());
        builderconfig.setPathMatcher(getPathMatcher());
        builderconfig.setSuffixPatternMatch(useSuffixPatternMatch());
        builderconfig.setTrailingSlashMatch(useTrailingSlashMatch());
        builderconfig.setRegisteredSuffixPatternMatch(useRegisteredSuffixPatternMatch());
        builderconfig.setContentNegotiationManager(getContentNegotiationManager());

        String[] paths = replaceApiVersionPathVariable(requestMapping.path(), apiVersionCondition);
        return RequestMappingInfo.paths(resolveEmbeddedValuesInPatterns(paths)).methods(requestMapping.method())
                .params(requestMapping.params()).headers(requestMapping.headers()).consumes(requestMapping.consumes())
                .produces(requestMapping.produces()).mappingName(requestMapping.name()).customCondition(customCondition)
                .options(builderconfig).build();
    }

    private String[] replaceApiVersionPathVariable(String[] paths, ApiVersionCondition apiVersionCondition) {
        Set<String> result = new TreeSet<>();
        Set<String> handledVersions = apiVersionCondition.getVersions();

        String needle = "\\{" + ApiVersioningConfiguration.getPathVarname() + "\\}";
        for (String path : paths) {
            for (String version : handledVersions) {
                StringBuilder sb = new StringBuilder(ApiVersioningConfiguration.getVersionPathPrefix());
                sb.append(version);
                result.add(path.replaceAll(needle, sb.toString()));
            }
            if (apiVersionCondition.doSupportLast()) {
                String v = path.replaceAll(needle, "");
                result.add(v.replaceAll("//", "/"));
            }
        }
        return result.toArray(new String[] {});
    }

    private RequestMappingInfo createRequestMappingInfo(AnnotatedElement element,
            ApiVersionCondition apiVersionCondition) {
        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping.class);
        RequestCondition<?> condition = element instanceof Class ? getCustomTypeCondition((Class<?>) element)
                : getCustomMethodCondition((Method) element);
        return requestMapping != null
                ? buildApiVersionPatternRequestCondition(requestMapping, condition, apiVersionCondition) : null;
    }
}
