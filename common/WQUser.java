package common;

import java.util.ArrayList;

/**
 * Classe che rappresenta l'utente di WordQuizzle con tutte le informazioni associate.
 * @author Marina Pierotti
 */

public class WQUser {

    /**
     * Nickname dell'utente
     */
    public String username;

    /**
     * Password dell'utente
     */
    public String password;

    /** 
     * Punteggio dell'utente
     */
    public int points;

    /**
     * Lista amici dell'utente
     */
    public ArrayList<String> friends;

    /**
     * Costruttore semplice per inizializzare username e password.
     * @param nickUtente il nickname dell'utente 
     * @param passwordUtente la password dell'utente
     */
    public WQUser(String nickUtente, String passwordUtente) {
        this.username = nickUtente;
        this.password = passwordUtente;
        this.points = 0;
        this.friends = new ArrayList<String>();
    }

    /**
     * Costruttore avanzato per inserire manualmente anche punteggio e lista amici diversi da quelli di default.
     * @param nickUtente il nickname dell'utente
     * @param passwordUtente la password dell'utente
     * @param punti il punteggio accumulato dall'utente
     * @param listaAmici la lista amici dell'utente
     */
    public WQUser(String nickUtente, String passwordUtente, int punti, ArrayList<String> listaAmici) {
        this.username = nickUtente;
        this.password = passwordUtente;
        this.points = punti;
        this.friends = listaAmici;
    }

    @Override
    public String toString() {
        return this.username + " " + this.password + " " + this.points + " " + this.friends;
    }
    
}
