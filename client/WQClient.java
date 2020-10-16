package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import common.WQInterface;

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
     * Costruttore del client.
     * @param porta La porta a cui si connette per inviare messaggi al server.
     */
    public WQClient(int porta) {
        this.port = porta;

        WQClientLink.client = this;

    }

    /**
     * Registrazione al servizio di WordQuizzle tramite RMI
     * @param nickUtente username dell'utente che si vuole registrare
     * @param password password dell'utente che si vuole registrare
     * @return 0 se la registrazione ha successo, -1 se esiste già un utente con lo stesso nickUtente, -2 se la password è vuota, -3 se ci sono errori di comunicazione
     */
    public int registra_utente(String nickUtente, String password) {

        int n;
        WQInterface rmi;

        try {
            Registry reg = LocateRegistry.getRegistry(port+1); // collegamento al server tramite registry
            rmi = (WQInterface) reg.lookup("WordQuizzle"); // lookup del servizio
            n = rmi.registra_utente(nickUtente, password); // tentativo di registrazione al servizio
            return n; // 0 se la registrazione ha successo, -1 se esiste già un utente con lo stesso nickUtente, -2 se la password è vuota
        } catch (RemoteException | NotBoundException e) {
            System.out.println(e.getMessage());
        }

        return -3; // se ci sono errori di comunicazione
    }
    
    public static void main(final String[] args) {

        int porta = Integer.parseInt(args[0]);

        WQClient myClient = new WQClient(porta);
        WQClientGUI myClientGUI = new WQClientGUI();

    }
        
}