package com.study.mvcframework.pojo;

import com.study.mvcframework.annotations.Security;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Handler {

    private Object controller;
    private Method method;
    private Pattern pattern;
    private String uri;
    private Map<String, Integer> paramIndexMapping = new HashMap();

    public Handler() {
    }

    public Handler(Object controller, Method method, String uri) {
        this.controller = controller;
        this.method = method;
        this.uri = uri;
        this.pattern = Pattern.compile(uri);
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Map<String, Integer> getParamIndexMapping() {
        return paramIndexMapping;
    }

    public void setParamIndexMapping(Map<String, Integer> paramIndexMapping) {
        this.paramIndexMapping = paramIndexMapping;
    }

    public boolean auth(HttpServletRequest req) {
        if (!controller.getClass().isAnnotationPresent(Security.class) && !method.isAnnotationPresent(Security.class)) {
            return true;
        }
        String id = req.getParameter("id");
        List<String> ids = Arrays.asList(method.getAnnotation(Security.class).value());
        return ids.contains(id);
    }
}
