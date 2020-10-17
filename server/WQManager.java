package server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;

/**
 * Gestore della comunicazione col server della singola istanza client.
 * 
 * @author Marina Pierotti
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
    private boolean isOnline;

    /**
     * Userame dell'utente gestito da questo gestore.
     */
    private String username;

    /**
     * Numero di porta per la sfida.
     */
    private int portaSfida;

    /**
     * Flag che indica se c'è una sfida in corso.
     */
    private boolean isPlaying;

    /**
     * Lista delle parole da tradurre per la sfida, finché non c'è una sfida in corso è null.
     */
    private HashMap<String, ArrayList<String>> words;


    /**
     * Costruttore. 
     * @param srv riferimento al server
     * @param skt canale di comunicazione
     */
    public WQManager(WQServer srv, SocketChannel skt) {
        this.server = srv;
        this.socket = skt;
        this.isOnline = true;
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
            while(isOnline) {

                if (words==null) { // niente sfida in corso

                    int n; // mantiene il numero di byte letti in 'buffer', diventa -1 se si arriva alla fine dello stream del SocketChannel
                    do {
                        Thread.sleep(100);
                        buffer.clear();
                        n =  ((SocketChannel) key.channel()).read(buffer);
                    } while (n==0 && words==null);

                    if (n==-1) isOnline = false; // non c'è più niente da leggere sul SocketChannel

                    else if(words==null) { 

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
                                    String user = comando.split(" ")[1];
                                    String pw = comando.split(" ")[2];
                                    System.out.println(">> Manager >> Verifica " + user + pw);
                                    int result = this.server.login(user, pw, this);
                                    // login effettuato con successo
                                    if(result == 0) { 
                                        this.username=user;

                                        // invio la risposta positiva
                                        messaggio = "answer:OK";
                                        ByteBuffer buf = ByteBuffer.wrap(messaggio.getBytes(StandardCharsets.UTF_8));
                                        do {
                                            n = ((SocketChannel)key.channel()).write(buf);
                                        } while (n>0);
                                        System.out.println(">> Manager >> " + user + " si è connesso.");

                                        // invio le informazioni riguardanti l'utente che si è loggato
                                        Gson json = new Gson();
                                        messaggio = json.toJson(this.server.getUser(this.username));
                                        buf = ByteBuffer.wrap(messaggio.getBytes(StandardCharsets.UTF_8));
                                        do {
                                            n = ((SocketChannel)key.channel()).write(buf);
                                        } while (n>0);

                                        // leggo dal Channel il numero di porta per le sfide
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
                                        received = StandardCharsets.UTF_8.decode(buf).toString();
                                        portaSfida = Integer.parseInt(received.split(" ")[1]);
                                    }

                                    // messaggi di errore sul login
                                    else if (result==-1) {
                                        messaggio = "answer:LOGINERR1";
                                        ByteBuffer buf = ByteBuffer.wrap(messaggio.getBytes(StandardCharsets.UTF_8));
                                        do {
                                            n = ((SocketChannel)key.channel()).write(buf);
                                        } while (n>0);
                                        isOnline = false;
                                    }
                                    else if (result==-2) {
                                        messaggio = "answer:LOGINERR2";
                                        ByteBuffer buf = ByteBuffer.wrap(messaggio.getBytes(StandardCharsets.UTF_8));
                                        do {
                                            n = ((SocketChannel)key.channel()).write(buf);
                                        } while (n>0);
                                        isOnline = false;
                                    }
                                    else if (result==-3) {
                                        messaggio = "answer:LOGINERR3";
                                        ByteBuffer buf = ByteBuffer.wrap(messaggio.getBytes(StandardCharsets.UTF_8));
                                        do {
                                            n = ((SocketChannel)key.channel()).write(buf);
                                        } while (n>0);
                                        isOnline = false;
                                    }
                                }
                                else {
                                    messaggio = "answer:ERR";
                                    ByteBuffer buf = ByteBuffer.wrap(messaggio.getBytes(StandardCharsets.UTF_8));
                                    do {
                                        n = ((SocketChannel)key.channel()).write(buf);
                                    } while (n>0);
                                }
                                break;
                            case "aggiungi_amico" :

                                break;
                            case "online" :

                                break;
                        }

                    }

                }

            }
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (this.username != null) {
            System.out.println(">> Manager >> " + this.username + " è andato offline.");
            this.server.logout(username);
        }
        // WQServerController.gui.subThreadsText();
    }
}