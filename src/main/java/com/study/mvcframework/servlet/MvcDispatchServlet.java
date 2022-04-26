package com.study.mvcframework.servlet;

import com.study.mvcframework.annotations.*;
import com.study.mvcframework.controller.DemoController;
import com.study.mvcframework.pojo.Handler;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MvcDispatchServlet extends HttpServlet {

    private final Properties properties = new Properties();

    private final List<String> fullClassNameList = new ArrayList();

    private final Map<String, Object> beanFactory = new HashMap();

    private final Map<String, Handler> handlerMap = new HashMap();

//    private final List<Handler> handlerMap = new ArrayList();

    // 1、ioc容器
    // 2、根据uri根据method，反射调用method.invoke
    // 3、反射调用必须有controller实例（handler：method、controller、参数下标对应）


    @Override
    public void init(ServletConfig config) throws ServletException {

        // 1、加载配置文件
        doLoadProperties(config.getInitParameter("contextConfigLocation"));
        // 2、扫描配置文件中字节码文件路径，得到全类名
        doScan((String) properties.get("base.package"));
        // 3、根据字节码文件全类名生成ioc容器（基于注解@Service、@Controller）
        doInstance();
        // 4、依赖注入（基于@Autowired注解）
        doAutowired();
        doInitHandlerMapping();
    }



    private void doInitHandlerMapping() {
        if (beanFactory.isEmpty()) return;
        for (Map.Entry<String, Object> entry : beanFactory.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            boolean flag = clazz.isAnnotationPresent(Controller.class);
            if (!flag) continue;
            RequestMapping anno = clazz.getAnnotation(RequestMapping.class);
            String uri = "/";
            if (anno != null && !anno.value().trim().equals("")) {
                uri += anno.value();
            }
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(RequestMapping.class)) continue;
                RequestMapping methodAnno = method.getAnnotation(RequestMapping.class);
                String methodUri = methodAnno.value();
                if (!uri.endsWith("/")) uri = uri + "/" + methodUri;
                else uri += methodUri;
                Handler handler = new Handler(entry.getValue(), method, uri);
                Parameter[] parameters = method.getParameters();
                for (int i = 0; i < parameters.length; i++) {
                    Parameter parameter = parameters[i];
                    if (parameter.getType() == HttpServletRequest.class || parameter.getType() == HttpServletResponse.class) {
                        handler.getParamIndexMapping().put(parameter.getType().getSimpleName(), i);
                    } else {
                        handler.getParamIndexMapping().put(parameter.getName(), i);
                    }
                }
                handlerMap.put(uri, handler);
            }
        }
    }

    private void doAutowired() {
        if (beanFactory.isEmpty()) return;
        for (Map.Entry<String, Object> entry : beanFactory.entrySet()) {
            Object val = entry.getValue();
            Class<?> clazz = val.getClass();
            if (!clazz.isAnnotationPresent(Controller.class)) {
                continue;
            }
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(AutoWired.class)) continue;
                AutoWired service = field.getAnnotation(AutoWired.class);
                String value = service.value();
                field.setAccessible(true);
                if (value.trim().equals("")) {
                    String name = field.getType().getName();
                    try {
                        field.set(val, beanFactory.get(name));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        field.set(val, beanFactory.get(value));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void doInstance() {
        if (fullClassNameList.size() == 0) return;
        for (String fullClassName : fullClassNameList) {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(fullClassName);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
//            boolean flag = clazz.isAnnotationPresent(Service.class) || clazz.isAnnotationPresent(Controller.class);
//            if (flag) {
//                String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
//                beanFactory.put(toLowerFirst(className), clazz);
//            }
            try {
                if (clazz.isAnnotationPresent(Service.class)) {
                    String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
                    beanFactory.put(toLowerFirst(className), clazz.newInstance());
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> interClazz : interfaces) {
                        beanFactory.put(interClazz.getName(), clazz.newInstance());
                    }
                } else if (clazz.isAnnotationPresent(Controller.class)) {
                    String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
                    beanFactory.put(toLowerFirst(className), clazz.newInstance());
                }
            } catch (IllegalAccessException | InstantiationException e) {

            }
        }
    }

    private String toLowerFirst(String className) {
        char[] chars = className.toCharArray();
        if (chars[0] >= 'A' && chars[0] <= 'Z') chars[0] += 32;
        return String.valueOf(chars);
    }

    private void doScan(String basePackage) {
        String basePath = basePackage.replaceAll("\\.", "/");
        String absPath = Thread.currentThread().getContextClassLoader().getResource("").getPath() + basePath;
        File file = new File(absPath);
        File[] childFiles = file.listFiles();
        for (File childFile : childFiles) {
            if (childFile.isFile()) {
                String name = childFile.getName();
                if (name.endsWith(".class")) {
                    String className = name.split("\\.")[0];
                    fullClassNameList.add(String.join(".", basePackage, className));
                }
            } else {
                doScan(basePackage + "." + childFile.getName());
            }
        }
    }

    private void doLoadProperties(String initParameter) {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(initParameter);
        try {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        String uri = req.getRequestURI();
        Handler handler = getHandler(uri);
        if (handler == null) {
            try {
                resp.getWriter().print("404 NOT FOUND! ");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        if (!handler.auth(req)) {
            try {
                resp.getWriter().print("pession denied! ");
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Method method = handler.getMethod();
        Map<String, Integer> paramIndexMapping = handler.getParamIndexMapping();
        Map<String, String[]> parameterMap = req.getParameterMap();
        Object[] params = new Object[paramIndexMapping.size()];
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String name = entry.getKey();
            if (!paramIndexMapping.containsKey(name)) continue;
            Integer index = paramIndexMapping.get(name);
            String[] vals = parameterMap.get(name);
            params[index] = Integer.valueOf(StringUtils.join(vals, ","));
        }
        Integer reqIdx = paramIndexMapping.get(HttpServletRequest.class.getSimpleName());
        if (reqIdx != null) {
            params[reqIdx] = req;
        }
        Integer respIdx = paramIndexMapping.get(HttpServletResponse.class.getSimpleName());
        if (respIdx != null) {
            params[respIdx] = resp;
        }
        try {
            method.invoke(handler.getController(), params);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private Handler getHandler(String uri) {
        Handler handler = handlerMap.get(uri);
        return handler;
//        for (Map.Entry<String, Handler> entry : handlerMap.entrySet()) {
//            Handler handler = entry.getValue();
//            Pattern pattern = handler.getPattern();
//            Matcher matcher = pattern.matcher(uri);
//            if (matcher.matches()) {
//                return handler;
//            }
//        }
//        return null;
    }

}
