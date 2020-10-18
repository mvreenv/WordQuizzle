package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import common.WQInterface;
import common.WQUser;

import com.google.gson.*;

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
     * 
     * @param username
     * @param password
     * @return
     */
    public int login(String username, String password) {

        try {
            if (registra_utente(username, password) > -2) { // 0 registrato con successo, -1 utente già registrato
                // connessione TCP al server
                socket = SocketChannel.open();
                System.out.println(">> CLIENT >> Connessione in corso sulla porta " + (port));
                socket.connect(new InetSocketAddress("127.0.0.1", port)); // indirizzo di loopback (perché )il server gira sulla stessa macchina)
                socket.configureBlocking(false);
                Selector selector = Selector.open();
                key = socket.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);

                // esegue il login
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

                    if (received.split(" ")[1].equals("OK")) {
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

                        // avvio il thread listener TCP del client
                        //new Thread(new WQClientReceiver(socket, key)).start();

                        // avvio il listener UDP 
                        return 0;

                    }
                    else if (received.split(" ")[1].equals("LOGINERR1")) {
                        return -1;
                    }
                    else if (received.split(" ")[1].equals("LOGINERR2")) {
                        return -2;
                    }
                    else if (received.split(" ")[1].equals("LOGINERR3")) {
                        return -3;
                    }
                    else // errore generico
                        return -4;
                }

            } 
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return -4; // errore generico
    }

    /**
     * Elabora comandi ricevuti dal server.
     * @param received la stringa comando mandata dal server
     * @return 0 se la stringa è elaborata con successo, -1 se la stringa è formattata male
     */
    public int receive(String received) {
        // il server invia comandi del tipo "comando informazioni"
        if(received.isEmpty() || received.isBlank() || received == null) {
            System.out.println(">> CLIENT >> Stringa comando ricevuta dal server formattata male.");
            return -1;
        }
        else {
            String comando = received.split(" ")[0];
            switch (comando) {
                case "answer" :
                    System.out.println(received);
                    return 0;


                default :
                    return -1;
            }

        }
    }
    
    public static void main(final String[] args) {

        int porta = Integer.parseInt(args[0]);
        WQClient myClient = new WQClient(porta);
        WQClientGUI myClientGUI = new WQClientGUI();

    }
        
}