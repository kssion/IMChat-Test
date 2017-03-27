package web.user;

import java.sql.SQLException;
import java.util.ArrayList;

import json.JSONObject;
import tools.DBUtil_v2;
import tools.Foundation;
import web.pub.JsonModel;

/**
 * 用户类
 * Created by Chance on 2017/3/11.
 */
public class User {
    private String unid;
    private String token;
    private boolean isValid;

    public User(String unid) {
        this.unid = unid;

        if (unid == null) {
            isValid = true;
        }

        // 用户不存在 isValid = false;
    }

    public String getUnid() {
        return unid;
    }

    public boolean isValid() {
        return isValid;
    }

//    public boolean login() {
//        String unid = request.getParameter("unid");
//        String pwd = request.getParameter("pwd");
//
//        if (unid == null || pwd == null) {
//            return new JsonModel(1, "请输入用户名或密码");
//        }
//
//        JsonModel model;
//
//        try {
//
//            JSONObject result = DBUtil_v2.querySimpleResult("SELECT count(*) AS count FROM tb_account WHERE unid=? AND password=?", unid, pwd);
//            int count = result.getInt("count");
//
//            if (count > 0) {
//
//                // 生成token 写入数据库
//                String token = Foundation.MD5(String.valueOf(System.currentTimeMillis()));
//                count = DBUtil_v2.executeUpdate("REPLACE INTO tb_login_info(unid, token) VALUE (?, ?)", unid, token);
//
//                if (count > 0) {
//                    model = new JsonModel(0, "登录成功");
//                    model.data.put("token", token);
//                    return model;
//                } else {
//                    model = new JsonModel(1, "授权失败");
//                    return model;
//                }
//            } else {
//                model = new JsonModel(1, "用户名或密码不正确");
//                return model;
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        model = new JsonModel(1, "登录失败，请重试！");
//        return model;
//    }
    /**
     *  获取token
     */
    public String getToken() {
        if (!this.isValid()) return null;

        try {
            JSONObject data = DBUtil_v2.querySimpleResult("SELECT token FROM tb_login_info WHERE unid=?", unid);
            if (data != null) {
                return data.getString("token");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *  是否存在好友
     */
    public boolean isAmigo(String unid_amigo) {
        if (!this.isValid()) return false;

        try {
            JSONObject data = DBUtil_v2.querySimpleResult("SELECT count(*) AS count FROM tb_amigo " +
                    "WHERE (unid_u=? AND unid_n=?) OR (unid_n=? AND unid_u=?)", unid, unid_amigo, unid, unid_amigo);
            if (data.getInt("count") > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 检查是否授权状态
     */
    public boolean checkAuthorized(String token) {
        if (!this.isValid()) return false;
        return (token != null && !token.equals(this.token));
    }

    /**
     *  获取好友账号列表
     */
    public ArrayList getAmigoList() {
        if (!this.isValid()) return null;

        ArrayList<String> amigos = new ArrayList<>();
        try {
            ArrayList<JSONObject> list = DBUtil_v2.queryMultipleResult("SELECT unid_u AS unid FROM tb_amigo WHERE unid_n=? UNION SELECT unid_n FROM tb_amigo WHERE unid_u=?", unid);
            for (JSONObject j : list) {
                amigos.add(j.getString("unid"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return amigos;
    }

}
