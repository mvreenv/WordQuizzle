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

import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.json.simple.JSONObject;


/**
 * Implementazione del server WordQuizzle.
 * @author Marina Pierotti
 */

public class WQServer extends RemoteServer implements WQInterface, WQServerInterface {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Mappa degli utenti registrati, come chiave si usa lo username (univoco) dell'utente.
     */
    private HashMap<String, WQUser> userDB; 

    /**
     * Porta di ascolto del server.
     */
    private int port;

    /**
     * Costruttore.
     * @param porta Porta di ascolto del server.
     */
    public WQServer(int porta) {

        this.port = porta;
        this.userDB = new HashMap<String, WQUser>();

        try {
            this.init();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        

    }

    private void init() throws RemoteException {

        // // inizializzazione RMI
        // WQInterface stub = (WQInterface) UnicastRemoteObject.exportObject(this, port+1);
        // LocateRegistry.createRegistry(port+1);
        // Registry r = LocateRegistry.getRegistry(port+1);
        // r.rebind("WordQuizzle", stub); 
        // System.out.println("Registrazioni inizializzate sulla porta " + (port+1));
        
        try {
            // lettura dati utente da file
            FileInputStream userDBFileInputStream = new FileInputStream(new File("datiServer"));
            String userBaseJson = "";
            byte[] buff = new byte[512];
            ByteBuffer bBuff;
            int n;
            do {
                n = userDBFileInputStream.read(buff);
                bBuff = ByteBuffer.wrap(buff);
                userBaseJson = userBaseJson.concat(StandardCharsets.UTF_8.decode(bBuff).toString());
            } while (n > -1);
            userDBFileInputStream.close();

            // conversione in json
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new StringReader(userBaseJson));
            reader.setLenient(true);
            Type type = new TypeToken<HashMap<String, WQUser>>(){}.getType();

            // upload nel database del server
            userDB = gson.fromJson(reader, type);
            System.out.println("User data (server) = " + userDB.values());

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
     * Salva lo stato dei dati degli utenti su un file.
     */
    public void saveServer(){
        if(userDB.size()>0){
            Gson gson = new Gson();
            int n = saveToFile("datiServer", gson.toJson(userDB));
            if(n == 0) System.out.println("Updated user data (server) = " + userDB.values());
            if(n == -1) System.out.println("Impossibile creare il file datiServer.");
            if(n == -2) System.out.println("IOException durante la scrittura sul file datiServer.");
        }
        else {
            System.out.println("Database vuoto o non inizializzato (niente da salvare).");
        }
        
    }

    public synchronized int registra_utente(String nickUtente, String password) {

        // verifica che l'utente non sia già registrato
        if(userDB.containsKey(nickUtente)) return -1;

        // verifica che la password non sia vuota
        else if(password=="") return -2;

        // crea nuovo utente e lo inserisce nel database 
        else {
            WQUser nuovoUtente = new WQUser(nickUtente, password);
            userDB.put(nickUtente, nuovoUtente);
            this.saveServer();
            System.out.println("Registrazione di " + nickUtente + " avvenuta.");
            return 0;
        }
    }

    // da implementare con connessione TCP
    public synchronized int login(String nickUtente, String password) {
        // TODO Auto-generated method stub
        return 0;
    }

    public void logout(String nickUtente) {
        // TODO Auto-generated method stub

    }

    public int aggiungiAmico(String nickUtente, String nickAmico) {
        // TODO Auto-generated method stub
        return 0;
    }

    public JSONObject lista_amici(String nickUtente) {
        // TODO Auto-generated method stub
        return null;
    }

    public int sfida(String nickUtente, String nickAmico) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int mostra_punteggio(String nickUtente) {
        // TODO Auto-generated method stub
        return 0;
    }

    public JSONObject mostra_classifica(String nickUtente) {
        // TODO Auto-generated method stub
        

        return null;
    }

    public static void main(String[] args) {
        WQServer serverprova = new WQServer(4000);
        WQUser userprova1 = new WQUser("marina", "ciao", 25, null);
        WQUser userprova2 = new WQUser("vale", "ciao2", 12, null);
        serverprova.userDB.put(userprova1.username, userprova1);
        serverprova.userDB.put(userprova2.username, userprova2);
        serverprova.saveServer();
    }

}