package server;

import common.WQUser;
import common.WQInterface;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.util.HashMap;
import java.rmi.server.RemoteServer;

import java.io.*;
import java.lang.reflect.Type;

import java.net.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Implementazione del server WordQuizzle.
 */

public class WQServer extends RemoteServer implements WQInterface, WQServerInterface {

    private static final long serialVersionUID = 1L;

    /**
     * Porta di ascolto del server.
     */
    private int port;

    /**
     * Flag che indica se il server è in esecuzione.
     */
    private boolean isRunning;
    
    /**
     * Mappa degli utenti registrati, come chiave si usa lo username (univoco) dell'utente.
     */
    private HashMap<String, WQUser> userDB; 

    /**
     * Mappa dei client collegati, come chiave si usa lo username dell'utente loggato nel client.
     */
    private HashMap<String, WQManager> onlineUsers;

    /**
     * Costruttore.
     * @param porta Porta di ascolto del server.
     */
    public WQServer(int porta) {

        ServerSocketChannel serverSocket = null; //Socket per le connessioni in entrata
        SocketChannel socket = null; // Socket da smistare ai gestori dei singoli client
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(25); // gestori

        this.port = porta;
        this.isRunning = true;
        this.userDB = new HashMap<String, WQUser>();
        this.onlineUsers = new HashMap<String, WQManager>();

        try {

            // carico i dati
            this.initServer();

            // apro le connessioni
            serverSocket = ServerSocketChannel.open();
            serverSocket.socket().bind(new InetSocketAddress(porta));
            serverSocket.configureBlocking(false);
            System.out.println(">> Il server WordQuizzle è online!");

            // listener per input comandi 
            Scanner scanner = new Scanner(System.in);

            // ciclo di ascolto
            do {
                socket = serverSocket.accept();

                // smistamento ai gestori
                if(socket!=null) {
                    threadPool.execute(new WQManager(this, socket));
                    System.out.println(">> SERVER >> Nuovo gestore in esecuzione.");
                }

            } while (isRunning);

            System.out.println(">> Server in corso di spegnimento.");
            scanner.close();
            threadPool.shutdown();
            serverSocket.close();

        } catch (IOException e) { 
            System.out.println(">> " + e.getMessage());
        }
        finally {
            isRunning = false;
            System.out.println(">> Il server è offline.");
            System.exit(1); 
        }
        

    }

    /**
     * Procedura di registrazione dell'utente.
     * @param nickUtente Username del nuovo utente da registrare.
     * @param password Password del nuovo utente da registrare.
     * @return 0 se la registrazione è andata a buon fine, -1 se l'utente è già registrato, -2 se la password è vuota
     */
    public synchronized int registra_utente(String nickUtente, String password) {

        // verifica che l'utente non sia già registrato
        if(userDB.containsKey(nickUtente)) return -1;

        // verifica che la password non sia vuota
        else if(password.length()==0) return -2;

        // crea nuovo utente e lo inserisce nel database 
        else {
            WQUser nuovoUtente = new WQUser(nickUtente, password);
            userDB.put(nickUtente, nuovoUtente);
            System.out.println(">> Registrazione di " + nickUtente + " avvenuta.");
            this.saveServer();
            return 0;
        }
    }

    /**
     * Procedura di login.
     * @param nickUtente Username dell'utente che vuole fare il login.
     * @param password Password dell'utente che vuole fare il login.
     * @param manager Thread gestore associato al client in cui si tenta il login.
     * @return 0 se il login è stato effettuato con successo, -1 se la password è errata, -2 se l'utente non è registrato, -3 se l'utente è già loggato.
     */
    public synchronized int login(String nickUtente, String password, WQManager manager) {
        // controllo se nickUtente è registrato
        if (userDB.containsKey(nickUtente)) { 
            // controllo che nickUtente non sia già loggato
            if(!onlineUsers.containsKey(nickUtente)) {
                // controllo che la password sia corretta
                if(userDB.get(nickUtente).password.equals(password)) {
                    onlineUsers.put(nickUtente, manager);

                    for (String amico : userDB.keySet() ) {
                        if ( userDB.get(amico).friends.contains(nickUtente) && onlineUsers.containsKey(amico)) {
                            ArrayList<String> amiciOnline = usersOnline(amico);
                            String messaggio = "online "; // formato: online amico1 amico2 ...
                            for (int i=0; i<amiciOnline.size(); i++) {
                                messaggio = messaggio + amiciOnline.get(i) + " ";
                            }
                            onlineUsers.get(amico).send(messaggio);

                        }
                    }
                    return 0; // login eseguito con successo
                }
                
                else { // password errata
                    return -1; 
                }
            }
            else { // nickutente è già loggato
                return -3;
            }
        }
        else { // nickUtente non è registrato
            return -2;
        }
    }

    /**
     * Esegue il logout dell'utente richiesto.
     * @param nickUtente Username dell'utente che chiede di eseguire il logout.
     */
    public void logout(String nickUtente) {
        this.onlineUsers.remove(nickUtente);

        for (String amico : userDB.keySet() ) {
            if ( userDB.get(amico).friends.contains(nickUtente) && onlineUsers.containsKey(amico)) {
                ArrayList<String> amiciOnline = usersOnline(amico);
                String messaggio = "online "; // formato: online amico1 amico2 ...
                for (int i=0; i<amiciOnline.size(); i++) {
                    messaggio = messaggio + amiciOnline.get(i) + " ";
                }
                onlineUsers.get(amico).send(messaggio);

            }
        }
    }

    /**
     * Aggiunge nickAmico alla lista amici di nickUtente.
     * @param nickUtente Username dell'utente che vuole aggiungere un amico.
     * @param nickAmico Username dell'utente che si vuole aggiungere come amico.
     * @return 0 se la creazione dell'amicizia va a buon fine, -1 se uno dei due username non esiste nel database, -2 se l'amicizia esiste già, -3 se i due username sono uguali.
     */
    public int aggiungi_amico(String nickUtente, String nickAmico) {
        // controllo che nickUtente e nickAmico non siano uguali uguali
        if(!nickUtente.equals(nickAmico)) { 
            // uno dei due username non esiste
            if(userDB.get(nickUtente).equals(null)|| userDB.get(nickAmico).equals(null)) {
                System.out.println(">> SERVER >> Tentativo amicizia: uno dei due utenti non esiste.");
                return -1;
            }     
            // la relazione di amicizia è già presente nel database
            else if(userDB.get(nickUtente).friends.contains(nickAmico)) {
                System.out.println(">> SERVER >> Tentativo amicizia: " + nickAmico + " è già tra gli amici di " + nickUtente + ".");
                return -2;
            }
            // creo la relazione di amicizia
            userDB.get(nickUtente).friends.add(nickAmico);
            this.saveServer();
            System.out.println(">> SERVER >> Amicizia: " + nickAmico + " è ora nella lista amici di " + nickUtente + ".");
            return 0;
        } 
        else {
            System.out.println(">> SERVER >> Tentativo amicizia: i due username sono uguali.");
            return -3;
        }

        
    }

    /**
     * Restituisce la lista degli amici di nickUtente.
     * @param nickUtente Username di cui si vuole ottenere la lista degli amici.
     * @return La lista degli amici in formato JSON.
     */
    public String lista_amici(String nickUtente) {
        // nickUtente vuoto
        if(nickUtente==null) {
            System.out.println(">> Lista amici: lo username inserito è vuoto.");
            return null;
        }
        // lista amici vuota
        else if (userDB.get(nickUtente).friends.isEmpty()) {
            System.out.println(">> Lista amici: " + nickUtente + " non ha ancora amici.");
            return null;
        }

        ArrayList<WQUser> list = new ArrayList<>();
        for (String amico : userDB.get(nickUtente).friends) {
            list.add(userDB.get(amico));
        }
        
        System.out.print(">> Amici di " + nickUtente + ": " );
        for(int i=0; i<list.size(); i++) {
            System.out.print(list.get(i).username + ", ");
        }
        System.out.println();

        Gson amici = new Gson();
        return amici.toJson(list.toArray());
    }

    /**
     * Invia una richiesta di sfida da parte di un utente ad un altro.
     * @param nickUtente username dell'utente sfidante
     * @param nickAmico username dell'utente sfidato
     */
    public void sfida(String nickUtente, String nickAmico) {

        if(userDB.containsKey(nickAmico)) { // controllo che nickAmico sia nel database

            if(userDB.get(nickUtente).friends.contains(nickAmico)) { // controllo che nickUtente e nickAmico siano amici

                if(onlineUsers.containsKey(nickAmico)) { // controllo che nickAmico sia online

                    DatagramSocket datagramSocket = onlineUsers.get(nickAmico).challengeRequest(nickUtente, this.port); 

                    if(datagramSocket!=null) { // la richiesta è stata accettata

                        // segnalo inizio sfida
                        System.out.println(">> SERVER >> Inizio sfida fra " + nickUtente + " e " + nickAmico);
                        WQManager managerUtente = onlineUsers.get(nickUtente);
                        managerUtente.send("challengeround 1");
                        WQManager managerAmico = onlineUsers.get(nickAmico);
                        managerAmico.send("challengeround 1");

                        try {

                            // leggo la lista di parole dal dizionario
                            BufferedReader lettoreDizionario = new BufferedReader(new FileReader(new File("dizionario.txt")));
                            ArrayList<String> dizionario = new ArrayList<>();
                            String riga;
                            while ( (riga = lettoreDizionario.readLine()) != null ) {
                                dizionario.add(riga);
                            }
                            lettoreDizionario.close();

                            int K = 10; // numero di parole da tradurre per la sfida K
                            HashMap<String, ArrayList<String>> paroleSfida = new HashMap<>();
                            Collections.shuffle(dizionario); // randomizzo l'ordine delle parole nel dizionario
                            for(int i=0; i<K; i++){
                                String parola = dizionario.get(i);

                                // Searches MyMemory for matches against a segment.
                                // Call example: https://api.mymemory.translated.net/get?q=Hello World!&langpair=en|it
                                // parameter q (mandatory) 
                                //      - The sentence you want to translate. Use UTF-8. 
                                //      - example value: Hello World!
                                // parameter langpair (mandatory) 
                                //      - Source and language pair, separated by the | symbol. Use ISO standard names or RFC3066
                                //      - example value: en|it 

                                URL url = new URL("https://api.mymemory.translated.net/get?q=" + parola + "&langpair=it|en");
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("GET");
                                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                String inputLine;
                                StringBuilder content = new StringBuilder();
                                while ((inputLine = in.readLine()) != null) {
                                    content.append(inputLine);
                                }
                                in.close();

                                // ricevo un oggetto in formato JSON con un campo "matches" contenente le possibili traduzioni della parola
                                JsonElement json = new JsonParser().parse(content.toString());
                                JsonArray listaTraduzioni = json.getAsJsonObject().get("matches").getAsJsonArray();
                                ArrayList<String> traduzioni = new ArrayList<>();
                                for (JsonElement match : listaTraduzioni) {
                                    String translation = match.getAsJsonObject().get("translation").getAsString()
                                            .toLowerCase()
                                            .replaceAll("!", "")
                                            .replaceAll("\\.", "")
                                            .replaceAll("-", ""); 
                                            // pulisco quello che recupero dal json per evitare errori dovuti a maiuscole/minuscole o caratteri speciali
                                    traduzioni.add(translation);
                                }
                                paroleSfida.put(parola, traduzioni);
                                System.out.print(parola + ":");
                                for (String tr : traduzioni) System.out.print(" " + tr);
                                System.out.println();
                            }
                            // avvio il thread arbitro della sfida
                            new Thread(new WQChallengeManager(managerUtente,managerAmico,this,paroleSfida)).start();

                        } catch (IOException e) {
                            System.out.println(">> SERVER >> Dizionario non trovato.");
                        }
                    }

                    else { // sfida rifiutata, utente già occupato o timer scaduto
                        System.out.println(">> SERVER >> SFIDA >> utente occupato o timer scaduto (DatagramSocket null)");
                        WQManager sfidante = onlineUsers.get(nickUtente);
                        sfidante.send("challengeround -2");
                    }
                }
                else { // nickAmico non è online
                    System.out.println(">> SERVER >> SFIDA >> utente offline");
                    WQManager sfidante = onlineUsers.get(nickUtente);
                    sfidante.send("challengeround -2");
                }
            }
            else { // nickUtente e nickAmico non sono amici
                System.out.println(">> SERVER >> SFIDA >> utente non tra gli amici");
                WQManager sfidante = onlineUsers.get(nickUtente);
                sfidante.send("challengeround -2");
            }
        }
        else { // nickAmico non è nel database
            WQManager sfidante = onlineUsers.get(nickUtente);
            sfidante.send("challengeround -2");
        }
    }

    /**
     * Restituisce il punteggio dell'utente specificato.
     * @param nickUtente username di cui si vuole conoscere il punteggio
     */
    public int mostra_punteggio(String nickUtente) {
        System.out.println(">> Punteggio di " + nickUtente + ": " + userDB.get(nickUtente).points);
        return userDB.get(nickUtente).points;
    }

    /**
     * Mostra la classifica in ordine descrescente degli amici di un utente (compreso).
     * @param nickUtente username dell'utente di cui si vuole ottenere la classifica
     */
    public String mostra_classifica(String nickUtente) {
        // nickUtente vuoto
        if(nickUtente==null) {
            System.out.println(">> Classifica amici: lo username inserito è vuoto.");
            return null;
        }
        // lista amici vuota
        else if (userDB.get(nickUtente).friends.isEmpty()) {
            System.out.println(">> Classifica amici: " + nickUtente + " non ha ancora amici.");
            return null;
        }

        ArrayList<WQUser> list = new ArrayList<>();
        for (String amico : userDB.get(nickUtente).friends) {
            list.add(userDB.get(amico));
        }
        list.add(userDB.get(nickUtente));

        // ordino la lista in base ai punti
        list.sort(new Comparator<WQUser>() {
            @Override
            public int compare(WQUser u1, WQUser u2) {
                return Integer.compare(u2.points, u1.points); // ordine crescente
            }
        });
        
        System.out.print(">> Classifica degli amici di " + nickUtente + ": " );
        for(int i=0; i<list.size(); i++) {
            System.out.print(list.get(i).username + "("+ list.get(i).points + "), ");
        }
        System.out.println();

        Gson amici = new Gson();
        return amici.toJson(list.toArray());
    }
    
    /**
     * Conclude la sfida fra user1 e user2 e aggiorna i loro punteggi.
     * @param user1 thread gestore del primo utente
     * @param puntiUtente1 punti guadagnati dal primo utente durante la partita
     * @param user2 thread gestore del secondo utente
     * @param puntiUtente2 punti guadagnati dal primo utente durante la partita
     */
    public void fineSfida(WQManager user1, int puntiUtente1, WQManager user2, int puntiUtente2) {

        // recupero gli oggetti utente dai gestori
        WQUser utente1 = userDB.get(user1.username); 
        WQUser utente2 = userDB.get(user2.username);
        
        // aggiorno i punteggi con i punti guadagnati durante la sfida
        utente1.points += puntiUtente1;
        utente2.points += puntiUtente2;
        
        // assegno il bonus vittoria e invio i messaggi di fine sfida ai client (bonus vittoria Z = +5)
        
        if ( puntiUtente1 > puntiUtente2 ) { // ha vinto user1
            utente1.points += 5;
            user1.send("answer challengewon " + utente1.points);
            user2.send("answer challengelost " + utente2.points);
        }

        else if ( puntiUtente2 > puntiUtente1 ) { // ha vinto user2
            utente2.points += 5;
            user2.send("answer challengewon " + utente2.points);
            user1.send("answer challengelost " + utente1.points);
        }

        else { // pareggio
            user1.send("answer challengetie " + utente1.points);
            user2.send("answer challengetie " + utente2.points);
        }

        // salvo i dati del server
        saveServer();

    }

    /**
     * Inizializza il server con le eventuali informazioni contenute nel file datiServer.json
     * @throws RemoteException
     */
    private void initServer() throws RemoteException {

        // inizializzazione RMI
        WQInterface stub = (WQInterface) UnicastRemoteObject.exportObject(this, port+1);
        LocateRegistry.createRegistry(port+1);
        Registry r = LocateRegistry.getRegistry(port+1);
        r.rebind("WordQuizzle", stub); 
        System.out.println(">> Registrazioni inizializzate sulla porta " + (port+1));
        
        try {
            // lettura dati utente da file
            FileInputStream userDBFileInputStream = new FileInputStream(new File("datiServer.json"));
            String userBaseJson = "";
            byte[] backingArray = new byte[512];
            ByteBuffer buffer;
            int n;
            do {
                n = userDBFileInputStream.read(backingArray);
                buffer = ByteBuffer.wrap(backingArray);
                userBaseJson = userBaseJson.concat(StandardCharsets.UTF_8.decode(buffer).toString());
            } while (n > -1);
            userDBFileInputStream.close();

            // conversione in json e caricamento nel database a runtime del server
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new StringReader(userBaseJson));
            reader.setLenient(true);
            Type type = new TypeToken<HashMap<String, WQUser>>(){}.getType();
            userDB = gson.fromJson(reader, type);
            System.out.println(">> User data (server) = " + userDB.values());

        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    
    }

    /**
     * Salva i dati fileData su un file identificato con filename.
     * @param filename nome del file su cui vengono salvati i dati
     * @param fileData dati da salvare sul file
     * @return 0 se il salvataggio va a buon fine, -1 se non riesce a creare il file, -2 se c'è un errore di I/O
     */
    private int saveToFile(String filename, String fileData) {
        try {
            FileOutputStream output = new FileOutputStream(new File(filename));
            output.write(fileData.getBytes(StandardCharsets.UTF_8));
            return 0;
        } catch (FileNotFoundException e) {
            return -1;
        } catch (IOException e) {
            return -2;
        }
    }

    /**
     * Salva lo stato dei dati degli utenti sul file 'datiServer.json'
     */
    public void saveServer(){
        if(userDB.size()>0){
            Gson gson = new Gson();
            int n = saveToFile("datiServer.json", gson.toJson(userDB));
            if(n == 0) System.out.println(">> Updated user data (server) = " + userDB.values());
            if(n == -1) System.out.println(">> Impossibile creare il file datiServer.");
            if(n == -2) System.out.println(">> IOException durante la scrittura sul file datiServer.");
        }
        else {
            System.out.println(">> Database vuoto o non inizializzato (niente da salvare).");
        }
        
    }
    
    /**
     * Stampa sul terminale del server la lista degli utenti online e restituisce la lista in formato Json.
     * @param user se è non null mostra i suoi amici online
     * @return la lista degli utenti online in formato JSON
     */
    public ArrayList<String> usersOnline(String user) {
        ArrayList<String> connessi = new ArrayList<>();
        if (user == null) { // voglio la lista di tutti gli utenti online
            
            for (String utente : onlineUsers.keySet()) {
                connessi.add(userDB.get(utente).username);
                // System.out.println(" " + userDB.get(utente).username);
            }
            return connessi;
        }
        else {
            for (String utente : onlineUsers.keySet()) {
                if (userDB.get(user).friends.contains(utente)) {
                    connessi.add(userDB.get(utente).username);
                }
            }
            return connessi;
        }
    }

    /**
     * Restituisce le informazioni su un utente registrato.
     * @param name username dell'utente
     * @return l'utente
     */
    public WQUser getUser(String name) {
        if(userDB.containsKey(name)) {
            return userDB.get(name);
        }
        return null;
    }

    public static void main(String[] args) {

        int porta = Integer.parseInt(args[0]);
        WQServer serverprova = new WQServer(porta);

    }

}