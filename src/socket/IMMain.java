package socket;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by Chance on 2017/2/10.
 * M: 2017.03.06
 */
public class IMMain implements ServletContextListener {
    private IMServerThread server = null;
    private boolean running = false;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        System.out.println("contextInitialized");
        if (!running && null == server) {
            server = new IMServerThread();
            server.start();
            running = true;
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        System.out.println("contextDestroyed");
        if (null != server && !server.isInterrupted()) {
            server.closeSocketServer();
            server.interrupt();
            running = false;
        }
    }
}