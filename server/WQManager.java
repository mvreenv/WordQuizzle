package server;

import java.nio.channels.SocketChannel;

/**
 * Gestore della comunicazione col server della singola istanza client.
 * @author Marina Pierotti
 */
public class WQManager implements Runnable{

    /**
     * Riferimento al server.
     */
    private WQServer server;

    /**
     * Canale di comunicazione.
     */
    private SocketChannel socket;

    /**
     * Indica lo stato del gestore.
     */
    private boolean isOnline;

    /**
     * Userame dell'utente gestito da questo gestore.
     */
    private String username;

    /**
     * Costruttore
     * @param srv riferimento al server
     * @param skt canale di comunicazione
     */
    public WQManager(WQServer srv, SocketChannel skt) {
        this.server = srv;
        this.socket = skt;
        this.isOnline = true;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub

    }
}
