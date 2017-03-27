package web.pub;

import json.JSONArray;
import json.JSONObject;

/**
 * Created by Chance on 2017/2/28.
 */
public class JsonModel {

    private JSONObject json;

    public int status;
    public String msg;
    public JSONObject data = new JSONObject();
    public JSONArray list = new JSONArray();


    public JsonModel() {
        super();
       json = new JSONObject();
    }

    public JsonModel(int status) {
        this();
        this.status = status;
        json.put("status", status);
    }

    public JsonModel(int status, String msg) {
        this(status);
        this.msg = msg;
        json.put("msg", msg);
    }

    @Override
    public String toString() {
        if (data != null && data.length() > 0) {
            json.put("data", data);
        } else if (data != null && list.length() > 0) {
            json.put("data", list);
        } else {
            json.put("data", JSONObject.NULL);
        }
        return json.toString();
    }
}
