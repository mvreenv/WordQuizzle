package client;

import java.util.TimerTask;

/**
 * TimerTask per indicare al Client che la sfida è terminata se il timer scade.
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
    }
    
}
