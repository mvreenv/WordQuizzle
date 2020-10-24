package client;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Timer;

import common.WQInterface;
import common.WQUser;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

/**
 * Classe che implementa le funzionalità del client di WordQuizzle.
 */

public class WQClient {

    /**
     * Porta per la connessione al server di WordQuizzle
     */
    private int port;

    /**
     * Socket per la comunicazione.
     */
    private SocketChannel socket;

    /**
     * Chiave per la comunicazione.
     */
    private SelectionKey key;
    
    /**
     * Timer per il tempo di invio di una traduzione durante la sfida.
     */
    private Timer translationTimer;

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

    /**
     * Esegue il login dell'utente specificato.
     * @param username lo username
     * @param password la password
     * @return 0 se il login è avvenuto con successo, -1 se la password è errata, -2 se l'utente non è registrato, -3 se l'utente è già loggato, -4 errore generico
     */
    public int login(String username, String password) {

        try { 
            // connessione TCP al server
            socket = SocketChannel.open();
            System.out.println(">> CLIENT >> Connessione in corso sulla porta " + (port));
            socket.connect(new InetSocketAddress("127.0.0.1", port)); // indirizzo di loopback perché il server gira sulla stessa macchina)
            socket.configureBlocking(false);
            Selector selector = Selector.open();
            key = socket.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);

            // prova ad eseguire il login
            String string = "login " + username + " " + password;
            ByteBuffer buf = ByteBuffer.wrap(string.getBytes());
            int n;
            do {
                n = ((SocketChannel)key.channel()).write(buf);
            } while (n>0);

            // attende di ricevere una risposta
            buf = ByteBuffer.allocate(1024);
            do {
                n = ((SocketChannel)key.channel()).read(buf);
            } while (n==0); 
            do { 
                n = ((SocketChannel)key.channel()).read(buf);
            } while (n>0);
            buf.flip();
            String received = StandardCharsets.UTF_8.decode(buf).toString();
            String command = received.split(" ")[0];
            // legge la risposta
            if (command.equals("answer")) {

                if (received.split(" ")[1].equals("LOGINOK")) {
                    // ottengo i dati dell'utente
                    WQUser myUser = null;
                    Gson json = new Gson();
                    buf = ByteBuffer.allocate(256);
                    do {
                        buf.clear();
                        n = ((SocketChannel)key.channel()).read(buf);
                    } while (n==0); 
                    do { 
                        n = ((SocketChannel)key.channel()).read(buf);
                    } while (n>0);
                    buf.flip();
                    received = StandardCharsets.UTF_8.decode(buf).toString();
                    myUser = json.fromJson(received, WQUser.class);
                    if (myUser!=null) {
                        WQClientLink.gui.setUser(myUser.username, myUser.points);
                        System.out.println(">> CLIENT >> " + myUser.username + " loggato.");
                    }

                    // avvio il thread listener TCP del client
                    new Thread(new WQClientReceiver(socket, key)).start();

                    // avvio il listener UDP 
                    try {
                        DatagramSocket datagramSocket = new DatagramSocket();
                        new Thread(new WQClientDatagramReceiver(datagramSocket)).start();
                        string = "challengeport " + datagramSocket.getLocalPort();
                        buf = ByteBuffer.wrap(string.getBytes(StandardCharsets.UTF_8));
                        do { 
                            n = ((SocketChannel) key.channel()).write(buf); 
                        } while (n > 0);
                        System.out.println(">> CLIENT >> Receiver UDP su porta " + datagramSocket.getLocalPort());
                    } catch (Exception e) {
                        string = "challengeport -1";
                        buf = ByteBuffer.wrap(string.getBytes(StandardCharsets.UTF_8));
                        do { 
                            n = ((SocketChannel) key.channel()).write(buf); 
                        } while (n > 0);
                        System.out.println(">> CLIENT >> Errore avvio Listener UDP " + e.getMessage());
                    }

                    return 0; // login avvenuto con successo

                }
                else if (received.split(" ")[1].equals("LOGINERR1")) {
                    return -1; // password errata
                }
                else if (received.split(" ")[1].equals("LOGINERR2")) {
                    return -2; // utente non registrato
                }
                else if (received.split(" ")[1].equals("LOGINERR3")) {
                    return -3; // utente già loggato
                }
                else // errore generico
                    return -4;
            }
        } catch (IOException e) {
            System.out.println(">> CLIENT >> Impossibile connettersi al server.");
            // System.out.println(e.getMessage());
            // e.printStackTrace();
        }
        return -4; // errore generico
    }

    public void logout(String username) {
        WQClientLink.gui.setUser(null, 0);
        try {
            this.socket.close();
        } catch (IOException e) {}
        this.socket = null;
        System.out.println(">> CLIENT >> " + username + " si è disconnesso." );

    }

    /**
     * Invia comandi al server.
     * @return 1 l'invio ha avuto successo, 0 se c'è IOException
     */
    public int send(String message) {
        try {

            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));

            int n;
            do {
                n = ((SocketChannel)key.channel()).write(buffer);
            } while (n>0);

            // messaggi di risposta a addfriend per dare feedback all'utente tramite gui client
            if(message.split(" ")[0].equals("addfriend")) {
                buffer.clear();
                buffer = ByteBuffer.allocate(1024);
                do {
                    n = ((SocketChannel)key.channel()).read(buffer);
                } while(n==0);
                do {
                    n = ((SocketChannel)key.channel()).read(buffer);
                } while(n>0);
                buffer.flip();
                String risposta = StandardCharsets.UTF_8.decode(buffer).toString();
                if(risposta.split(" ")[0].equals("answer")) { 
                    switch (risposta.split(" ")[1]) {
                        case "ADDFRIENDOK" :
                            System.out.println(">> CLIENT >> AddFriend >> Amicizia creata con successo.");
                            try {Thread.sleep(100);}
                            catch (InterruptedException e) {}
                            send("online"); // aggiorno la lista degli amici online in caso l'utente appena aggiunto sia collegato per mostrarlo nella lista
                            return 1;
                        case "ADDFRIENDERR1" : // uno dei due username non esiste
                        System.out.println(">> CLIENT >> AddFriend >> Uno dei due username non esiste.");
                            return -1;
                        case "ADDFRIENDERR2" : // la relazione di amicizia è già esistente
                        System.out.println(">> CLIENT >> AddFriend >> La relazione di amicizia è già presente nel database.");
                            return -2;
                        case "ADDFRIENDERR3" : // se nickUtente e nickAmico sono lo stesso username
                        System.out.println(">> CLIENT >> AddFriend >> I due username sono uguali.");
                            return -3;
                        default : 
                            return 0;
                    }

                }

            }
            return 1;
        } catch (IOException e) {
            System.out.println(">> CLIENT >> Send >> " + e.getMessage());
        }
        return 0;
    }

    /**
     * Elabora comandi ricevuti dal server.
     * @param received la stringa comando mandata dal server
     * @return 1 se la stringa è elaborata con successo, 0 se la stringa è formattata male
     */
    public int receive(String received) {
        // il server invia comandi del tipo <comando informazioni>
        if(received.isEmpty() || received.isBlank() || received == null) {
            System.out.println(">> CLIENT >> Stringa comando ricevuta dal server formattata male.");
            return 0;
        }
        else {
            // System.out.println(">> CLIENT >> comando ricevuto >> " + received);
            String comando = received.split(" ")[0];
            switch (comando) {
                case "answer" :

                    if(received.contains("challenge")) { // messaggi di inizio e fine sfida
                        // recupero i punti ottenuti
                        int points = Integer.parseInt(received.split(" ")[2]);

                        // sfida vinta
                        if (received.split(" ")[1].equals("challengewon")) {
                            // System.out.println(">> CLIENT >> Hai vinto la sfida e sei a " + points + " punti.");
                            WQClientLink.gui.challengeResultDialog("challengewon", points);
                        } 
                        else if (received.split(" ")[1].equals("challengelost")) {
                            // System.out.println(">> CLIENT >> Hai perso la sfida e sei a " + points + " punti.");
                            WQClientLink.gui.challengeResultDialog("challengelost", points);
                        }
                        else if (received.split(" ")[1].equals("challengetie")) {
                            // System.out.println(">> CLIENT >> Hai pareggiato la sfida e sei a " + points + " punti.");
                            WQClientLink.gui.challengeResultDialog("challengetie", points);
                        }

                        // aggiorno i punti sulla GUI
                        WQClientLink.gui.setPoints(points);

                    }

                    else { // le altre stringhe le stampo sul terminale
                        System.out.println(">> CLIENT >> " + received);
                    }
                    return 0;

                case "online" :
                    String amiciOnline = received.substring(comando.length()+1);
                    WQClientLink.gui.updateOnlineFriends(amiciOnline);
                    return 1;

                case "userpoints" :
                    int n = Integer.parseInt(received.split(" ")[1]);
                    System.out.println(">> CLIENT receive >> punti utente >> " + received.split(" ")[1]);
                    WQClientLink.gui.setPoints(n);
                    return 1;

                case "challengeround" : // messaggi che arrivano durante una sfida
                    String parola = received.split(" ")[1];

                    if (parola.equals("1")) { // inizio sfida
                        System.out.println(">> CLIENT >> Sfida iniziata.");
                        WQClientDatagramReceiver.sfidaInCorso = true;
                        WQClientLink.gui.startChallenge();
                        translationTimer = new Timer();
                        translationTimer.schedule(new WQClientTimerTask(this), 60000); // T2 = 1 minuto per la sfida
                    }
                    else if (parola.equals("-1")) { // fine sfida (errore)
                        System.out.println(">> CLIENT >> Errore. Sfida terminata.");
                        WQClientLink.gui.challenger = null;
                        WQClientDatagramReceiver.sfidaInCorso = false;
                    }
                    else if (parola.equals("-2")) { // sfida rifiutata
                        System.out.println(">> CLIENT >> Sfida rifiutata.");
                        WQClientLink.gui.challenger = null;
                    }
                    else if (parola.equals("-3")) { // fine sfida
                        System.out.println(">> CLIENT >> Sfida completata.");
                        if (translationTimer!=null) {
                            translationTimer.cancel();
                            translationTimer = null;
                        }
                        WQClientDatagramReceiver.sfidaInCorso = false;
                        WQClientLink.gui.endChallenge();
                        
                    }
                    else { // parola da tradurre per la sfida
                        System.out.println(">> CLIENT >> Parola da tradurre: " + parola);
                        if(translationTimer!=null) {
                            WQClientLink.gui.setCurrentWord(parola);
                        }
                        else {
                            WQClientDatagramReceiver.sfidaInCorso = false;
                            WQClientLink.gui.endChallenge();
                        }
                        // translationTimer = new Timer();
                        // translationTimer.schedule(new WQClientTimerTask(this), 10000); // 10 secondi per tradurre una parola
                    }
                    return 1;

                case "leaderboard" :
                    if (received.split(" ").length>1) WQClientLink.gui.mostraClassifica(received.split(" ")[1]);
                    else WQClientLink.gui.mostraClassifica("");
                    return 1;

                case "friendlist" :
                    if (received.split(" ").length>1) WQClientLink.gui.listaAmici(received.split(" ")[1]);
                    else WQClientLink.gui.listaAmici("");
                    return 1;

                default : 
                    return 0;


            }
        }
        
    }
    
    public static void main(final String[] args) {

        int porta = Integer.parseInt(args[0]);
        WQClient myClient = new WQClient(porta);
        WQClientGUI myClientGUI = new WQClientGUI();

    }
        
}