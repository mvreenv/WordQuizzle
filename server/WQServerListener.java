package server;

import java.nio.channels.SocketChannel;

public class WQServerListener implements Runnable{

    /**
     * Riferimento al server.
     */
    private WQServer server;

    /**
     * Canale di comunicazione.
     */
    private SocketChannel socket;
    
    public WQServerListener(WQServer srv, SocketChannel skt) {
        this.server = srv;
        this.socket = skt;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub

    }
}
