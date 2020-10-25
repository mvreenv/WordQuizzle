package client;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Classe che implementa metodi per visualizzare finestre di dialogo personalizzate.
 * @author Marina Pierotti
 */
public class WQClientGUIPopup {

    /**
     * Mantiene il risultato da restituire quando si interagisce con la finestra di dialogo per l'accettazione della sfida.
     */
    private static int result = JOptionPane.DEFAULT_OPTION;

    /**
     * Mostra una finestra di dialogo con un messaggio testuale.
     * @param parent Finestra genitore del dialogo.
     * @param message Messaggio da visualizzare nel dialogo.
     * @param dialogTitle Titolo della finestra di dialogo.
     */
    public static void showMessageDialog(JFrame parent, String message, String dialogTitle) {

        JFrame dialogFrame = new JFrame(dialogTitle);
        dialogFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel messagePanel = new JPanel();
        messagePanel.setBackground(WQClientGUI.LIGHT_BLUE);
        messagePanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.insets.left = 10;
        constraints.insets.right = 10;
        constraints.insets.top = 20;
        constraints.insets.bottom = 0;

        int gridy = 0;
        for (String line : message.split("\n")) {
            JLabel lineLabel = new JLabel(line);
            lineLabel.setSize(lineLabel.getPreferredSize());
            lineLabel.setForeground(WQClientGUI.WHITE);
            constraints.gridx = 0;
            constraints.gridy = gridy;
            messagePanel.add(lineLabel, constraints);
            gridy++;
            constraints.insets.top = 0;

        }

        JButton okButton = new JButton("OK");
        okButton.setBackground(WQClientGUI.DARK_BLUE);
        okButton.setForeground(WQClientGUI.MINT_GREEN);
        okButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dialogFrame.dispose();
            }
        });

        dialogFrame.getRootPane().setDefaultButton(okButton);

        constraints.gridx = 0;
        constraints.gridy = gridy + 1;
        constraints.insets.top = 10;
        constraints.insets.bottom = 10;
        constraints.anchor = GridBagConstraints.ABOVE_BASELINE_TRAILING;
        messagePanel.add(okButton, constraints);

        dialogFrame.add(messagePanel);

        dialogFrame.pack();
        if (parent != null) {
            dialogFrame.setLocation(parent.getX() + parent.getWidth() / 2 - dialogFrame.getWidth() / 2,
                    parent.getY() + parent.getHeight() / 2 - dialogFrame.getHeight() / 2);
        } else {
            dialogFrame.setLocationRelativeTo(null);
        }
        dialogFrame.setVisible(true);

    }

    /**
     * Mostra una finestra di dialogo che notifica l'utente che ha ricevuto una richiesta di sfida, e chiede se vuole accettarla o rifiutarla.
     * @param parent Finestra genitore della finestra di dialogo.
     * @param adversary Avversario da cui si riceve la richiesta di sfida.
     * @return Risultato dell'azione eseguita dall'utente: JOptionPane.DEFAULT_OPTION se l'utente chiude la finestra di dialogo, JOptionPane.YES_OPTION se l'utente accetta la richiesta di sfida, JOptionPane.NO_OPTION se l'utente rifiuta la richiesta di sfida.
     */
    public static int showChallengeDialog(JFrame parent, String adversary) {

        result = JOptionPane.DEFAULT_OPTION;

        JFrame challengeFrame = new JFrame("Sfida");
        challengeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLabel challengeLabel1 = new JLabel(adversary + " ti sta sfidando!");
        challengeLabel1.setForeground(WQClientGUI.WHITE);

        Icon icon = new ImageIcon("challenge.gif");
        JLabel gifLabel = new JLabel(icon);

        JPanel challengePanel = new JPanel();
        challengePanel.setBackground(WQClientGUI.LIGHT_BLUE);
        challengePanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.insets.left = 10;
        constraints.insets.right = 10;
        constraints.insets.top = 10;
        constraints.insets.bottom = 0;

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        challengePanel.add(challengeLabel1, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        challengePanel.add(gifLabel, constraints);

        JButton acceptButton = new JButton("Accetta");
        acceptButton.setBackground(WQClientGUI.DARK_BLUE);
        acceptButton.setForeground(WQClientGUI.MINT_GREEN);
        acceptButton.setMargin(new Insets(5, 5, 5, 5));
        acceptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = JOptionPane.YES_OPTION;
                challengeFrame.setVisible(false);
            }
        });

        JButton refuseButton = new JButton("Rifiuta");
        refuseButton.setBackground(WQClientGUI.DARK_BLUE);
        refuseButton.setForeground(WQClientGUI.MINT_GREEN);
        refuseButton.setMargin(new Insets(5, 5, 5, 5));
        refuseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = JOptionPane.NO_OPTION;
                challengeFrame.setVisible(false);
            }
        });

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.insets.bottom = 10;
        constraints.insets.left = 30;
        constraints.gridwidth = 1;
        challengePanel.add(acceptButton, constraints);

        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.insets.left = 10;
        constraints.insets.right = 30;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.ABOVE_BASELINE_TRAILING;
        challengePanel.add(refuseButton, constraints);

        challengeFrame.add(challengePanel);

        challengeFrame.pack();
        if (parent!=null) {
            challengeFrame.setLocation(parent.getX() + parent.getWidth()/2 - challengeFrame.getWidth()/2, parent.getY() + parent.getHeight()/2 - challengeFrame.getHeight()/2);
        }
        else {
            challengeFrame.setLocationRelativeTo(null);
        }
        challengeFrame.setVisible(true);

        // dopo 10 secondi la finestra scompare (T1=10 secondi tempo per rispondere a una richiesta di sfida)
        TimerTask task = new TimerTask(){
            @Override
            public void run() {
                challengeFrame.setVisible(false);
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 10000);

        do {
            try { Thread.sleep(500);} 
            catch (InterruptedException e) {}
        } while (challengeFrame.isVisible());

        challengeFrame.dispose();
        return result;

    }
    
}