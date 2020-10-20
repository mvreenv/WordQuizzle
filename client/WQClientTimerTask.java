package client;

import java.util.TimerTask;

/**
 * TimerTask per il timeout di invio di una traduzione durante la sfida. (Invia "-1" al server.)
 */

public class WQClientTimerTask extends TimerTask {

    /**
     * Riferimento al client.
     */
    private WQClient client;

    public WQClientTimerTask(WQClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        client.send("-1");
    }
    
}
