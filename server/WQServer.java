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
 * @author Marina Pierotti
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
     * Mappa degli utenti collegati, come chiave si usa lo username dell'utente. FIX CI VA WQManager
     */
    private HashMap<String, WQUser> onlineUsers;

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
        this.onlineUsers = new HashMap<String, WQUser>();

        try {

            // carico i dati
            this.initServer();

            // apro le connessioni
            serverSocket = ServerSocketChannel.open();
            serverSocket.socket().bind(new InetSocketAddress(porta));
            serverSocket.configureBlocking(false);
            System.out.println(">> Il server WordQuizzle è online! (digita \"help\" per vedere la lista dei comandi)");

            // listener per input comandi 
            Scanner scanner = new Scanner(System.in);

            // ciclo di ascolto
            while (isRunning) {
                socket = serverSocket.accept();

                // smistamento ai gestori
                if(socket!=null) {
                    threadPool.execute(new WQManager(this, socket));
                }

                // ascolto dei comandi da terminale sul server
                else if(scanner.hasNext()) {
                    String command = scanner.nextLine();
                    String[] words = command.split(" ");

                    switch (words[0]) {
                        case "help" :
                            System.out.println(">> LISTA DEI COMANDI DEL SERVER WORDQUIZZLE ");
                            System.out.println(">> aggiungi_amico user1 user2\taggiunge user2 alla lista amici di user1");
                            System.out.println(">> amici user\t\t\tmostra la lista amici di user");
                            System.out.println(">> classifica user\t\tmostra la classifica degli amici di user (user incluso)");
                            System.out.println(">> login user password\t\teffettua il login di user con password");
                            System.out.println(">> logout user\t\t\teffettua il logout di user");
                            System.out.println(">> online \t\t\tmostra la lista degli utenti online");
                            System.out.println(">> punteggio user\t\tmostra il punteggio di user");
                            System.out.println(">>");
                            break;
                        case "aggiungi_amico" :
                            if(words.length==3) this.aggiungiAmico(words[1], words[2]);
                            else System.out.println(">> Input errato, riprova. (aggiungi_amico user1 user2)");
                            break;
                        case "amici" :
                            if(words.length==2) this.lista_amici(words[1]);
                            else System.out.println(">> Input errato, riprova. (amici user)");
                            break;
                        case "classifica" :
                            if(words.length==2) this.mostra_classifica(words[1]);
                            else System.out.println(">> Input errato, riprova. (classifica user)");
                            break;
                        case "login" :
                            if (words.length==3) this.login(words[1], words[2], new WQManager(this, socket)); // CHECK
                            else System.out.println(">> Input errato, riprova. (login user password)");
                            break;
                        case "logout" :
                            if (words.length==2) this.logout(words[1]);
                            else System.out.println(">> Input errato, riprova. (logout user)");
                            break;
                        case "online" :
                            if(this.onlineUsers.size()>0) this.usersOnline();
                            else System.out.println(">> Utenti online: nessun utente online.");
                            break;
                        case "punteggio" :
                            if(words.length==2) this.mostra_punteggio(words[1]);
                            else System.out.println(">> Input errato, riprova. (punteggio user)");
                            break;
                        case "switch_off" :
                            isRunning = false;
                            break;
                        default :
                            System.out.println(">> Input errato, (digita \"help\" per vedere la lista dei comandi)");
                            break;
                    }
                }
                else Thread.sleep(500);
            } 

            System.out.println(">> Server in corso di spegnimento.");
            scanner.close();
            threadPool.shutdown();
            serverSocket.close();

        } catch (IOException | InterruptedException e) {
            System.out.println(">> " + e.getMessage());
        }
        finally {
            isRunning = false;
            System.out.println(">> Il server è offline.");
            System.exit(1); //CHECK
        }
        

    }

    /**
     * Procedura di registrazione dell'utente.
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

    // da implementare con connessione TCP
    public synchronized int login(String nickUtente, String password, WQManager manager) {

        // verifica se l'utente è già loggato
        if(onlineUsers.get(nickUtente)!=null) {
            System.out.println(">> Login: l'utente " + nickUtente + " ha già effettuato il login.");
            return -3;
        }

        // verifica se l'utente è registrato
        else if(userDB.get(nickUtente)==null) {
            System.out.println(">> Login: l'utente " + nickUtente + " non è registrato.");
            return -2;
        }

        // verifica la password
        else if(!password.equals(userDB.get(nickUtente).password)) {
            System.out.println(">> Login: passowrd errata per l'utente " + nickUtente + ".");
            return -1;
        }

        // FIX NELLA LISTA ONLINE USER CI VA <String, WQManager>
        // inserimento dell'utente nella lista degli utenti online
        onlineUsers.put(nickUtente, userDB.get(nickUtente));
        System.out.println(">> Login: " + nickUtente + " login effettuato con successo.");
        return 0;
    }

    public void logout(String nickUtente) {
        this.onlineUsers.remove(nickUtente);
        System.out.println(">> Logout: " + nickUtente + " logout effettuato con successo.");
    }

    /**
     * Aggiunge nickAmico alla lista amici di nickUtente.
     */
    public int aggiungiAmico(String nickUtente, String nickAmico) {
        // uno dei due username non esiste
        if(userDB.get(nickUtente)==null || userDB.get(nickAmico)==null) {
            System.out.println(">> Tentativo amicizia: uno dei due utenti non esiste.");
            return -1;
        }     // la relazione di amicizia è già presente nel database
        else if(userDB.get(nickUtente).friends.contains(nickAmico)) {
            System.out.println(">> Tentativo amicizia: " + nickAmico + " è già tra gli amici di " + nickUtente + ".");
            return -2;
        }
        // nickUtente e nickAmico sono uguali
        else if(nickUtente.equals(nickAmico)) {
            System.out.println(">> Tentativo amicizia: i due username sono uguali.");
            return -3;
        }
        // creo la relazione di amicizia
        userDB.get(nickUtente).friends.add(nickAmico);
        this.saveServer();
        System.out.println(">> Amicizia: " + nickAmico + " è ora nella lista amici di " + nickUtente + ".");
        return 0;
    }

    /**
     * Restituisce la lista degli amici di nickUtente.
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

    public int sfida(String nickUtente, String nickAmico) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int mostra_punteggio(String nickUtente) {
        System.out.println(">> Punteggio di " + nickUtente + ": " + userDB.get(nickUtente).points);
        return userDB.get(nickUtente).points;
    }

    /**
     * Mostra la classifica in ordine descrescente degli amici di nickUtente (compreso).
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

            // conversione in json e upload nel database a runtime del server
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
     * Salva lo stato dei dati degli utenti sul file datiServer.json
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
     * Stampa sul terminale del server la lista degli utenti online.
     * @return la lista degli utenti online in formato JSON
     */
    public String usersOnline() {
        ArrayList<WQUser> connessi = new ArrayList<>();
        for (String utente : onlineUsers.keySet()) {
            connessi.add(userDB.get(utente));
        }
        
        System.out.print(">> Utenti online: ");
        for(int i=0; i<connessi.size(); i++) {
            System.out.print(connessi.get(i).username + ", ");
        }
        System.out.println();

        Gson amici = new Gson();
        return amici.toJson(connessi.toArray());
    }

    /**
     * Restituisce le informazioni su un utente registrato.
     * @param name username dell'utente
     * @return le informazioni sull'utente
     */
    public String getUser(String name) {
        if(userDB.containsKey(name)) {
            Gson json = new Gson();
            return json.toJson(this.userDB.get(name));
        }
        return null;
    }

    public static void main(String[] args) {

        int porta = Integer.parseInt(args[0]);
        WQServer serverprova = new WQServer(porta);

    }

}