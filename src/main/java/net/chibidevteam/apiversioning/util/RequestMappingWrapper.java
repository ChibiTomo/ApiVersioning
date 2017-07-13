package net.chibidevteam.apiversioning.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.chibidevteam.apiversioning.annotation.ApiRequestMapping;

/**
 * This class is used to wrap {@link RequestMapping @RequesMapping} and {@link ApiRequestMapping @ApiRequesMapping}
 * 
 * @author Yannis THOMIAS
 */
public class RequestMappingWrapper {

    /** Logger that is available to subclasses */
    protected final Log     logger = LogFactory.getLog(getClass());

    private String[]        paths;
    private RequestMethod[] methods;
    private String[]        params;
    private String[]        headers;
    private String[]        consumes;
    private String[]        produces;
    private String          name;

    public RequestMappingWrapper(ApiRequestMapping apiRequestMapping) {
        if (apiRequestMapping != null) {
            paths = apiRequestMapping.path();
            methods = apiRequestMapping.method();
            params = apiRequestMapping.params();
            headers = apiRequestMapping.headers();
            consumes = apiRequestMapping.consumes();
            produces = apiRequestMapping.produces();
            name = apiRequestMapping.name();
        }
    }

    public RequestMappingWrapper(RequestMapping requestMapping) {
        if (requestMapping != null) {
            paths = requestMapping.path();
            methods = requestMapping.method();
            params = requestMapping.params();
            headers = requestMapping.headers();
            consumes = requestMapping.consumes();
            produces = requestMapping.produces();
            name = requestMapping.name();
        }
    }

    public String[] getPaths() {
        return paths;
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
        builder.append('}');
        return builder.toString();
    }
}
