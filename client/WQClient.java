package client;

/**
 * Classe che implementa le funzionalità del client di WordQuizzle.
 * @author Marina Pierotti
 */

public class WQClient {

    /**
     * Porta per la connessione al server di WordQuizzle
     */
    private int port;

    /**
     * 
     * @param porta La porta a cui si connette per inviare messaggi al server.
     */
    public WQClient(int porta) {
        this.port = porta;


    }

    
    public static void main(final String[] args) {

        //int porta = Integer.parseInt(args[0]);

        int porta = 4000;
        WQClient myClient = new WQClient(porta);
        WQClientGUI myClientGUI = new WQClientGUI();

    }
    


    
}