package common;

import java.util.ArrayList;

/**
 * Classe che rappresenta l'utente di WordQuizzle con tutte le informazioni associate.
 * @author Marina Pierotti
 */

public class WQUser {

    /**
     * Nickname dell'utente.
     */
    public String username;

    /**
     * Password dell'utente.
     */
    public String password;

    /** 
     * Punteggio dell'utente.
     */
    public int points;

    /**
     * Lista amici dell'utente.
     */
    public ArrayList<String> friends;

    /**
     * Costruttore semplice per inizializzare username e password.
     * @param nickUtente Il nickname dell'utente.
     * @param passwordUtente La password dell'utente.
     */
    public WQUser(String nickUtente, String passwordUtente) {
        this.username = nickUtente;
        this.password = passwordUtente;
        this.points = 0;
        this.friends = new ArrayList<String>();
    }

    /**
     * Costruttore per inserire manualmente anche punteggio e lista amici diversi da quelli di default.
     * @param nickUtente Il nickname dell'utente.
     * @param passwordUtente La password dell'utente.
     * @param punti Il punteggio accumulato dall'utente.
     * @param listaAmici La lista amici dell'utente.
     */
    public WQUser(String nickUtente, String passwordUtente, int punti, ArrayList<String> listaAmici) {
        this.username = nickUtente;
        this.password = passwordUtente;
        this.points = punti;
        this.friends = listaAmici;
    }

    /**
     * Fornisce una descrizione di una riga contenente tutte le informazioni dell'utente.
     * @return Stringa con le informazioni dell'utente in formato "Username: nome - Password: pw - Punti: x - Amici: amico1, amico2,"
     */
    public String description() {
        String d = "Username: " + this.username + " - Password: " + this.password + " - Punti: " + this.points + " - Amici: ";
        if(!this.friends.isEmpty() || this.friends!=null){
            for(String friend : this.friends) {
                d = d + friend + ", ";
            }
        }
        return d;
    }

    @Override
    public String toString() {
        return this.username + " " + this.password + " " + this.points + " " + this.friends;
    }

}
