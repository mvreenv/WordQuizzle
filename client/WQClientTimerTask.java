package client;

import java.util.TimerTask;

/**
 * TimerTask per la durata della sfida.
 */
public class WQClientTimerTask extends TimerTask {

    /**
     * Riferimento al client.
     */
    private WQClient client;

    /**
     * Costruttore.
     * @param client Riferimento al client.
     */
    public WQClientTimerTask(WQClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        client.receive("challengeround -3");
        // client.send("challengeanswer -1");
    }
    
}
