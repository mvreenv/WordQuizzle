package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interfaccia che definisce il comportamento del servizio RMI per la registrazione degli utenti a WordQuizzle.
 * @author Marina Pierotti
 */

public interface WQInterface extends Remote {
    /**
     * Registra un nuovo utente al servizio.
     * @param nickUtente Username dell'utente da registrare
     * @param password Password associata allo username dell'utente da registrare
     * @return 0 indica l'avvenuta registrazione, -1 se lo username è già presente nel database, -2 se la password è vuota
     */
    public int registra_utente(String nickUtente, String password) throws RemoteException;
}