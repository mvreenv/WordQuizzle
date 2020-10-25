package server;

/**
 * Inferfaccia del server WordQuizzle che raccoglie i requisiti della specifica del progetto.
 * @author Marina Pierotti
 */

public interface WQServerInterface {

    /**
     * Login per un utente già registrato al servizio.
     * @param nickUtente Username dell'utente che vuole effettuare il login.
     * @param password Password associata all'utente.
     * @param manager Gestore della connessione col client in cui l'utente si logga.
     * @return 0 se il login è stato effettuato con successo, -1 se la password è errata, -2 se l'utente non è registrato, -3 se l'utente è già loggato.
     */
    public int login(String nickUtente, String password, WQManager manager);

    /**
     * Effettua il logout dal servizio.
     * @param nickUtente Username dell'utente che vuole effettuare il logout.
     */
    public void logout(String nickUtente);

    /**
     * Crea una relazione di amicizia fra due utenti registrati al servizio.
     * @param nickUtente Username dell'utente che richiede di creare la relaizone di amicizia.
     * @param nickAmico Username dell'utente che si vuole aggiungere come amico.
     * @return 0 se la registrazione dell'amicizia è avvenuta, -1 se uno dei due username non esiste, -2 se la relazione di amicizia è già esistente, -3 se nickUtente e nickAmico sono lo stesso username.
     */
    public int aggiungi_amico(String nickUtente, String nickAmico);

    /**
     * Restituisce la lista degli amici di un utente registrato al servizio.
     * @param nickUtente Username dell'utente che richiede di vedere la propria lista di amici.
     * @return Oggetto JSON che rappresenta la lista degli amici.
     */
    public String lista_amici(String nickUtente);

    /**
     * Invia una richiesta di sfida da parte di un utente ad un altro.
     * @param nickUtente Username dell'utente che richiede la sfida.
     * @param nickAmico Username dell'utente che viene sfidato.
     */
    public void sfida(String nickUtente, String nickAmico);

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
    public String mostra_classifica(String nickUtente);
}