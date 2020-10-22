package server;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import org.w3c.dom.UserDataHandler;

import client.WQClientLink;
import common.WQUser;

/**
 * Gestore della comunicazione col server della singola istanza client.
 */

public class WQManager implements Runnable {

    /**
     * Riferimento al server.
     */
    private WQServer server;

    /**
     * Canale di comunicazione.
     */
    private SocketChannel socket;

    /**
     * Chiave per la comunicazione.
     */
    private SelectionKey key;

    /**
     * Indica lo stato del gestore.
     */
    public boolean isOnline;

    /**
     * Userame dell'utente gestito da questo gestore.
     */
    public String username;

    /**
     * Numero di porta per la sfida.
     */
    public int portaSfida;

    /**
     * Flag che indica se c'è una sfida in corso.
     */
    public boolean isPlaying;

    /**
     * Lista delle parole da tradurre per la sfida, finché non c'è una sfida in
     * corso è null.
     */
    public HashMap<String, ArrayList<String>> words;

    /**
     * Punti ottenuti durante una sifda.
     */
    public int matchPoints;

    /**
     * Costruttore.
     * 
     * @param srv riferimento al server
     * @param skt canale di comunicazione
     */
    public WQManager(WQServer srv, SocketChannel skt) {
        this.server = srv;
        this.socket = skt;
        this.isOnline = true;
    }

    /**
     * Invia il messaggio msg al client.
     * 
     * @param str Il testo da spedire
     */
    public void send(String msg) {
        try {
            ByteBuffer buf = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
            int n;
            do {
                n = ((SocketChannel) key.channel()).write(buf);
            } while (n > 0);
        } catch (Exception e) {
        }
    }

    /**
     * Invia in UDP la richiesta di sfida da parte di nickSfidante al client gestito da questo WQManager.
     * @param nickSfidante 
     * @param porta gli passo la porta di ascolto del server
     */
    public DatagramSocket challenge(String nickSfidante, int porta) {
        
        try {
            DatagramSocket datagramSocket;
            datagramSocket = new DatagramSocket();
            datagramSocket.connect(InetAddress.getByName("127.0.0.1"), this.portaSfida);
            datagramSocket.setSoTimeout(10000); // timeout della sfida T1 = 10 secondi
            byte[] buffer = ("challengerequest " + nickSfidante).getBytes(StandardCharsets.UTF_8);
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
            datagramSocket.send(datagramPacket);
            System.out.println(">> MANAGER " + this.username + " INVIO UDP >> " + StandardCharsets.UTF_8.decode(ByteBuffer.wrap(datagramPacket.getData())).toString() + " - indirizzo " + datagramPacket.getAddress() + ":" + datagramPacket.getPort());

            try {
                buffer = new byte[256];
                datagramPacket = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(datagramPacket);
                String ricevuta = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(datagramPacket.getData())).toString();
                ricevuta.stripTrailing();
                System.out.println(">> MANAGER " + this.username + " >> DATAGRAM SOCKET RECEIVE >> " + ricevuta);
                if (ricevuta.split(" ")[0].equals("challengeresponse")) {
                    // ricevo 'challengeresponse OK', 'challengeresponse NO' o 'challengeresponse BUSY'
                    String risposta = ricevuta.split(" ")[1];
                    if (risposta.contains("OK")) {
                        System.out.println(">> MANAGER >> " + this.username + " ha accettato la sfida.");
                        return datagramSocket;
                    }
                    else if (risposta.contains("BUSY")) {
                        System.out.println(">> MANAGER >> " + this.username + " è già occupato in un'altra sfida.");
                        return null;
                    }
                    else { // risposta.contains("NO")
                        System.out.println(">> MANAGER >> " + this.username + " ha rifiutato la sfida.");
                        return null;
                    }
                }
                else {
                    System.out.println(">> MANAGER >> Pacchetto UDP con comando sconosciuto.");
                    return null;
                }
            } catch (SocketTimeoutException e) { // controllo il timeout
                System.out.println(">> MANAGER >> Timeout sfida.");
                return null;
            }

        } catch (IOException e) {
            System.out.println(">> MANAGER >> Eccezione Challenge >> " + e.getMessage());
            e.printStackTrace();
            return null;
        } 
    }

    /**
     * Avvia la sfida sul client gestito. Invia le parole da tradurre, aspetta la traduzione e assegna i punti.
     */
    public void startChallenge() {

        isPlaying = true;
        this.matchPoints = 0;

        ByteBuffer buffer = ByteBuffer.allocate(256);
        int n;

        try {
            for (String parola : words.keySet()) {

                send("challengeround " + parola);

                do {
                    try { Thread.sleep(100); }
                    catch (InterruptedException e) {}
                    buffer.clear();
                    n = ((SocketChannel)key.channel()).read(buffer);
                } while (n==0);

                do {
                    n = ((SocketChannel)key.channel()).read(buffer);
                } while (n>0);

                buffer.flip();
                String ricevuta = StandardCharsets.UTF_8.decode(buffer).toString();
                String comando = ricevuta.split(" ")[0];
                String parolaTradotta = ricevuta.split(" ")[1];
                ArrayList<String> possibiliTraduzioni = words.get(parola);

                if(comando.equals("challengeanswer")) {
                    if(parolaTradotta.equals("-1")) { // tempo scaduto per la parola
                        matchPoints += 0; // parola non tradotta, 0 punti
                    }
                    else if (possibiliTraduzioni.contains(parolaTradotta.toLowerCase())) {
                        matchPoints += 2; // traduzione corretta (X=+2)
                    }
                    else {
                        matchPoints -= 2; // traduzione errata (Y=-2)
                    }
                }
            }

        } catch (IOException e) {}

        // fine sfida
        send("challengeround -3");
        isPlaying = false;
        words = null;
    }

    @Override
    public void run() {
        ByteBuffer buffer = ByteBuffer.allocate(256);
        String messaggio;

        try {

            // configuro socket e selector per la comunicazione
            socket.configureBlocking(false);
            Selector selector = Selector.open();
            key = socket.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

            // corpo del thread gestore
            do {

                if (words==null) { // niente sfida in corso

                    int n; // mantiene il numero di byte letti in 'buffer', diventa -1 se si arriva alla fine dello stream del SocketChannel
                    do {
                        try {
                        Thread.sleep(100);
                        } catch (InterruptedException e) {}
                        buffer.clear();
                        n =  ((SocketChannel) key.channel()).read(buffer);
                    } while (n==0 && words==null); //

                    if (n==-1) isOnline = false; // non c'è più niente da leggere sul SocketChannel

                    else if(words==null) { // niente sfida in corso

                        do {
                            n = ((SocketChannel) key.channel()).read(buffer);
                        } while (n>0);
                        buffer.flip(); // The limit is set to the current position and then the position is set to zero.

                        // parsing della stringa letta sul SocketChannel
                        String received = StandardCharsets.UTF_8.decode(buffer).toString();
                        String comando = received.split(" ")[0];

                        // comandi che vengono inviati dalle istanze client al server
                        switch (comando) {
                            case "login" :
                                if(this.username == null) {
                                    String user = received.split(" ")[1];
                                    String pw = received.split(" ")[2];
                                    System.out.println(">> MANAGER >> Verifica credenziali " + user + " " + pw);
                                    int result = this.server.login(user, pw, this);
                                    // login effettuato con successo
                                    if(result == 0) { 
                                        this.username=user;

                                        // invio la risposta positiva
                                        messaggio = "answer LOGINOK";
                                        ByteBuffer buf = ByteBuffer.wrap(messaggio.getBytes(StandardCharsets.UTF_8));
                                        do {
                                            n = ((SocketChannel)key.channel()).write(buf);
                                        } while (n>0);
                                        System.out.println(">> MANAGER >> " + user + " si è connesso.");

                                        // invio le informazioni riguardanti l'utente che si è loggato
                                        Gson json = new Gson();
                                        messaggio = json.toJson(this.server.getUser(this.username));
                                        buf.clear();
                                        buf = ByteBuffer.wrap(messaggio.getBytes(StandardCharsets.UTF_8));
                                        do {
                                            n = ((SocketChannel)key.channel()).write(buf);
                                        } while (n>0);

                                        // leggo numero di porta per le sfide
                                        buf = ByteBuffer.allocate(256);
                                        do {
                                            Thread.sleep(100);
                                            buf.clear();
                                            n = ((SocketChannel)key.channel()).read(buf);
                                        } while (n==0);
                                        do {
                                            n = ((SocketChannel)key.channel()).read(buf);
                                        } while (n>0);
                                        buf.flip();

                                        received = StandardCharsets.UTF_8.decode(buf).toString(); // challengeport <numeroporta>
                                        if(received.split(" ")[0].equals("challengeport"))
                                            portaSfida = Integer.parseInt(received.split(" ")[1]);
                                    }

                                    // messaggi di errore sul login
                                    else if (result==-1) {
                                        messaggio = "answer LOGINERR1";
                                        ByteBuffer buf = ByteBuffer.wrap(messaggio.getBytes(StandardCharsets.UTF_8));
                                        do {
                                            n = ((SocketChannel)key.channel()).write(buf);
                                        } while (n>0);
                                        isOnline = false;
                                    }
                                    else if (result==-2) {
                                        messaggio = "answer LOGINERR2";
                                        ByteBuffer buf = ByteBuffer.wrap(messaggio.getBytes(StandardCharsets.UTF_8));
                                        do {
                                            n = ((SocketChannel)key.channel()).write(buf);
                                        } while (n>0);
                                        isOnline = false;
                                    }
                                    else if (result==-3) {
                                        messaggio = "answer LOGINERR3";
                                        ByteBuffer buf = ByteBuffer.wrap(messaggio.getBytes(StandardCharsets.UTF_8));
                                        do {
                                            n = ((SocketChannel)key.channel()).write(buf);
                                        } while (n>0);
                                        isOnline = false;
                                    }
                                }
                                else {
                                    messaggio = "answer ERR";
                                    ByteBuffer buf = ByteBuffer.wrap(messaggio.getBytes(StandardCharsets.UTF_8));
                                    do {
                                        n = ((SocketChannel)key.channel()).write(buf);
                                    } while (n>0);
                                }
                                break;

                            case "addfriend" : 
                                System.out.println(">> MANAGER >> verifica aggiungi_amico " + received.split(" ")[1] + " " + received.split(" ")[2]);
                                int result = this.server.aggiungiAmico(received.split(" ")[1], received.split(" ")[2]);
                                if (result==0) { // la registrazione dell'amicizia è avvenuta
                                    messaggio = "answer ADDFRIENDOK";
                                    ByteBuffer buf = ByteBuffer.wrap(messaggio.getBytes(StandardCharsets.UTF_8));
                                    do {
                                        n = ((SocketChannel)key.channel()).write(buf);
                                    } while (n>0);
                                }
                                else if (result==-1) { // uno dei due username non esiste
                                    messaggio = "answer ADDFRIENDERR1";
                                    ByteBuffer buf = ByteBuffer.wrap(messaggio.getBytes(StandardCharsets.UTF_8));
                                    do {
                                        n = ((SocketChannel)key.channel()).write(buf);
                                    } while (n>0);
                                }
                                else if (result==-2) { // la relazione di amicizia è già esistente
                                    messaggio = "answer ADDFRIENDERR2";
                                    ByteBuffer buf = ByteBuffer.wrap(messaggio.getBytes(StandardCharsets.UTF_8));
                                    do {
                                        n = ((SocketChannel)key.channel()).write(buf);
                                    } while (n>0);
                                }
                                else if (result==-3) { // se nickUtente e nickAmico sono lo stesso username
                                    messaggio = "answer ADDFRIENDERR3";
                                    ByteBuffer buf = ByteBuffer.wrap(messaggio.getBytes(StandardCharsets.UTF_8));
                                    do {
                                        n = ((SocketChannel)key.channel()).write(buf);
                                    } while (n>0);
                                }
                                break;

                            case "online" : 
                                System.out.println(">> MANAGER >> verifica amici online di " + username);
                                ArrayList<String> amiciOnline = this.server.usersOnline(username);
                                messaggio = "online "; // formato : online amico1 amico2 ...
                                for(int i=0; i<amiciOnline.size(); i++) {
                                    messaggio = messaggio + amiciOnline.get(i) + " ";
                                }
                                send(messaggio);
                                break;

                            case "friendlist" :
                            System.out.println(">> MANAGER >> recupero lista completa amici di " + username);
                                messaggio = "friendlist " + this.server.lista_amici(username);
                                send(messaggio);
                                break;
                            
                            case "leaderboard" :
                                System.out.println(">> MANAGER >> recupero classifica amici di " + username);
                                messaggio = "leaderboard " + this.server.mostra_classifica(username);
                                send(messaggio);
                                break;

                            case "userpoints" :
                                System.out.println(">> MANAGER >> recupero il punteggio di " + username);
                                messaggio = "userpoints " + this.server.getUser(received.split(" ")[1]).points;
                                send(messaggio);
                                break;
                            
                            case "challenge" :
                                if(received.split(" ").length>1) {
                                    System.out.println(">> MANAGER >> " + username + " sfida " + received.split(" ")[1]);
                                    this.server.sfida(this.username, received.split(" ")[1]);
                                }
                                else send("challengeround -1");
                                break;
                        }

                    }

                }

                else { // words != null
                    // System.out.println("Parole sfida: ");
                    // for (String parola : words.keySet()) {
                    //     System.out.print(parola + ": ");
                    //     for (String traduzione : words.get(parola)) {
                    //         System.out.print(traduzione + " ");
                    //     }
                    //     System.out.print("\n");
                    // }
                    this.startChallenge();
                }

 
            } while(isOnline);
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        // isOnline è diventato false 
        if (this.username != null) { 
            System.out.println(">> MANAGER >> " + this.username + " è andato offline.");
            this.server.logout(username);
        }
    }
}