package socket;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.javafx.scene.control.skin.VirtualFlow.*;
import json.JSONObject;

/**
 * Created by Chance on 2017/2/18.
 */
public class IMMessageManager {
    private static ConcurrentHashMap<String, ArrayLinkedList<String>> msgArray = new ConcurrentHashMap<>();

    /**
     * 添加离线消息
     * @param unid unid
     * @param msg msg
     */
    public static void addOfflineMessage(String unid, String msg) {
        ArrayLinkedList<String> msgs = (ArrayLinkedList<String>)getOfflineMessage(unid);
        if (null == msgs) {
            msgs = new ArrayLinkedList<>();
            msgArray.put(unid, msgs);
        }
        msgs.addLast(msg);
        System.out.println("添加离线消息：" + unid + ":" + msg);
    }
    public static void removeOfflineMessage(String unid) {
        if (null != unid && msgArray.containsKey(unid)) {
            msgArray.remove(unid);
            System.out.println("移除离线消息：" + unid);
        }
    }
    public static List<String> getOfflineMessage(String unid) {
        if (null != unid && msgArray.containsKey(unid)) {
            System.out.println("获取离线消息列表：" + unid);
            return msgArray.get(unid);
        }
        return null;
    }

}
