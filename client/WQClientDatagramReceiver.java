package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import javax.swing.JOptionPane;

/**
 * Thread listener dei pacchetti UDP in arrivo al client.
 */

public class WQClientDatagramReceiver implements Runnable {

    /**
     * Socket per i datagrammi UDP.
     */
    private DatagramSocket datagramSocket;

    /**
     * Flag che indica se c'è una sfida in corso.
     */
    public static boolean sfidaInCorso = false;

    public WQClientDatagramReceiver(DatagramSocket socket) {
        this.datagramSocket = socket;
    }

    @Override
    public void run() {
        
        byte[] buffer;
        DatagramPacket datagramPacket;
        
        while (true) {
            try {
                
                buffer = new byte[52];
                datagramPacket = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(datagramPacket);

                String ricevuta = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(buffer)).toString();
                ricevuta.stripTrailing();
                // System.out.println(">> CLIENT UDP RECEIVER >> Ricevo: " + StandardCharsets.UTF_8.decode(ByteBuffer.wrap(datagramPacket.getData())).toString() + " - indirizzo " + datagramPacket.getAddress() + ":" + datagramPacket.getPort() );

                String comando = ricevuta.split(" ")[0];

                if(comando.equals("challengerequest")) { // comando tipo: challengeRequest nickSfidante

                    // verifica che non ci sia già una sfida in corso
                    if (sfidaInCorso) { 
                        System.out.println(">> CLIENT UDP RECEIVER >> Sfida rifiutata (sei già occupato).");
                        buffer = "challengeresponse BUSY".getBytes(StandardCharsets.UTF_8);
                        InetAddress indirizzo = datagramPacket.getAddress();
                        int porta = datagramPacket.getPort();
                        datagramPacket = new DatagramPacket(buffer, buffer.length, indirizzo, porta);
                        datagramSocket.send(datagramPacket);
                    }

                    // chiede all'utente cosa vuole fare con la richiesta di sfida
                    else { 
                        sfidaInCorso = true;
                        System.out.println(">> CLIENT UDP RECEIVER >> Sfida ricevuta.");
                        int n = WQClientLink.gui.challengeDialog(ricevuta.split(" ")[1]);

                        // l'utente accetta la sfida
                        if(n== JOptionPane.YES_OPTION) { 
                            
                            System.out.println(">> CLIENT UDP RECEIVER >> Sfida accettata.");
                            buffer = "challengeresponse OK".getBytes(StandardCharsets.UTF_8);
                            InetAddress indirizzo = datagramPacket.getAddress();
                            int porta = datagramPacket.getPort();
                            datagramPacket = new DatagramPacket(buffer, buffer.length, indirizzo, porta);
                            System.out.println(">> CLIENT UDP RECEIVER >> Invio: " + StandardCharsets.UTF_8.decode(ByteBuffer.wrap(datagramPacket.getData())).toString() + " - indirizzo " + datagramPacket.getAddress() + " :" + (datagramPacket.getPort()) );
                            datagramSocket.send(datagramPacket);

                            // WQClientLink.gui.startChallenge(ricevuta.split(" ")[1]);
                        }

                        // l'utente rifiuta la sfida
                        else { 
                            sfidaInCorso = false;
                            System.out.println(">> CLIENT UDP RECEIVER >> Sfida rifiutata.");
                            buffer = "challengeresponse NO".getBytes(StandardCharsets.UTF_8);
                            InetAddress indirizzo = datagramPacket.getAddress();
                            int porta = datagramPacket.getPort();
                            datagramPacket = new DatagramPacket(buffer, buffer.length, indirizzo, porta);
                            // System.out.println(">> CLIENT UDP RECEIVER >> Invio: " + StandardCharsets.UTF_8.decode(ByteBuffer.wrap(datagramPacket.getData())).toString() + " - indirizzo " + datagramPacket.getAddress() + " :" + (datagramPacket.getPort()) );
                            datagramSocket.send(datagramPacket);
                        }


                    }

                }

            } catch (IOException e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) { 
                    System.out.println(">> CLIENT UDP RECEIVER >> EXCEPTION >> " + e.getMessage());
                    // e.printStackTrace();
                }
            }
        }

    }
    
    
}
