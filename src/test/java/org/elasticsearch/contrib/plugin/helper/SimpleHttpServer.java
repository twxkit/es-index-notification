package org.elasticsearch.contrib.plugin.helper;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;

import java.util.ArrayList;
import java.util.List;

public class SimpleHttpServer {

    private int port;
    private List<Handler> handlers;

    public SimpleHttpServer(int port) {
        this.port = port;
        this.handlers = new ArrayList<Handler>();
    }

    public SimpleHttpServer() {
        this(8000);
    }

    public SimpleHttpServer registerHandler(Handler handler) {
        this.handlers.add(handler);
        return this;
    }

    public void runWith(Task action) throws Exception {
        Server server = new Server(port);
        try {
            server.setHandlers(handlers.toArray(new Handler[]{}));
            server.start();
            action.execute();
        } finally {
            try {
                server.stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
