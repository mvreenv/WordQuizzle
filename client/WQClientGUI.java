package client;

import common.WQUser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

/**
 * Classe che gestisce l'interfaccia grafica del client di WordQuizzle.
 * 
 * @author Marina Pierotti
 */

public class WQClientGUI {

    /**
     * Frame per la costruzione della GUI del client WordQuizzle.
     */
    private JFrame frame;

    /**
     * Pannello di login coi suoi componenti
     */
    private JPanel loginPanel;
    private JTextField usernameInput; // campo per inserire lo username
    private JPasswordField passwordInput; // campo per inserire la password
    private JButton loginButton; // se premuto tenta il login col contenuto di usernameInput e passwordInput
    private JButton registerButton; // se premuto tenta di registrare un nuovo utente con usernameInput e passwordInput

    /**
     * Pannello in alto alla finestra che mostra informazioni sullo username e il
     * punteggio
     */
    private JPanel topPanel;
    private String username; // username dell'utente loggato
    private int points; // punteggio totale dell'utente loggato
    private JLabel userLogged; // label per il nome 
    private JLabel pointsLogged; // label per i punti

    /**
     * Username dell'utente sfidato
     */
    private String challenger;

    /**
     * Panello che mostra le operazioni svolgibili dall'utente dopo il login
     */
    private JPanel settingsPanel;
    private JButton addFriendButton; // bottone per aggiungere un amico
    private JButton challengeFriendButton; // bottone per sfidare un amico
    private JButton showLeaderboardButton; // bottone per vedere la classifica coi propri amici
    private JButton showFriendListButton; // bottone per vedere la lista degli amici (anche offline)
    private JButton logoutButton; // bottone per eseguire il logout
    private JTextArea textAreaAmiciOnline;

    /**
     * Pannello dell'interfaccia della sfida
     */
    private JPanel challengePanel;
    private JTextField translationInput; // campo per inserire la parola tradotta
    private JButton translateButton; // bottone per inviare il contenuto di translationInput

    public WQClientGUI() {

        super();

        WQClientLink.gui = this;

        frame = new JFrame("WordQuizzle");
        frame.addWindowListener(new WindowListener(){

			@Override
			public void windowOpened(WindowEvent e) {}

			@Override
			public void windowClosing(WindowEvent e) { WQClientLink.client.logout(username); }

			@Override
            public void windowClosed(WindowEvent e) { WQClientLink.client.logout(username); }

			@Override
			public void windowIconified(WindowEvent e) {}

			@Override
			public void windowDeiconified(WindowEvent e) {}

			@Override
			public void windowActivated(WindowEvent e) {}

			@Override
			public void windowDeactivated(WindowEvent e) {}
            
        });
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLayout(new BorderLayout());

        // LOGIN PANEL START
        loginPanel = new JPanel();
        loginPanel.setLayout(new GridBagLayout()); // GridLayout(int rows, int cols, int hgap, int vgap)
        GridBagConstraints loginPanelConstraints = new GridBagConstraints();

        JLabel usernameLabel = new JLabel("Username");
        usernameInput = new JTextField();
        usernameLabel.setLabelFor(usernameInput);

        JLabel passwordLabel = new JLabel("Password");
        passwordInput = new JPasswordField();
        passwordLabel.setLabelFor(passwordInput);

        loginButton = new JButton("Login");
        loginButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String pw = String.copyValueOf(passwordInput.getPassword());
                int n = WQClientLink.client.login(usernameInput.getText(), pw);
                if (n==0) { // login avvenuto con successo
                    JOptionPane.showMessageDialog(frame, "Login avvenuto con successo!");
                    WQClientLink.client.send("online"); // chiedo al server la lista aggiornata degli utenti online
                    frame.invalidate();
                    frame.remove(loginPanel);
                    frame.add(topPanel);
                    frame.add(settingsPanel);
                    frame.revalidate();
                    frame.repaint();
                }
                else if (n==-1) { // password errata
                    JOptionPane.showMessageDialog(frame, "Password errata.", "Errore login", JOptionPane.WARNING_MESSAGE);
                }
                else if (n==-2) { // utente non registrato
                    JOptionPane.showMessageDialog(frame, "Username non riconosciuto.", "Errore login", JOptionPane.WARNING_MESSAGE);
                }
                else if (n==-3) { // l'utente è già loggato -- non dovrebbe mai succedere
                    JOptionPane.showMessageDialog(frame, "Utente già loggato.", "Errore login", JOptionPane.WARNING_MESSAGE);
                }
                
            }
        });

        registerButton = new JButton("Registrati");
        registerButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String pw = String.copyValueOf(passwordInput.getPassword());
                int n = WQClientLink.client.registra_utente(usernameInput.getText(), pw);
                if (n==0) {
                    JOptionPane.showMessageDialog(frame, "Registrazione avvenuta con successo.", "Esito registrazione", JOptionPane.INFORMATION_MESSAGE);
                    usernameInput.setText("");
                    passwordInput.setText("");
                }
                else if (n==-1) {
                    JOptionPane.showMessageDialog(frame, "Username già registrato, registrazione non eseguita.", "Esito registrazione", JOptionPane.ERROR_MESSAGE);
                    usernameInput.setText("");
                    passwordInput.setText("");
                }
                else if (n==-2) JOptionPane.showMessageDialog(frame, "Password vuota, registrazione fallita.", "Esito registrazione", JOptionPane.ERROR_MESSAGE);
                else if (n==-3) JOptionPane.showMessageDialog(frame, "Errore di comunicazione col server, riprovare.", "Esito registrazione", JOptionPane.ERROR_MESSAGE);
            }
        });

        loginPanelConstraints.fill = GridBagConstraints.HORIZONTAL;

        loginPanelConstraints.insets.right = 10;

        loginPanelConstraints.gridx = 0;
        loginPanelConstraints.gridy = 0;
        loginPanel.add(usernameLabel, loginPanelConstraints);

        loginPanelConstraints.gridx = 1;
        loginPanelConstraints.gridy = 0;
        loginPanel.add(usernameInput, loginPanelConstraints);

        loginPanelConstraints.insets.top = 10;

        loginPanelConstraints.gridx = 0;
        loginPanelConstraints.gridy = 1;
        loginPanel.add(passwordLabel, loginPanelConstraints);

        loginPanelConstraints.gridx = 1;
        loginPanelConstraints.gridy = 1;
        loginPanel.add(passwordInput, loginPanelConstraints);

        loginPanelConstraints.insets.top = 20;
        loginPanelConstraints.insets.right = 20;

        loginPanelConstraints.gridx = 0;
        loginPanelConstraints.gridy = 2;
        loginPanel.add(registerButton, loginPanelConstraints);

        loginPanelConstraints.insets.left = 20;

        loginPanelConstraints.gridx = 1;
        loginPanelConstraints.gridy = 2;
        loginPanel.add(loginButton, loginPanelConstraints);
        // LOGIN PANEL END
        
        // TOP PANEL START
        topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());

        userLogged = new JLabel(username);
        pointsLogged = new JLabel(Integer.toString(points));

        topPanel.add(BorderLayout.WEST, userLogged);
        topPanel.add(BorderLayout.EAST, pointsLogged);
        // TOP PANEL END

        // SETTINGS PANEL START
        settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridBagLayout());
        GridBagConstraints settingsPanelConstraints = new GridBagConstraints();

        addFriendButton = new JButton("Aggiungi amico");
        addFriendButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String friendName = JOptionPane.showInputDialog(frame, "Chi vuoi aggiungere come amico?", null);
                int n = WQClientLink.client.send("aggiungi_amico " + username + " " + friendName);
                if (n==1) JOptionPane.showMessageDialog(frame, friendName + " aggiunto alla tua lista amici.", "Amico", JOptionPane.INFORMATION_MESSAGE);
                else if (n==0) JOptionPane.showMessageDialog(frame, "Errore formattazione dati.", "Errore amicizia.", JOptionPane.ERROR_MESSAGE);
                else if (n==-1) JOptionPane.showMessageDialog(frame, "Uno dei due utenti non esiste.", "Errore amicizia.", JOptionPane.ERROR_MESSAGE);
                else if (n==-2) JOptionPane.showMessageDialog(frame, "Sei già amico di " + friendName + ".", "Errore amicizia.", JOptionPane.ERROR_MESSAGE);
                else if (n==-3) JOptionPane.showMessageDialog(frame, "Non puoi aggiungere te stesso come amico.", "Errore amicizia.", JOptionPane.ERROR_MESSAGE);
            }
        });
        settingsPanelConstraints.gridx = 0;
        settingsPanelConstraints.gridy = 1;
        settingsPanelConstraints.insets.bottom = 10;
        settingsPanel.add(addFriendButton, settingsPanelConstraints);

        challengeFriendButton = new JButton("Sfida amico");
        settingsPanelConstraints.gridx = 0;
        settingsPanelConstraints.gridy = 2;
        settingsPanel.add(challengeFriendButton, settingsPanelConstraints);

        showLeaderboardButton = new JButton("Mostra classifica");
        showLeaderboardButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                int n = WQClientLink.client.send("leaderboard " + WQClientLink.gui.username);
                if (n==0) JOptionPane.showMessageDialog(frame, "Errore classifica.", "Errore.", JOptionPane.ERROR_MESSAGE);
            }
        });
        settingsPanelConstraints.gridx = 0;
        settingsPanelConstraints.gridy = 3;
        settingsPanel.add(showLeaderboardButton, settingsPanelConstraints);

        showFriendListButton = new JButton("Lista completa amici");
        showFriendListButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                int n = WQClientLink.client.send("friendlist " + WQClientLink.gui.username);
                if (n==0) JOptionPane.showMessageDialog(frame, "Errore lista amici.", "Errore.", JOptionPane.ERROR_MESSAGE);
            }
        });
        settingsPanelConstraints.gridx = 0;
        settingsPanelConstraints.gridy = 4;
        settingsPanel.add(showFriendListButton, settingsPanelConstraints);
        
        logoutButton = new JButton("Aggiorna amici online");
        logoutButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                int n = WQClientLink.client.send("online " + WQClientLink.gui.username);
                if (n==0) JOptionPane.showMessageDialog(frame, "Errore amici online.", "Errore.", JOptionPane.ERROR_MESSAGE);
            }
        });
        settingsPanelConstraints.gridx = 0;
        settingsPanelConstraints.gridy = 5;
        settingsPanelConstraints.insets.top = 0;
        settingsPanelConstraints.insets.bottom = 0;
        settingsPanel.add(logoutButton, settingsPanelConstraints);

        JLabel labelAmiciOnline = new JLabel("Amici online");
        settingsPanelConstraints.gridx = 1;
        settingsPanelConstraints.gridy = 0;
        settingsPanelConstraints.insets.left = 10;
        settingsPanel.add(labelAmiciOnline, settingsPanelConstraints);

        textAreaAmiciOnline = new JTextArea();
        textAreaAmiciOnline.setAutoscrolls(true);
        textAreaAmiciOnline.setEditable(false);
        textAreaAmiciOnline.setLineWrap(true);
        textAreaAmiciOnline.setPreferredSize(new Dimension(150, 170));
        settingsPanelConstraints.gridx = 1;
        settingsPanelConstraints.gridy = 1;
        settingsPanelConstraints.gridheight = 5;
        settingsPanelConstraints.insets.left = 10;
        settingsPanel.getLayout().preferredLayoutSize(frame);
        settingsPanel.add(textAreaAmiciOnline, settingsPanelConstraints);
        
        //SETTIMGS PANEL END

        // CHALLENGE PANEL START
        challengePanel = new JPanel();
        challengePanel.setLayout(new GridBagLayout());
        GridBagConstraints challengePanelConstraints = new GridBagConstraints();
        challengePanelConstraints.fill = GridBagConstraints.HORIZONTAL;

        JLabel challengerLabel = new JLabel("Challenge vs. " + challenger);
        challengePanelConstraints.gridx = 0;
        challengePanelConstraints.gridy = 0;
        challengePanel.add(challengerLabel, challengePanelConstraints);

        Font timerFont = new Font("timer font", Font.ROMAN_BASELINE, 20);

        JLabel challengeTimer = new JLabel("00:58");
        challengeTimer.setFont(timerFont);
        challengePanelConstraints.gridx = 2;
        challengePanelConstraints.gridy = 0;
        challengePanel.add(challengeTimer, challengePanelConstraints);

        JLabel matchScore = new JLabel("Match score: 12");
        matchScore.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        challengePanelConstraints.gridx = 1;
        challengePanelConstraints.gridy = 1;
        challengePanel.add(matchScore, challengePanelConstraints);

        Font challengeFont = new Font("challenge", Font.TRUETYPE_FONT, 40);

        JLabel currentWord = new JLabel("bottiglia");
        currentWord.setFont(challengeFont);
        challengePanelConstraints.gridx = 1;
        challengePanelConstraints.gridy = 2;
        challengePanelConstraints.insets.top = 40;
        challengePanel.add(currentWord, challengePanelConstraints);

        translationInput = new JTextField();
        translationInput.setHorizontalAlignment(JTextField.CENTER);
        translationInput.setFont(challengeFont);
        challengePanelConstraints.gridx = 0;
        challengePanelConstraints.gridy = 3;
        challengePanelConstraints.insets.top = 20;
        challengePanelConstraints.gridwidth = 3;
        challengePanel.add(translationInput, challengePanelConstraints);

        translateButton = new JButton("Translate");
        translateButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String translation = translationInput.getText();
                // completare il codice
            }
        });

        // se premo invio durante la digitazione della traduzione è come se avessi cliccato il bottone "Translate"
        translationInput.addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e){
                if(e.getKeyCode()==KeyEvent.VK_ENTER) translateButton.doClick();
            }
        });

        challengePanelConstraints.gridx = 2;
        challengePanelConstraints.gridy = 4;
        challengePanelConstraints.insets.top = 10;
        challengePanelConstraints.gridwidth = 1;
        challengePanel.add(translateButton, challengePanelConstraints);
        // CHALLENGE PANEL END

        if (username==null) {
            frame.add(loginPanel, BorderLayout.CENTER);
        }
        else if(username!=null && challenger==null) {
            frame.add(topPanel, BorderLayout.NORTH);
            frame.add(settingsPanel, BorderLayout.CENTER);
        }
        else if (username !=null && challenger!=null) {
            frame.add(challengePanel, BorderLayout.CENTER);
        }

        // frame.add(topPanel, BorderLayout.NORTH);
        // frame.add(settingsPanel, BorderLayout.CENTER);
        
        frame.setSize(400,300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        

    }

    public void setPoints(int punti) {
        this.points = punti;
        this.pointsLogged.setText(Integer.toString(punti));
    }

    
    public void setUser(String user, int p) {
        this.username = user;
        this.points = p;
    }

    /**
     * Riceve la lista degli utenti online in formato JSON e aggiorna la lista degli amici online dell'utente loggato.
     */
    public void updateOnlineFriends(String str) {
        textAreaAmiciOnline.setText("");
        String[] lista = str.split(" ");
        for (int i=0; i<lista.length; i++) {
            textAreaAmiciOnline.setText(textAreaAmiciOnline.getText() + lista[i] + "\n");
        }
    }

}