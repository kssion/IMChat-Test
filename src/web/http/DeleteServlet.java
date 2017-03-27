/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web.http;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import json.*;

/**
 * @time    2016.04.01
 * @author  Chance
 */
public class DeleteServlet extends BaseServlet {
    
    public DeleteServlet() {
        super();
    }
    
    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/json;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        
        try (PrintWriter out = response.getWriter()) {
            Object json = process(request, response);
            if (json == null) {
                json = new JSONObject().put("status", "1").put("msg", "未返回数据！");
                
            }
            out.write(json.toString());
	} catch (Exception ex) {
            throw new ServletException(ex);
	}
    }
}
