package server;

import org.json.simple.JSONObject;

/**
 * Inferfaccia del server WordQuizzle.
 * @author Marina Pierotti
 */

public interface WQServerInterface {

    /**
     * Login per un utente già registrato al servizio.
     * @param nickUtente Username dell'utente che vuole effettuare il login.
     * @param password Password associata all'utente .
     * @return Codice che indica l'avvenuto login o errore in caso la password sia errata.
     */
    public int login(String nickUtente, String password);

    /**
     * Effettua il logout dal servizio.
     * @param nickUtente Username dell'utente che vuole effettuare il logout.
     */
    public void logout(String nickUtente);

    /**
     * Crea una relazione di amicizia fra due utenti registrati al servizio.
     * @param nickUtente Username dell'utente che richiede di creare la relaizone di amicizia.
     * @param nickAmico Username dell'utente che si vuole aggiungere come amico.
     * @return Codice di avvenuta registrazione dell'amicizia o un codice di errore in caso uno dei due username non esista oppure la relazione di amicizia è già esistente.
     */
    public int aggiungiAmico(String nickUtente, String nickAmico);

    /**
     * Restituisce la lista degli amici di un utente registrato al servizio.
     * @param nickUtente Username dell'utente che richiede di vedere la propria lista di amici.
     * @return Oggetto JSON che rappresenta la lista degli amici.
     */
    public JSONObject lista_amici(String nickUtente);

    /**
     * Invia una richiesta di sfida da parte di un utente ad un altro.
     * @param nickUtente Username dell'utente che richiede la sfida.
     * @param nickAmico Username dell'utente che viene sfidato.
     * @return Codice di errore in caso l'utente sfidato non sia nella lista di amici dello sfidante.
     */
    public int sfida(String nickUtente, String nickAmico);

    /**
     * Restituisce il punteggio dell'utente specificato.
     * @param nickUtente Username dell'utente di cui si vuole conoscere il puntegggio.
     * @return Punteggio utente.
     */
    public int mostra_punteggio(String nickUtente);

    /**
     * Restituisce la classifica calcolata in base ai punteggi utente.
     * @param nickUtente Username dell'utente che richiede la lista.
     * @return Oggetto JSON contenente la classifica.
     */
    public JSONObject mostra_classifica(String nickUtente);
}