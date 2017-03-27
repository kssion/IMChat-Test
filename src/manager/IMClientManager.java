package manager;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Auth: Chance
 * Created by Chance on 2017/3/14.
 */
public class IMClientManager {

    private static final HashMap<String, IMClient> clientsMap = new HashMap<>();
    private static final ArrayList<IMClient> reusingMap = new ArrayList<>(); // 可复用的客户端处理程序

    // 客户进入 添加
    public static void addSocket(Socket s) {
        IMClient client;
        synchronized (reusingMap) {
            if (reusingMap.isEmpty()) {
                client = reusingMap.remove(0);
            } else {
                client = new IMClient();
            }
        }
        client.start();
    }

    // 获取客户端
    public static IMClient getClient(String unid) {
        synchronized (clientsMap) {
            if (null != unid) {
                return clientsMap.get(unid);
            }
            return null;
        }
    }

    // 登录成功 添加到列表
    public static void addClient(IMClient client) {
        synchronized (clientsMap) {
            if (clientsMap.containsKey(client.getUnid())) {
                IMClient c = clientsMap.get(client.getUnid());
                c.setOffline();
            }
            clientsMap.put(client.getUnid(), client);
        }
    }

    // 退出或离线 移除 添加到复用列表
    public static void removeClient(IMClient client) {
        synchronized (clientsMap) {
            if (null != client) {
                if (null != client.getUnid() && clientsMap.containsKey(client.getUnid())) {
                    clientsMap.remove(client.getUnid());
                }
                synchronized (reusingMap) {
                    reusingMap.add(client);
                }
            }
        }
    }

}
