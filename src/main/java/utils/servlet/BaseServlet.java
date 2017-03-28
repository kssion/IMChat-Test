/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.utils.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @time    2016.04.01
 * @author  Chance
 */
public class BaseServlet extends HttpServlet {
    
    public BaseServlet() {
        super();
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void init() throws ServletException {

    }

    @SuppressWarnings("unchecked")
    final Object process(HttpServletRequest request, HttpServletResponse response) {

        Map<String, String[]> params = request.getParameterMap();

        int i = 0;
        System.out.print("{");
        for (String key : params.keySet()) {
            String[] args = params.get(key);
            if (i++ > 0) {
                System.out.print(", ");
            }

            System.out.print(key);
            System.out.print(":[");
            for (String v : args) {
                if (!args[0].equals(v)) {
                    System.out.print(", ");
                }
                System.out.print(v);
            }
            System.out.print("]");
        }
        System.out.println("}");

        Object outobj;

        String URI = request.getRequestURI();
        String thisName = this.getServletName() + "/";

        String[] strs = URI.split(thisName);

        if (strs.length == 2 && !strs[1].contains("/")) {
            String fucName = strs[1];
            Class clz = this.getClass();
            Method method = null;

            try {

                Method[] methods = clz.getDeclaredMethods();

                for (Method m : methods) {
                    if (fucName.equals(m.getName())) {
                        method = m;
                    }
                }

                HashMap<String, String> map = new HashMap<>();
                for (String k : params.keySet()) {
                    String[] vs = params.get(k);
                    String value = null;
                    if (vs.length > 0) {
                        value = vs[vs.length - 1];
                    }
                    map.put(k, value);
                }

                if (method != null) {

                    Class[] clazz = method.getParameterTypes();

                    if (clazz.length == 0) {
                        return method.invoke(this);
                    } else if (Arrays.equals(clazz, new Class[]{HttpServletRequest.class})) {
                        return method.invoke(this, request);
                    } else if (Arrays.equals(clazz, new Class[]{Map.class})) {
                        return method.invoke(this, map);
                    } else if (Arrays.equals(clazz, new Class[]{HttpServletRequest.class, HttpServletResponse.class})) {
                        return method.invoke(this, request, response);
                    }
                }
                return new JsonModel(1, "NoSuchMethodException");

            } catch (SecurityException e) {
                outobj = new JsonModel(1, "SecurityException");
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                outobj = new JsonModel(1, "IllegalAccessException");
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                outobj = new JsonModel(1, "IllegalArgumentException");
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                outobj = new JsonModel(1, "InvocationTargetException");
                e.printStackTrace();
            }
        } else {
            outobj = new JsonModel(1, "请求的URL不正确");
        }
        return outobj;
    }

    public Object url(HttpServletRequest request) {
        return null;
    }
}
