package socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Chance on 2017/2/10.
 */
class IMServerThread extends Thread {
    private ServerSocket serverSocket = null;
    private final int port = 12000;
    private IMClientManager clientManager;

    public IMServerThread() {
        clientManager = new IMClientManager();
        try {
            this.serverSocket = new ServerSocket(port);
            System.out.println("*** socket start ***");
        } catch (Exception e) {
            System.out.println("IMSocketThread创建socket服务出错");
            e.printStackTrace();
        }
    }

    public void run() {

        System.out.println(System.currentTimeMillis());

        while (!this.isInterrupted()) {
            try {
                Socket socket = serverSocket.accept();

                if (null != socket && !socket.isClosed()) {
                    clientManager.addSocket(socket);
                }

            } catch (Exception e) {
                System.out.println("socket close");
                break;
            }
        }
    }

    public void closeSocketServer() {
        try {
            if (null != serverSocket && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            System.out.println("*** socket stop ***");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
