package client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * Thread listener dei messaggi TCP in arrivo dal server.
 */

public class WQClientReceiver implements Runnable {

    /**
     * Canale di comunicazione.
     */
    private SocketChannel socket;

    /**
     * Chiave per la comunicazione.
     */
    private SelectionKey key;

    public WQClientReceiver(SocketChannel s, SelectionKey k) {
        this.socket = s;
        this.key = k;
    }

    @Override
    public void run() {

        try {
            do  {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                int n;

                do {
                    try { 
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // e.printStackTrace();
                    }
                    buffer.clear();
                    
                    n = ((SocketChannel) key.channel()).read(buffer);
                    
                } while (n==0);

                do {
                    n = ((SocketChannel) key.channel()).read(buffer);
                } while (n>0);

                buffer.flip();
                String received = StandardCharsets.UTF_8.decode(buffer).toString();
                // System.out.println(">> CLIENT TCP RECEIVER >> " + received);
                WQClientLink.client.receive(received);
            } while (socket.isConnected());
               
        } catch (IOException e) {
            // System.out.println(">> CLIENT TCP RECEIVER >> EXCEPTION >> " + e.getMessage());
            // e.printStackTrace();
        }
    
    }
}
