package web.user;

import java.sql.SQLException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import socket.IMMessageManager;
import web.http.GetServlet;
import json.JSONObject;
import web.pub.JsonModel;
import socket.IMClientManager;
import tools.DBUtil_v2;
import tools.Foundation;

/**
 * 用户接口
 * Created by Chance on 2017/2/26.
 */
@WebServlet(name = "Users", urlPatterns = {"/Users/*"})
public class Users extends GetServlet {

    /**
     * 登录
     */
    public Object login(HttpServletRequest request) {

        String unid = request.getParameter("unid");
        String pwd = request.getParameter("pwd");
        String sign = request.getParameter("sign");

        if (unid == null || pwd == null) {
            return new JsonModel(1, "请输入用户名或密码");
        }

        if (sign == null) {
            return new JsonModel(1, "授权失败");
        }

        JsonModel model;

        try {

            JSONObject result = DBUtil_v2.querySimpleResult("SELECT password FROM tb_account WHERE unid=?", unid);
            if (null != result) {
                String p = result.getString("password");

                String time_sign = Foundation.decryption_sign(sign);
                String p_v = Foundation.SHA256(time_sign + "#" + p);

                if (pwd.equals(p_v)) {
                    // 生成token 写入数据库
                    String token = Foundation.MD5(String.valueOf(System.currentTimeMillis()));
                    DBUtil_v2.executeUpdate("REPLACE INTO tb_login_info(unid, token) VALUE (?, ?)", unid, token);
                    model = new JsonModel(0, "登录成功");
                    model.data.put("token", token);
                    return model;
                }
            }
            model = new JsonModel(1, "用户名或密码不正确");
            return model;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        model = new JsonModel(1, "登录失败，请重试！");
        return model;
    }

    /**
     * 注册
     */
    public Object register(HttpServletRequest request) {

        String unid = request.getParameter("unid");
        String pwd = request.getParameter("pwd");

        if (unid == null || pwd == null) {
            return new JsonModel(1, "请输入用户名或密码");
        }

        Foundation.getIpAddress(request);

        JsonModel json;

        try {

            JSONObject result = DBUtil_v2.querySimpleResult("SELECT count(*) AS count FROM tb_account WHERE unid=?", unid);
            int count = result.getInt("count");
            if (count > 0) {
                json = new JsonModel(1, "用户名已经注册");
                return json;

            } else {

                count = DBUtil_v2.executeUpdate("INSERT INTO tb_account(unid, password) VALUE (?, ?)", unid, pwd);

                if (count > 0) {
                    // 记录注册时间
                    DBUtil_v2.executeUpdate("INSERT INTO tb_user_info(unid) VALUE (?)", unid);
                    json = new JsonModel(0, "注册成功");
                    return json;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        json = new JsonModel(1, "注册失败，请重试！");
        return json;
    }

    /**
     * 获取用户信息
     */
    public Object getInfo(HttpServletRequest request) {

        String unid = request.getParameter("unid");

        if (unid == null) {
            return new JsonModel(1, "请输入unid");
        }

        JsonModel json;
        try {
            JSONObject data = DBUtil_v2.querySimpleResult("SELECT i.unid,i.register_date FROM tb_account AS a,tb_user_info AS i WHERE a.unid=? AND a.unid=i.unid", unid);

            if (data == null) {
                data = new JSONObject();
            }
            json = new JsonModel(0, "查询成功");
            json.data = data;
            return json;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        json = new JsonModel(1, "查询失败");
        return json;
    }

    /**
     * 添加好友
     */
    public Object applyAmigo(HttpServletRequest request) {

        String unid = request.getParameter("unid");
        String unid_amigo = request.getParameter("unid_apply");
        String token = request.getParameter("token");
        {
            /*
             token：sha256(unid:unid_amigo#token)
              */
        }

        if (unid == null) {
            return new JsonModel(1, "unid不能为空");
        }

        if (unid_amigo == null) {
            return new JsonModel(1, "好友unid不能为空");
        }

        if (unid.equals(unid_amigo)) {
            return new JsonModel(1, "不能添加自己为好友");
        }

        if (Usersx.existsAmigo(unid, unid_amigo)) {
            return new JsonModel(1, "已经是好友");
        }

        JsonModel json = null;

        try {

            JSONObject data = DBUtil_v2.querySimpleResult("SELECT count(*) AS count FROM tb_account WHERE unid=?", unid_amigo);
            if (data.getInt("count") == 0) {
                json = new JsonModel(1, "用户不存在");
            } else {

                String t = Usersx.getToken(unid);
                if (t != null) {
                    String a_token = Foundation.SHA256(unid + ":" + unid_amigo + "#" + t);
                    if (a_token.equals(token)) {

                        // 查询有没有申请记录
                        JSONObject record = DBUtil_v2.querySimpleResult("SELECT * FROM tb_apply_amigo WHERE unid=? AND to_unid=?", unid, unid_amigo);
                        if (record != null) {
                            return new JsonModel(0, "已经发送过加好友请求，等待对方同意！");
                        }

                        String msgid = System.currentTimeMillis() + "";
                        String aid = Foundation.MD5(msgid);

                        // 加好友请求信息
                        JSONObject rjson = new JSONObject("{'code':'20005'}");
                        rjson.put("unid", 10005);
                        rjson.put("apply_unid", unid);
                        rjson.put("msgid", msgid);
                        rjson.put("aid", aid);

                        int i = DBUtil_v2.executeUpdate("INSERT INTO tb_apply_amigo(aid, unid, to_unid) VALUES (?,?,?)",
                                aid, unid, unid_amigo);
                        if (i > 0) {
                            IMClientManager.IMClient amigo = IMClientManager.getClient(unid_amigo);
                            if (amigo != null) {
                                amigo.writeMessage(rjson.getString("msgid"), rjson);
                            } else {
                                IMMessageManager.addOfflineMessage(unid_amigo, rjson.toString());
                            }
                            json = new JsonModel(0, "加好友请求已经发送");
                        } else {
                            json = new JsonModel(1, "申请添加好友失败");
                        }

                    } else {
                        json = new JsonModel(1, "身份验证失败");
                    }
                } else {
                    json = new JsonModel(1, "尚未登录");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return json;
    }

    /*

     */
    public Object applyAmigoResult(HttpServletRequest request) {
        // {aid:xxxxx, unid:xxx, token:xxx} # token: sha256(aid@unid#token)

        String aid = request.getParameter("aid");
        String unid = request.getParameter("unid"); // 自己的unid
        String unid_amigo = request.getParameter("unid_amigo"); // 申请人的unid
        String token = request.getParameter("token");
        String status = request.getParameter("status");
        {
            /*
             token：sha256(aid@unid#token)
              */
        }

        if (aid == null) {
            return new JsonModel(1, "事件id不能为空");
        }

        if (unid == null) {
            return new JsonModel(1, "unid不能为空");
        }

        if (unid_amigo == null) {
            return new JsonModel(1, "unid_amigo不能为空");
        }

        JsonModel json = null;

        try {

            JSONObject data = DBUtil_v2.querySimpleResult("SELECT count(*) AS count FROM tb_apply_amigo WHERE aid=?", aid);
            if (data.getInt("count") == 0) {
                return new JsonModel(1, "事件id不存在");
            }

            String t = Usersx.getToken(unid);

            if (t != null) {
                String a_token = Foundation.SHA256(aid + "@" + unid + "#" + t);
                if (a_token.equals(token)) {

                    boolean isOK = true;
                    if ("1".equals(status)) { // 同意
                        // 更新好友
                        isOK = DBUtil_v2.executeUpdate("INSERT INTO tb_amigo (unid_u, unid_n) VALUES (?,?)", unid_amigo, unid) == 1;

                        if (isOK) {

                            String msgid = System.currentTimeMillis() + "";

                            // 加好友请求信息
                            JSONObject rjson = new JSONObject("{'code':'200050'}");
                            rjson.put("unid", 10005);
                            rjson.put("unid_amigo", unid);
                            rjson.put("msgid", msgid);
                            rjson.put("aid", aid);
                            rjson.put("msg", "对方已同意好友申请");

                            // 通知申请人
                            IMClientManager.IMClient amigo = IMClientManager.getClient(unid_amigo);
                            if (amigo != null) {
                                amigo.writeMessage(rjson.getString("msgid"), rjson);
                            } else {
                                IMMessageManager.addOfflineMessage(unid_amigo, rjson.toString());
                            }

                            json = new JsonModel(0, "已同意好友申请");
                        } else {
                            return new JsonModel(1, "添加好友失败");
                        }

                    } else {
                        json = new JsonModel(0, "已拒绝好友申请");
                    }

                    // 删除好友请求
                    isOK &= DBUtil_v2.executeUpdate("DELETE FROM tb_apply_amigo WHERE aid=?", aid) == 1;

                } else {
                    json = new JsonModel(1, "身份验证失败");
                }
            } else {
                json = new JsonModel(1, "尚未登录");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return json;
    }

}
