package manager;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;

import com.sun.javafx.scene.control.skin.VirtualFlow;
import json.JSONArray;
import json.JSONObject;
import socket.*;
import web.user.Usersx;

/**
 * Auth: Chance
 * Created by Chance on 2017/3/14.
 */
public class IMClient extends Thread {
    private static final byte[] header_symbol = new byte[]{0x05, 0x02};
    private static final byte[] blank_bytes = new byte[]{0, 0, 0, 0};
    private static final byte[] keep_alive_bytes = new byte[]{0x7b, 0x7d};
    private static final int kReceiveTimeDelay = (10 + 1) * 1000;

    private Socket socket = null;
    // 输入流
    private InputStream inputStream = null;
    // 输出流
    private DataOutputStream dataOutputStream = null;

    // 上次接收时间
    private long lastReceiveTime = System.currentTimeMillis();
    // 运行中
    private boolean running = true;
    // 在线
    private boolean online = false;

    // unid
    private String unid = null;

    // 正在发送消息列表
    private LinkedHashMap<Object, JSONObject> messageList = new LinkedHashMap<>();

    // 数据接收处理
    private int length = 0;
    private ByteArrayOutputStream dataPartStorage = new ByteArrayOutputStream();

    public IMClient() {

    }

    @Override
    public String toString() {
        return String.format("<IMClient unid = %s>", this.unid);
    }

    public String getUnid() {
        return unid;
    }
    public boolean isOnline() {
        return online;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    /**
     * 写入数据
     */
    private void write(byte[] bytes) throws IOException {
        try {
            int len = bytes.length;
            dataOutputStream.write(header_symbol);
            dataOutputStream.write((len >>> 24) & 0xFF);
            dataOutputStream.write((len >>> 16) & 0xFF);
            dataOutputStream.write((len >>> 8) & 0xFF);
            dataOutputStream.write((len) & 0xFF);
            if (bytes.length > 0) {
                dataOutputStream.write(bytes);
            }
            dataOutputStream.flush();
            lastReceiveTime = System.currentTimeMillis();
        } catch (IOException ex) {
            throw new IOException(ex);
        }
    }

    public void write(String string) throws IOException {
        this.write(string.getBytes());
    }

    public void writeData(JSONObject json) {
        try {
            this.write(json.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送数据（给自己）
     * @param json 消息对象
     */
    public void writeMessage(String msgid, JSONObject json) {
        try {
            messageList.put(msgid, json);
            this.write(json.toString());
        } catch (IOException e) {
            e.printStackTrace();
            this.running = false;
        }
    }

    /**
     * 保存未发送的消息
     */
    void saveMessageToOffline() {
        System.err.println("saveMessageToOffline");
        if (messageList.size() > 0) {
            Collection<JSONObject> msgs = messageList.values();
            for (JSONObject json : msgs) {
                IMMessageManager.addOfflineMessage(this.unid, json.toString());
            }
            messageList.clear();
            System.out.println("消息保存至离线:" + this);
        }
    }

    /**
     *	断开当前用户
     */
    private void clientEnd() {
        System.err.println("clientEnd");
        try {
            inputStream.close();
            dataOutputStream.close();
            dataPartStorage.close();

            if (null != socket && !socket.isClosed()) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {

            IMClientManager.removeClient(this);
            System.err.println("Remove Client.");
            unid = null;
        }
    }

    public void setOffline() {
        this.online = false;
        this.running = false;
    }

    @Override
    public void run() {

        System.out.println("client in:" + socket.getRemoteSocketAddress());

        try {
            inputStream	= socket.getInputStream();
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            while (running) {

                if (System.currentTimeMillis() - lastReceiveTime > kReceiveTimeDelay) {
                    System.err.println("通信超时");
                    break;

                } else {
                    if (inputStream.available() > 0) {
                        this.readInputStream(inputStream);
                        lastReceiveTime = System.currentTimeMillis();
                    } else {
                        Thread.sleep(100);
                    }
                }
            }

        } catch (Exception ex) {
            System.out.println("run: -> " + ex.getMessage());
        } finally {

            this.online = false;
            System.out.println("client out:" + socket.getRemoteSocketAddress());
            this.saveMessageToOffline();

            this.clientEnd();
        }
    }

    private void readInputStream(InputStream inStream) {

        int readLength = 4096;
        byte[] buffer = new byte[readLength];
        int len;

        try {
            while ((len = inStream.read(buffer)) > -1) {
                if (len > 0) {
                    dataPartStorage.write(buffer, 0, len);
                    byte[] bytes = dataPartStorage.toByteArray();
                    dataPartStorage.reset();

                    System.out.println((unid==null ? "" : unid + ":") + new String(buffer, 0, len));

                    this.dataLoop(bytes);
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void dataLoop(byte[] bytes) { // 包含数据头部

        if (bytes.length >= 6) {
//				System.out.println("len >= 6; len = " + bytes.length);

            ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
            outSteam.write(bytes, 0, 2);

            // Header:\x05\x02 \x00\x00\x00\x00 content
            boolean isEqual = Arrays.equals(outSteam.toByteArray(), header_symbol);
            outSteam.reset();

            if (isEqual) {
//					System.out.println("isEqual = true;");
                this.length = Utils.NumberUtil.byte4ToInt(bytes, 2);
                // 剩余
                int surplus = bytes.length - 6 - this.length;
//					System.out.println("len:" + length + " surplus:" + surplus);

                // 大于或等于
                if (surplus >= 0) {
//						System.out.println("surplus = 0");

                    outSteam.write(bytes, 6, length);
                    // 拿到完整数据 进行处理
//						System.err.println("<< 处理数据 >>\n");
                    this.receiveDataHandle(outSteam.toByteArray());

                    if (surplus > 0) {
//							System.out.println("surplus > 0");
                        outSteam.reset();
                        outSteam.write(bytes, bytes.length - surplus, surplus);
                        dataLoop(outSteam.toByteArray());
                    }
                } else { // 数据不足
                    System.out.println("数据不足");
                    dataPartStorage.write(bytes, 0, bytes.length);
                    System.err.println(dataPartStorage.toString());
                }
            } else {
                System.err.println("数据头部不能被解析");
                int index = ArraysUtil.search(bytes, header_symbol);
                if (index > 0) {
                    dataPartStorage.write(bytes, index, bytes.length - index);
                }
            }
        } else {
            dataPartStorage.write(bytes, 0, bytes.length);
        }
    }

    /**
     * 处理收到数据
     * @param bytes 数据
     */
    private void receiveDataHandle(byte[] bytes) {
        try {

            // 两个字节 心跳包
            if (Arrays.equals(keep_alive_bytes, bytes)) {
                this.write("{}");
                return;
            }

            JSONObject json = new JSONObject(new String(bytes));

            int t = json.getInt("code");
            switch (t) {
					/*
					 	收到数据->回复
					 */
                case 10001: // 授权登录
                {
                    this.receive_authorizeHandle(json);
                    break;
                }
                case 10002: // 发送消息转发
                {
                    this.receive_sendMessageHandle(json);
                    break;
                }

					/*
					 	发送数据->收到回复
					 */
                case 20002: // 发送消息ok
                {
                    // {'code':2,'unid':'rencx','msg':'hello'}

                    Object msgid = json.get("msgid");
                    if (msgid == null) {
                        System.out.println("msgid is null.");
                    } else {
                        messageList.remove(msgid);
                    }
                    break;
                }
                case 20003: // 发送离线消息ok
                {
                    System.out.println("离线消息发送成功 清理离线消息");
                    IMMessageManager.removeOfflineMessage(this.unid);
                    break;
                }
                case 20005: { // 发送好友申请ok

                    System.out.println("发送好友申请ok");

                    Object msgid = json.get("msgid");
                    if (msgid == null) {
                        System.out.println("msgid is null.");
                    } else {
                        messageList.remove(msgid);
                    }
                    break;
                }
                case 200050: { // 发送好友申请结果ok

                    System.out.println("发送好友申请结果ok");

                    Object msgid = json.get("msgid");
                    if (msgid == null) {
                        System.out.println("msgid is null.");
                    } else {
                        messageList.remove(msgid);
                    }
                    break;
                }
                default:
                    System.err.println("未定义:" + json.toString());
                    break;
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            clientEnd();
        }
    }

    void receive_authorizeHandle(JSONObject json) {
        String login_unid = json.getString("unid");
        System.out.println("login:" + login_unid);

        boolean authorize = Usersx.checkAuthorized(login_unid, json.getString("token"));
        if (!authorize) {
            // 返回授权状态 10001
            JSONObject data = new JSONObject("{\"code\":\"10001\",\"v\":\"1\"}");
            this.writeData(data);
            System.err.println("授权失败：" + login_unid);
            return; // 授权失败
        }

        // 如果已经存在 删除
        IMClient client = IMClientManager.getClient(login_unid);
        if (null != client) {
            System.err.println("删除原来已经存在的用户");
            client.clientEnd();
        }

        // 返回授权状态 10001
        JSONObject data = new JSONObject("{\"code\":\"10001\",\"v\":\"0\"}");
        this.writeData(data);

        synchronized (this) {

            // 添加到用户列表
            this.unid = login_unid;
            this.online = true;

            IMClientManager.addClient(this);

            // 返回离线消息 20003
            JSONObject offlineMsgs = new JSONObject("{'code':'20003'}");
            JSONArray msgList = new JSONArray();
            VirtualFlow.ArrayLinkedList<String> msgs = (VirtualFlow.ArrayLinkedList<String>)IMMessageManager.getOfflineMessage(this.unid);

            if (msgs != null) {

                for (int i = 0; i < msgs.size(); i++) {
                    String msg = msgs.get(i);
                    msgList.put(new JSONObject(msg));
                    if (i > 0 && i % 10 == 0) {
                        offlineMsgs.put("msglist", msgList);
                        this.writeData(offlineMsgs);
                        offlineMsgs.remove("msglist");
                        msgList = new JSONArray();
                    }
                }

                if (msgList.length() > 0) {
                    offlineMsgs.put("msglist", msgList);
                    this.writeData(offlineMsgs);
                }
            }
        }
    }
    void receive_sendMessageHandle(JSONObject json) {
        // {'code':2,'unid':'rencx','msg':'hello'}

        double time = System.currentTimeMillis() / 1000.0;

        JSONObject returnJson = new JSONObject("{'code':10002}");
        returnJson.put("msgid", json.get("msgid"));
        returnJson.put("time", time);
        returnJson.put("v", 0);
        this.writeData(returnJson);

        json.put("time", time);
//        this.sendMessage(json); // 交给IMClientManager处理

    }
}
