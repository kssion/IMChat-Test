package socket;

import com.sun.javafx.scene.control.skin.VirtualFlow.*;
import json.JSONArray;
import json.JSONObject;
import web.user.Usersx;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.concurrent.*;

public class IMClientManager {

    private static final byte[] header_symbol = new byte[]{0x05, 0x02};
	private static final byte[] blank_bytes = new byte[]{0, 0, 0, 0};
	private static final byte[] keep_alive_bytes = new byte[]{0x7b, 0x7d};

	private static final ConcurrentHashMap<String, IMClient> clientsMap = new ConcurrentHashMap<>();

	IMClientManager() { }

	public void addSocket(Socket s) {
		IMClient client = new IMClient(s);
		new Thread(client).start();
	}


	public static IMClient getClient(String unid) {
		if (null != unid) {
			return clientsMap.get(unid);
		}
		return null;
	}
	private static void addClient(IMClient client) {
		clientsMap.put(client.unid, client);
	}
	private static void removeClient(String unid) {
		if (null != unid && clientsMap.containsKey(unid)) {
			clientsMap.remove(unid);
		}
	}


	/**
	 * Created by Chance on 2017/2/10.
	 */
	public final class IMClient implements Runnable {

		private static final int kReceiveTimeDelay = (10 + 1) * 1000;

		long lastReceiveTime;
		boolean isOver;
		boolean running = true;
		boolean online = false;
		public boolean isOnline() {
			return this.online;
		}

		private Socket socket;
		private InputStream inputStream = null;
		private DataOutputStream dataOutputStream = null;
	    private String unid = null;
		private LinkedHashMap<Object, JSONObject> msgList = null;

		// 数据接收处理
		private int length = 0;
		private ByteArrayOutputStream dataPartStorage = new ByteArrayOutputStream();

		private IMClient() {
			isOver = false;
			lastReceiveTime = System.currentTimeMillis();
			msgList = new LinkedHashMap<>();
		}

		IMClient(Socket s) {
			this();
	        this.socket = s;
	    }

	    @Override
		public String toString() {
			return String.format("<IMClient unid = %s>", this.unid);
		}

		/**
		 * 客户端是否有效
		 */
		private boolean isValidClient() {
			try {
				dataOutputStream.write(header_symbol);
				dataOutputStream.write(blank_bytes);
				dataOutputStream.flush();
			} catch (IOException e) {
				System.err.println(this + " offline.");
				return false;
			}
			return true;
		}

//		/**
//		 * 写入数据
//		 */
//		private void write(byte[] bytes) {
//
//			synchronized (this) {
//
//				try {
//					int len = bytes.length;
//					dataOutputStream.write(header_symbol);
//					dataOutputStream.write((len >>> 24) & 0xFF);
//					dataOutputStream.write((len >>> 16) & 0xFF);
//					dataOutputStream.write((len >>> 8) & 0xFF);
//					dataOutputStream.write((len) & 0xFF);
//					dataOutputStream.write(bytes);
//					dataOutputStream.flush();
//					lastReceiveTime = System.currentTimeMillis();
//				} catch (IOException ex) {
//					System.out.println("write: -> 客户端已经断开");
//				}
//			}
//        }
//		public void write(String string) {
//            this.write(string.getBytes());
//        }
//		public void writeLasting(String string) {
//			if (this.isValidClient()) {
//				this.write(string);
//			} else {
//				System.err.println("该用户已断开:" + unid);
//				IMMessageManager.addOfflineMessage(this.unid, string);
//			}
//		}
//
//		/**
//		 * 发送数据（给自己）
//		 * @param json 消息对象
//		 */
//		public void writeMessage(String msgid, JSONObject json) {
//			msgList.put(msgid, json);
//			this.writeLasting(json.toString());
//		}
//
//		/**
//		 * 发送数据（给指定的好友）
//		 * @param msg 消息json {t:20002, msgid:1000000, unid:xxx, msg:text}
//		 */
//		public void sendTo(String unid, String msg) {
//
//			if (Usersx.existUser(unid)) {
//				IMClient client = getClient(unid);
//				if (null != client) {
//					client.writeLasting(msg);
//				} else {
//					System.err.println("该用户不在线:" + unid);
//					IMMessageManager.addOfflineMessage(unid, msg);
//				}
//			}
//		}
//		public void sendTo(String unid, JSONObject msg) {
//
//			if (Usersx.existUser(unid)) {
//				IMClient client = getClient(unid);
//				if (null != client) {
//					client.writeMessage(msg.getString("msgid"), msg);
//				} else {
//					System.err.println("该用户不在线:" + unid);
//					IMMessageManager.addOfflineMessage(unid, msg.toString());
//				}
//			}
//		}
//
//		/**
//		 * 发送消息（给好友）
//		 * @param msgJson 消息json {t:20002, msgid:1000000, unid:xxx, msg:text, time:1460000000}
//		 */
//		public void sendMessage(JSONObject msgJson) {
//
//			// 消息接收者
//			String unid = msgJson.getString("unid");
//
//			IMClient client = getClient(unid);
//
//			// {'code':'20002','unid':'" + unid + "','msg':'" + msg + "'}
//			// 消息发送者替换自己
//			msgJson.put("unid", this.unid);
//			msgJson.put("code", "20002");
//
//			if (null != client) {
//				client.writeMessage(msgJson.getString("msgid"), msgJson);
//			} else {
//				System.err.println("该用户不在线:" + unid);
//				IMMessageManager.addOfflineMessage(unid, msgJson.toString());
//			}
//		}
//
//		/**
//		 * 保存未发送的消息
//		 */
//		void saveMessageToOffline() {
//			System.err.println("saveMessageToOffline");
//			if (msgList.size() > 0) {
//
//				Collection<JSONObject>msgs = msgList.values();
//				for (JSONObject json : msgs) {
//					IMMessageManager.addOfflineMessage(this.unid, json.toString());
//				}
//				msgList.clear();
//				System.out.println("消息保存至离线:" + this);
//			}
//		}
//
//		/**
//		 *	断开当前用户
//		 */
//        private void clientEnd() {
//			System.err.println("clientEnd");
//            if (!isOver) {
//				System.err.println("Over.");
//                try {
//					running = false;
//                    inputStream.close();
//                    dataOutputStream.close();
//                    dataPartStorage.close();
//
//                    if (null != socket && !socket.isClosed()) {
//                        socket.close();
//                    }
//                } catch (IOException e) {
//                    System.err.println(e.getMessage());
//                } finally {
//                    removeClient(unid);
//					System.err.println("Remove Client.");
//                    unid = null;
//                    isOver = true;
//                }
//            }
//        }

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
		public void writeMessage(Object msgid, JSONObject json) {
			try {
				msgList.put(msgid, json);
				this.write(json.toString());
			} catch (IOException e) {
				e.printStackTrace();
				this.running = false;
			}
		}

		/**
		 * 发送消息（给好友）
		 * @param msgJson 消息json {t:20002, msgid:1000000, unid:xxx, msg:text, time:1460000000}
		 */
		public void sendMessage(JSONObject msgJson) {

			// 消息接收者
			String unid = msgJson.getString("unid");

			IMClient client = getClient(unid);

			// {'code':'20002','unid':'" + unid + "','msg':'" + msg + "'}
			// 消息发送者替换自己
			msgJson.put("unid", this.unid);
			msgJson.put("code", "20002");

			if (null != client && client.isOnline()) {
				client.writeMessage(msgJson.get("msgid"), msgJson);
			} else {
				System.err.println("该用户不在线:" + unid);
				IMMessageManager.addOfflineMessage(unid, msgJson.toString());
			}
		}

		/**
		 * 保存未发送的消息
		 */
		void saveMessageToOffline() {
			System.err.println("saveMessageToOffline, count:" + msgList.size());
			if (msgList.size() > 0) {
				Collection<JSONObject> msgs = msgList.values();
				for (JSONObject json : msgs) {
					IMMessageManager.addOfflineMessage(this.unid, json.toString());
				}
				msgList.clear();
				System.out.println("消息保存至离线:" + this);
			}
		}

		/**
		 *	断开当前用户
		 */
		private void clientEnd() {
			try {
				this.online = false;
				this.running = false;
				IMClientManager.removeClient(this.unid);
				this.saveMessageToOffline();

				this.inputStream.close();
				this.dataOutputStream.close();
				this.dataPartStorage.close();

				if (null != this.socket && !this.socket.isClosed()) {
					this.socket.close();
					this.socket = null;
				}
			} catch (IOException e) {
				System.err.println(e.getMessage());
			} finally {

				System.err.println("Remove Client.");
				this.unid = null;
			}
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
						throw new Exception("通信超时");

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
				this.clientEnd();

	        } finally {

				System.out.println("client out:" + socket.getRemoteSocketAddress());
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

//						System.out.println((unid==null ? "" : unid + ":") + new String(buffer, 0, len));

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
							msgList.remove(msgid);
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
							msgList.remove(msgid);
						}
						break;
					}
					case 200050: { // 发送好友申请结果ok

						System.out.println("发送好友申请结果ok");

						Object msgid = json.get("msgid");
						if (msgid == null) {
							System.out.println("msgid is null.");
						} else {
							msgList.remove(msgid);
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
                this.clientEnd();
            }
        }

        void receive_authorizeHandle(JSONObject json) {
			String login_unid = json.getString("unid");
			System.out.println("request login:" + login_unid);

			boolean authorize = Usersx.checkAuthorized(login_unid, json.getString("token"));
			if (!authorize) {
				// 返回授权状态 10001
				JSONObject data = new JSONObject("{\"code\":\"10001\",\"status\":\"1\"}");
				this.writeData(data);
				System.err.println("授权失败：" + login_unid);
				return; // 授权失败
			}

			// 同步添加或替换
			{
				synchronized (clientsMap) {
					// 如果已经存在 删除
					IMClient client = IMClientManager.getClient(login_unid);
					if (null != client) {
						System.err.println("已经登录的客户端将强制离线");
						client.clientEnd();
					}

					// 返回授权状态 10001
					JSONObject data = new JSONObject("{\"code\":\"10001\",\"status\":\"0\"}");
					this.writeData(data);

					try {
						Thread.sleep(200);
						System.out.println("延迟0.2秒登录");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// 添加到用户列表
					this.unid = login_unid;
					this.online = true;

					IMClientManager.addClient(this);

					// 返回离线消息 20003
					JSONObject offlineMsgs = new JSONObject("{'code':'20003'}");
					JSONArray msgList = new JSONArray();
					ArrayLinkedList<String> msgs = (ArrayLinkedList<String>)IMMessageManager.getOfflineMessage(this.unid);

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
		}
		void receive_sendMessageHandle(JSONObject json) {
			// {'code':2,'unid':'rencx','msg':'hello'}

			double time = System.currentTimeMillis() / 1000.0;

			JSONObject returnJson = new JSONObject("{'code':10002}");
			returnJson.put("msgid", json.get("msgid"));
			returnJson.put("time", time);
			returnJson.put("status", 0);
			this.writeData(returnJson);

			json.put("time", time);
			this.sendMessage(json);
		}


		// 重新实现 读取方式：分别读取头部和内容 header：6、body：len
//		private void readData(int len) {
//
//			int readLength = 4096;
//			byte[] buffer = new byte[readLength];
//
//			try {
//				InputStream inputStream = socket.getInputStream();
//				while ((len = inputStream.read(buffer)) > 0) {
//					if (len > 0) {
//						dataPartStorage.write(buffer, 0, len);
//						byte[] bytes = dataPartStorage.toByteArray();
//						dataPartStorage.reset();
//
//						System.err.println((unid==null ? "" : unid + ":") + new String(buffer, 0, len));
//
//						this.dataLoop(bytes);
//					}
//				}
//			} catch (IOException e) {
//				System.err.println(e.getMessage());
//			}
//		}

	}
}
