package main.java.api;

import main.java.utils.servlet.*;

import javax.servlet.annotation.WebServlet;

/**
 * Created by Chance on 2017/3/20.
 */
@WebServlet(name = "Utils", urlPatterns = "/Utils/*")
public class Utils extends GetServlet {

    /**
     * 同步时间
     */
    public Object synctime() {
        return System.currentTimeMillis() / 1000.00;
    }
}
