package net.chibidevteam.apiversioning.annotation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestConditionHolder;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMethodMappingNamingStrategy;

import net.chibidevteam.apiversioning.config.ApiVersioningConfiguration;

/**
 * @author Yannis THOMIAS
 */
public class RequestMappingCombiner {

    /** Logger that is available to subclasses */
    protected final Log            logger = LogFactory.getLog(getClass());

    private String[]               paths;
    private RequestMethod[]        methods;
    private String[]               params;
    private String[]               headers;
    private String[]               consumes;
    private String[]               produces;
    private String                 name;

    private RequestConditionHolder condition;

    private boolean                useApiPath;

    public RequestMappingCombiner(ApiRequestMapping apiRequestMapping, RequestMapping requestMapping) {
        if (logger.isTraceEnabled()) {
            logger.trace("New RequesMappingCombiner from: " + apiRequestMapping + " and " + requestMapping);
        }
        add(apiRequestMapping);
        add(requestMapping);
    }

    public RequestMappingCombiner(RequestMapping... requestMappings) {
        if (logger.isTraceEnabled()) {
            logger.trace("New RequesMappingCombiner from RequestMapping: " + StringUtils.join(requestMappings, ", "));
        }
        for (RequestMapping requestMapping : requestMappings) {
            add(requestMapping);
        }
    }

    public RequestMappingCombiner(ApiRequestMapping... apiRequestMappings) {
        if (logger.isTraceEnabled()) {
            logger.trace(
                    "New RequesMappingCombiner from ApiRequestMapping: " + StringUtils.join(apiRequestMappings, ", "));
        }
        for (ApiRequestMapping apiRequestMapping : apiRequestMappings) {
            add(apiRequestMapping);
        }
    }

    public RequestMappingCombiner(RequestMappingInfo... requestMappingInfos) {
        if (logger.isTraceEnabled()) {
            logger.trace("New RequesMappingCombiner from RequestMappingInfo: "
                    + StringUtils.join(requestMappingInfos, ", "));
        }
        for (RequestMappingInfo requestMappingInfo : requestMappingInfos) {
            add(requestMappingInfo);
        }
    }

    private void add(ApiRequestMapping apiRequestMapping) {
        if (apiRequestMapping != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("About to add ApiRequestMapping: " + apiRequestMapping);
                logger.trace("Before adding ApiRequestMapping: " + this);
            }
            useApiPath = true;
            buildPath(apiRequestMapping.path());
            buildMethod(apiRequestMapping.method());
            params = mergeStrArrays(params, apiRequestMapping.params());
            headers = mergeStrArrays(headers, apiRequestMapping.headers());
            consumes = mergeStrArrays(consumes, apiRequestMapping.consumes());
            produces = mergeStrArrays(produces, apiRequestMapping.produces());
            buildName(apiRequestMapping.name());
            if (logger.isTraceEnabled()) {
                logger.trace("After adding ApiRequestMapping: " + this);
            }
        }
    }

    private void add(RequestMapping requestMapping) {
        if (requestMapping != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("About to add RequestMapping: " + requestMapping);
                logger.trace("Before adding RequestMapping: " + this);
            }
            buildPath(requestMapping.path());
            buildMethod(requestMapping.method());
            params = mergeStrArrays(params, requestMapping.params());
            headers = mergeStrArrays(headers, requestMapping.headers());
            consumes = mergeStrArrays(consumes, requestMapping.consumes());
            produces = mergeStrArrays(produces, requestMapping.produces());
            buildName(requestMapping.name());
            if (logger.isTraceEnabled()) {
                logger.trace("After adding RequestMapping: " + this);
            }
        }
    }

    private void add(RequestMappingInfo requestMappingInfo) {
        if (requestMappingInfo == null) {
            return;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("About to add RequestMappingInfo: " + requestMappingInfo);
            logger.trace("Before adding RequestMappingInfo: " + this);
        }
        if (requestMappingInfo.getPatternsCondition() != null) {
            buildPath(requestMappingInfo.getPatternsCondition().getPatterns().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        }
        if (requestMappingInfo.getMethodsCondition() != null) {
            buildMethod(requestMappingInfo.getMethodsCondition().getMethods().toArray(new RequestMethod[] {}));
        }
        if (requestMappingInfo.getParamsCondition() != null) {
            String[] newParams = toStringArray(requestMappingInfo.getParamsCondition().getExpressions());
            params = mergeStrArrays(params, newParams);
        }
        if (requestMappingInfo.getHeadersCondition() != null) {
            String[] newHeaders = toStringArray(requestMappingInfo.getHeadersCondition().getExpressions());
            headers = mergeStrArrays(headers, newHeaders);
        }
        if (requestMappingInfo.getConsumesCondition() != null) {
            String[] newConsumes = toStringArray(requestMappingInfo.getConsumesCondition().getExpressions());
            consumes = mergeStrArrays(consumes, newConsumes);
        }
        if (requestMappingInfo.getProducesCondition() != null) {
            String[] newProduces = toStringArray(requestMappingInfo.getProducesCondition().getExpressions());
            produces = mergeStrArrays(produces, newProduces);
        }
        buildName(requestMappingInfo.getName());
        buildCondition(requestMappingInfo.getCustomCondition());
        if (logger.isTraceEnabled()) {
            logger.trace("After adding RequestMappingInfo: " + this);
        }
    }

    private String[] toStringArray(Set<? extends Object> expressions) {
        Set<String> array = new HashSet<>();
        for (Object exp : expressions) {
            array.add(exp.toString());
        }
        return array.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    public String[] getPaths(ApiVersionCondition apiVersionCondition) {

        if (logger.isTraceEnabled()) {
            logger.trace("RequestMappingCombiner paths: " + StringUtils.join(paths, ", "));
        }
        if (apiVersionCondition == null) {
            return paths;
        }

        Set<String> result = new TreeSet<>();
        Set<String> handledVersions = apiVersionCondition.getVersions();

        String needle = ApiVersioningConfiguration.getVersionPathVariableEscaped();
        for (String path : paths) {
            for (String version : handledVersions) {
                StringBuilder sb = new StringBuilder(ApiVersioningConfiguration.getVersionPathPrefix());
                sb.append(version);
                result.add(path.replaceAll(needle, sb.toString()));
            }
            if (apiVersionCondition.doSupportLast()) {
                result.add(getLastVersionPath(path));
            }
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Conditionaly formed paths are: " + StringUtils.join(result, ", "));
        }
        return result.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    private String getLastVersionPath(String path) {
        String needle = ApiVersioningConfiguration.getVersionPathVariableEscaped();
        String v = path.replaceAll(needle, "");
        return v.replaceAll("//", "/");
    }

    private void buildPath(String[] array) {
        String[] newPaths = array;

        if (logger.isTraceEnabled()) {
            logger.trace("Current paths are: " + StringUtils.join(paths, ", "));
            logger.trace("About to add paths: " + StringUtils.join(newPaths, ", "));
        }

        if (ArrayUtils.isEmpty(paths)) {
            paths = newPaths;
        } else {
            Set<String> newPath = new HashSet<>();
            for (String p1 : paths) {
                for (String p2 : newPaths) {
                    newPath.add(mergePath(p1, p2));
                }
            }
            paths = newPath.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
        }

        if (logger.isTraceEnabled()) {
            logger.trace("New paths are: " + StringUtils.join(paths, ", "));
        }

        paths = addApiPath(paths);

        if (logger.isTraceEnabled()) {
            logger.trace("And after adding ApiPath: " + StringUtils.join(paths, ", "));
        }
    }

    private String mergePath(String p1, String p2) {
        if (p1.contains(p2)) {
            return p1;
        }
        if (p2.contains(p1) || p1.equals(getLastVersionPath(p1))) {
            return p2;
        }
        if (p2.equals(getLastVersionPath(p2))) {
            return p1;
        }
        if (p1.equals(getLastVersionPath(p1))) {
            return p2;
        }
        return removeTrailingSlash(p1) + prependSlash(p2);
    }

    private void buildMethod(RequestMethod[] newMethods) {
        if (ArrayUtils.isEmpty(methods)) {
            methods = newMethods;
        }
        RequestMethod[] array = ArrayUtils.addAll(methods, newMethods);
        Set<RequestMethod> newSet = new HashSet<>();
        newSet.addAll(Arrays.asList(array));
        methods = newSet.toArray(new RequestMethod[] {});

        if (logger.isTraceEnabled()) {
            logger.trace("New methods are: " + StringUtils.join(methods, ", "));
        }
    }

    private void buildName(String newName) {
        if (name != null && newName != null) {
            String separator = RequestMappingInfoHandlerMethodMappingNamingStrategy.SEPARATOR;
            name = name + separator + newName;
        } else if (name == null) {
            name = newName != null ? newName : null;
        }
    }

    private void buildCondition(RequestCondition<?> customCondition) {
        if (customCondition == null) {
            return;
        }
        RequestConditionHolder newCondition = new RequestConditionHolder(customCondition);
        if (condition == null) {
            condition = newCondition;
        } else {
            condition = newCondition.combine(condition);
        }
    }

    private String removeTrailingSlash(String path) {
        String result = path.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private String prependSlash(String path) {
        String result = path.trim();
        if (!result.startsWith("/")) {
            result = "/" + result;
        }
        return result;
    }

    private String[] addApiPath(String[] path) {
        if (!useApiPath || ArrayUtils.isEmpty(path)) {
            return path;
        }
        String baseApiPath = ApiVersioningConfiguration.getApiPath();
        Set<String> newPath = new HashSet<>();
        for (String p : path) {
            newPath.add(removeTrailingSlash(baseApiPath) + prependSlash(p));
        }
        return newPath.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    private String[] mergeStrArrays(String[] a1, String[] a2) {
        if (logger.isTraceEnabled()) {
            logger.trace(
                    "Merging arrays [" + StringUtils.join(a1, ", ") + "] and [" + StringUtils.join(a2, ", ") + "]");
        }
        String[] array = ArrayUtils.addAll(a1, a2);
        Set<String> newSet = new HashSet<>();
        newSet.addAll(Arrays.asList(array));
        if (logger.isTraceEnabled()) {
            logger.trace("After merge: " + newSet);
        }
        return newSet.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    public RequestMethod[] getMethods() {
        return methods;
    }

    public String[] getParams() {
        return params;
    }

    public String[] getHeaders() {
        return headers;
    }

    public String[] getConsumes() {
        return consumes;
    }

    public String[] getProduces() {
        return produces;
    }

    public String getName() {
        return name;
    }

    public RequestConditionHolder getCondition() {
        return condition;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        if (ArrayUtils.isEmpty(paths)) {
            builder.append("NO_PATH");
        } else {
            builder.append(StringUtils.join(paths, ", "));
        }
        if (!ArrayUtils.isEmpty(methods)) {
            builder.append(",methods=").append(StringUtils.join(methods, ", "));
        }
        if (!ArrayUtils.isEmpty(params)) {
            builder.append(",params=").append(StringUtils.join(params, ", "));
        }
        if (!ArrayUtils.isEmpty(headers)) {
            builder.append(",headers=").append(StringUtils.join(headers, ", "));
        }
        if (!ArrayUtils.isEmpty(consumes)) {
            builder.append(",consumes=").append(StringUtils.join(consumes, ", "));
        }
        if (!ArrayUtils.isEmpty(produces)) {
            builder.append(",produces=").append(StringUtils.join(produces, ", "));
        }
        if (!StringUtils.isEmpty(name)) {
            builder.append(",name=").append(name);
        }
        if (condition != null && !condition.isEmpty()) {
            builder.append(",custom=").append(condition);
        }
        builder.append('}');
        return builder.toString();
    }
}
