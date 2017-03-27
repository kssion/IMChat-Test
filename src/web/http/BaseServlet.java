/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web.http;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import web.pub.JsonModel;

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

                if (method != null) {

                    switch (method.getParameterCount()) {
                        case 0: outobj = method.invoke(this); break;
                        case 1: outobj = method.invoke(this, request); break;
                        case 2: outobj = method.invoke(this, request, response); break;
                        default: outobj = new JsonModel(1, "NoSuchMethodException"); break;
                    }
                } else {
                    outobj = new JsonModel(1, "NoSuchMethodException");
                }

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
