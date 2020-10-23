package server;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Thread gestore della sfida fra due utenti.
 */
public class WQChallengeManager implements Runnable {

    /**
     * Riferimento al gestore del primo utente.
     */
    private WQManager user1;

    /**
     * Riferimento al gestore del secondo utente.
     */
    private WQManager user2;

    /**
     * Riferimento al server.
     */
    private WQServer server;

    /**
     * Lista delle parole con le loro traduzioni da inviare ai due utenti.
     */
    private HashMap<String, ArrayList<String>> challengeWords;

    public WQChallengeManager(WQManager u1, WQManager u2, WQServer s, HashMap<String, ArrayList<String>> w) {
        this.user1 = u1;
        this.user2 = u2;
        this.server = s;
        this.challengeWords = w;
    }

    @Override
    public void run() {

        // invio ai due sfidanti le parole da tradurre
        user1.words = new HashMap<>(challengeWords);
        user2.words = new HashMap<>(challengeWords);

        try { // aspetto tre secondi per dare tempo ai due utenti di far partire la sfida
            Thread.sleep(3000);
        } catch (InterruptedException e) {}

        do {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}
        } while (user1.isPlaying);

        do {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}
        } while (user2.isPlaying);

        server.fineSfida(user1, user1.matchPoints, user2, user2.matchPoints);
    }
    
}
