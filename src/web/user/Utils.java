package web.user;

import web.http.GetServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Chance on 2017/3/20.
 */
@WebServlet(name = "Utils", urlPatterns = "/Utils/*")
public class Utils extends GetServlet {

    /**
     * 同步时间
     */
    public Object synctime(HttpServletRequest request) {
        return System.currentTimeMillis() / 1000.00;
    }
}
