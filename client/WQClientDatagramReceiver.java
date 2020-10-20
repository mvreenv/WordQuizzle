package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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
                
                buffer = new byte[256];
                datagramPacket = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(datagramPacket);

                String ricevuta = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(buffer)).toString();
                String comando = ricevuta.split(" ")[0];

                if(comando.equals("challengerequest")) { // comando tipo: challengeRequest nickSfidante

                    // verifica che non ci sia già una sfida in corso
                    if (sfidaInCorso) { 
                        buffer = "challengeresponse BUSY".getBytes(StandardCharsets.UTF_8);
                        datagramPacket = new DatagramPacket(buffer, buffer.length, datagramPacket.getAddress(), datagramPacket.getPort());
                        datagramSocket.send(datagramPacket);
                    }

                    // chiede all'utente cosa vuole fare con la richiesta di sfida
                    else { 
                        sfidaInCorso = true;
                        int n = WQClientLink.gui.challengeDialog(ricevuta.split(" ")[1]);

                        // l'utente accetta la sfida
                        if(n== JOptionPane.OK_OPTION) { 
                            buffer = "challengeresponse OK".getBytes(StandardCharsets.UTF_8);
                            datagramPacket = new DatagramPacket(buffer, buffer.length, datagramPacket.getAddress(), datagramPacket.getPort());
                            datagramSocket.send(datagramPacket);
                        }

                        // l'utente rifiuta la sfida
                        else { 
                            sfidaInCorso = false;
                            buffer = "challengeresponse NO".getBytes(StandardCharsets.UTF_8);
                            datagramPacket = new DatagramPacket(buffer, buffer.length, datagramPacket.getAddress(), datagramPacket.getPort());
                            datagramSocket.send(datagramPacket);
                        }


                    }

                }

            } catch (IOException e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {} 
                System.out.println(">> CLIENT DATAGRAM RECEIVER >> EXCEPTION >> " + e.getMessage());
                // e.printStackTrace();
            }
        }

    }
    
    
}
