package web.user;

import java.sql.SQLException;
import java.util.ArrayList;

import json.JSONObject;
import socket.IMClientManager;
import socket.IMMessageManager;
import tools.DBUtil_v2;
import tools.Foundation;
import web.pub.JsonModel;

/**
 * 用户工具类
 * Created by Chance on 2017/3/3.
 */
public class Usersx {

    /**
     *  用户是否存在
     */
    public static boolean existUser(String unid) {

        if (unid != null) {
            try {
                JSONObject data = DBUtil_v2.querySimpleResult("SELECT count(*) AS count FROM tb_account WHERE unid=?", unid);
                return data.getInt("count") > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 检查是否授权状态
     */
    public static boolean checkAuthorized(String unid, String token) {

        if (unid != null) {
            try {
                JSONObject data = DBUtil_v2.querySimpleResult("SELECT count(*) AS count FROM tb_login_info WHERE unid=? AND token=?", unid, token);
                return data.getInt("count") > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     *  获取token
     */
    public static String getToken(String unid) {
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
    public static boolean existsAmigo(String unid, String unid_amigo) {

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
     *  获取好友账号列表
     */
    public static ArrayList getAmigoList(String unid) {

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
