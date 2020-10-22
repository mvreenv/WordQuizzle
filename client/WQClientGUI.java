package client;

import common.WQUser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
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
import javax.swing.JComponent;
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
     * Palette di colori per l'interfaccia.
     */
    private static final Color DARK_BLUE = Color.decode("#252A36");
    private static final Color LIGHT_BLUE = Color.decode("#425563");
    private static final Color MINT_GREEN = Color.decode("#80E0A7");
    private static final Color WHITE = Color.WHITE;

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
    private JLabel challengerLabel; // label col nome dell'aversario
    private JLabel currentWord; // parola da tradurre nel round corrente della sfida
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
			public void windowClosing(WindowEvent e) { 
                if (username!=null) WQClientLink.client.logout(username); 
            }

			@Override
            public void windowClosed(WindowEvent e) { }

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
        frame.setBackground(LIGHT_BLUE);

        // LOGIN PANEL START
        loginPanel = new JPanel();
        loginPanel.setBackground(LIGHT_BLUE);
        loginPanel.setLayout(new GridBagLayout()); // GridLayout(int rows, int cols, int hgap, int vgap)
        GridBagConstraints loginPanelConstraints = new GridBagConstraints();

        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setForeground(WHITE);
        usernameInput = new JTextField();
        usernameInput.setBackground(DARK_BLUE);
        usernameInput.setForeground(WHITE);
        usernameInput.setCaretColor(MINT_GREEN);
        usernameLabel.setLabelFor(usernameInput);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setForeground(WHITE);
        passwordInput = new JPasswordField();
        passwordInput.setBackground(DARK_BLUE);
        passwordInput.setForeground(WHITE);
        passwordInput.setCaretColor(MINT_GREEN);
        passwordInput.addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e){
                if(e.getKeyCode()==KeyEvent.VK_ENTER) loginButton.doClick();
            }
        });
        passwordLabel.setLabelFor(passwordInput);

        loginButton = new JButton("Login");
        loginButton.setBackground(DARK_BLUE);
        loginButton.setForeground(MINT_GREEN);
        loginButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String pw = String.copyValueOf(passwordInput.getPassword());
                int n = WQClientLink.client.login(usernameInput.getText(), pw);
                if (n==0) { // login avvenuto con successo
                    JOptionPane.showMessageDialog(frame, "Login avvenuto con successo!");
                    WQClientLink.client.send("online"); // chiedo al server la lista aggiornata degli utenti online
                    usernameInput.setText(""); // svuoto l'input username
                    passwordInput.setText(""); // svuoto l'input password
                    frame.invalidate();
                    frame.getContentPane().removeAll();
                    frame.add(topPanel, BorderLayout.NORTH);
                    frame.add(settingsPanel, BorderLayout.CENTER);
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
        registerButton.setBackground(DARK_BLUE);
        registerButton.setForeground(WHITE);
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

        Font logoFont = new Font("WQlogo", Font.ROMAN_BASELINE, 40);
        JLabel logo = new JLabel("WordQuizzle");
        logo.setForeground(MINT_GREEN);
        logo.setFont(logoFont);
        loginPanelConstraints.gridx = 0;
        loginPanelConstraints.gridy = 0;
        loginPanelConstraints.gridwidth = 2;
        loginPanelConstraints.insets.bottom = 20;
        loginPanel.add(logo, loginPanelConstraints);

        loginPanelConstraints.gridx = 0;
        loginPanelConstraints.gridy = 1;
        loginPanelConstraints.gridwidth = 1;
        loginPanelConstraints.insets.bottom = 0;
        loginPanel.add(usernameLabel, loginPanelConstraints);

        loginPanelConstraints.gridx = 1;
        loginPanelConstraints.gridy = 1;
        loginPanel.add(usernameInput, loginPanelConstraints);

        loginPanelConstraints.insets.top = 10;

        loginPanelConstraints.gridx = 0;
        loginPanelConstraints.gridy = 2;
        loginPanel.add(passwordLabel, loginPanelConstraints);

        loginPanelConstraints.gridx = 1;
        loginPanelConstraints.gridy = 2;
        loginPanel.add(passwordInput, loginPanelConstraints);

        loginPanelConstraints.insets.top = 20;
        loginPanelConstraints.insets.right = 20;

        loginPanelConstraints.gridx = 0;
        loginPanelConstraints.gridy = 3;
        loginPanel.add(registerButton, loginPanelConstraints);

        loginPanelConstraints.insets.left = 20;

        loginPanelConstraints.gridx = 1;
        loginPanelConstraints.gridy = 3;
        loginPanel.add(loginButton, loginPanelConstraints);

        // LOGIN PANEL END
        
        // TOP PANEL START
        topPanel = new JPanel();
        topPanel.setLayout(new GridBagLayout());
        GridBagConstraints topPanelConstraints = new GridBagConstraints();

        userLogged = new JLabel("Utente: " + username);
        pointsLogged = new JLabel("Punti: " + Integer.toString(points));

        topPanel.setBackground(DARK_BLUE);
        userLogged.setForeground(MINT_GREEN);
        pointsLogged.setForeground(MINT_GREEN);

        topPanelConstraints.gridx = 0;
        topPanelConstraints.gridy = 0;
        topPanelConstraints.insets.top = 10;
        topPanelConstraints.insets.bottom = 10;
        topPanelConstraints.insets.left = 10;
        topPanelConstraints.insets.right = 30;
        topPanel.add(userLogged, topPanelConstraints);
        
        topPanelConstraints.gridx = 1;
        topPanelConstraints.gridy = 0;
        topPanelConstraints.insets.left = 30;
        topPanelConstraints.insets.right = 10;
        topPanel.add(pointsLogged, topPanelConstraints);
        // TOP PANEL END

        // SETTINGS PANEL START
        settingsPanel = new JPanel();
        settingsPanel.setBackground(LIGHT_BLUE);
        settingsPanel.setLayout(new GridBagLayout());
        GridBagConstraints settingsPanelConstraints = new GridBagConstraints();

        addFriendButton = new JButton("Aggiungi amico");
        addFriendButton.setBackground(DARK_BLUE);
        addFriendButton.setForeground(WHITE);
        addFriendButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String friendName = JOptionPane.showInputDialog(frame, "Chi vuoi aggiungere come amico?", null);
                if (friendName!=null && !friendName.isBlank() && !friendName.isEmpty()) {
                    int n = WQClientLink.client.send("addfriend " + username + " " + friendName);
                    if (n==1) JOptionPane.showMessageDialog(frame, friendName + " aggiunto alla tua lista amici.", "Amico", JOptionPane.INFORMATION_MESSAGE);
                    else if (n==0) JOptionPane.showMessageDialog(frame, "Errore formattazione dati.", "Errore amicizia.", JOptionPane.ERROR_MESSAGE);
                    else if (n==-1) JOptionPane.showMessageDialog(frame, "Uno dei due utenti non esiste.", "Errore amicizia.", JOptionPane.ERROR_MESSAGE);
                    else if (n==-2) JOptionPane.showMessageDialog(frame, "Sei già amico di " + friendName + ".", "Errore amicizia.", JOptionPane.ERROR_MESSAGE);
                    else if (n==-3) JOptionPane.showMessageDialog(frame, "Non puoi aggiungere te stesso come amico.", "Errore amicizia.", JOptionPane.ERROR_MESSAGE);
                }
                
            }
        });
        settingsPanelConstraints.gridx = 0;
        settingsPanelConstraints.gridy = 1;
        settingsPanelConstraints.insets.bottom = 10;
        settingsPanel.add(addFriendButton, settingsPanelConstraints);

        challengeFriendButton = new JButton("Sfida amico");
        challengeFriendButton.setBackground(DARK_BLUE);
        challengeFriendButton.setForeground(WHITE);
        challengeFriendButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String friendName = JOptionPane.showInputDialog(frame, "Chi vuoi sfidare?", null);
                int n = WQClientLink.client.send("challenge " + friendName);
                // if (n==1) JOptionPane.showMessageDialog(frame, "Sfida inviata.", "Invio sfida.", JOptionPane.INFORMATION_MESSAGE);
                if (n==0) JOptionPane.showMessageDialog(frame, "Errore invio sfida.", "Invio sfida.", JOptionPane.ERROR_MESSAGE);
                challenger = friendName;
                challengerLabel.setText("Sfida contro " + friendName);
                
            }
        });
        settingsPanelConstraints.gridx = 0;
        settingsPanelConstraints.gridy = 2;
        settingsPanel.add(challengeFriendButton, settingsPanelConstraints);

        showLeaderboardButton = new JButton("Mostra classifica");
        showLeaderboardButton.setBackground(DARK_BLUE);
        showLeaderboardButton.setForeground(WHITE);
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
        showFriendListButton.setBackground(DARK_BLUE);
        showFriendListButton.setForeground(WHITE);
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
        
        logoutButton = new JButton("Logout");
        logoutButton.setBackground(DARK_BLUE);
        logoutButton.setForeground(WHITE);
        logoutButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                WQClientLink.client.logout(WQClientLink.gui.username);
                frame.invalidate();
                frame.remove(settingsPanel);
                frame.remove(topPanel);
                frame.add(loginPanel, BorderLayout.CENTER);
                frame.revalidate();
                frame.repaint();

            }
        });
        settingsPanelConstraints.gridx = 0;
        settingsPanelConstraints.gridy = 5;
        settingsPanelConstraints.insets.top = 0;
        settingsPanelConstraints.insets.bottom = 0;
        settingsPanel.add(logoutButton, settingsPanelConstraints);

        JLabel labelAmiciOnline = new JLabel("Amici online");
        labelAmiciOnline.setForeground(WHITE);
        settingsPanelConstraints.gridx = 1;
        settingsPanelConstraints.gridy = 0;
        settingsPanelConstraints.insets.left = 50;
        settingsPanelConstraints.insets.bottom = 10;
        settingsPanelConstraints.insets.top = 10;
        settingsPanel.add(labelAmiciOnline, settingsPanelConstraints);

        textAreaAmiciOnline = new JTextArea();
        textAreaAmiciOnline.setBackground(DARK_BLUE);
        textAreaAmiciOnline.setForeground(MINT_GREEN);
        textAreaAmiciOnline.setMargin(new Insets(10,10,10,10));
        textAreaAmiciOnline.setEditable(false);
        textAreaAmiciOnline.setPreferredSize(new Dimension(100, 170));
        settingsPanelConstraints.gridx = 1;
        settingsPanelConstraints.gridy = 1;
        settingsPanelConstraints.gridheight = 6;
        settingsPanelConstraints.insets.left = 50;
        settingsPanelConstraints.insets.top = 0;
        settingsPanel.getLayout().preferredLayoutSize(frame);
        settingsPanel.add(textAreaAmiciOnline, settingsPanelConstraints);
        settingsPanelConstraints.insets.top = 20;
        
        //SETTIMGS PANEL END

        // CHALLENGE PANEL START
        challengePanel = new JPanel();
        challengePanel.setBackground(LIGHT_BLUE);
        challengePanel.setLayout(new GridBagLayout());
        GridBagConstraints challengePanelConstraints = new GridBagConstraints();
        challengePanelConstraints.fill = GridBagConstraints.HORIZONTAL;

        challengerLabel = new JLabel("Sfida contro " + challenger);
        challengerLabel.setForeground(WHITE);
        challengePanelConstraints.gridx = 0;
        challengePanelConstraints.gridy = 0;
        challengePanel.add(challengerLabel, challengePanelConstraints);

        Font challengeFont = new Font("challenge", Font.TRUETYPE_FONT, 40);
        currentWord = new JLabel("carico...");
        currentWord.setFont(challengeFont);
        currentWord.setForeground(WHITE);
        challengePanelConstraints.gridx = 0;
        challengePanelConstraints.gridy = 1;
        challengePanelConstraints.gridwidth = 3;
        challengePanelConstraints.insets.top = 40;
        challengePanel.add(currentWord, challengePanelConstraints);

        translationInput = new JTextField();
        translationInput.setHorizontalAlignment(JTextField.CENTER);
        translationInput.setFont(challengeFont);
        translationInput.setBackground(DARK_BLUE);
        translationInput.setForeground(WHITE);
        translationInput.setCaretColor(MINT_GREEN);
        translationInput.setSize(300, translationInput.getHeight());
        challengePanelConstraints.gridx = 0;
        challengePanelConstraints.gridy = 2;
        challengePanelConstraints.insets.top = 20;
        challengePanelConstraints.gridwidth = 3;
        challengePanel.add(translationInput, challengePanelConstraints);

        translateButton = new JButton("Traduci");
        translateButton.setBackground(DARK_BLUE);
        translateButton.setForeground(MINT_GREEN);
        translateButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {

                // pulisco la stringa da inviare
                String translation = translationInput.getText().toLowerCase().replaceAll(" ", "");
                
                // invio la risposta
                if(translation.length()!=0) WQClientLink.client.send("challengeanswer " + translation);
                else WQClientLink.client.send("challengeanswer -1");

                // pulisco il campo di input
                translationInput.setText("");

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
        challengePanelConstraints.gridy = 3;
        challengePanelConstraints.insets.top = 10;
        challengePanelConstraints.gridwidth = 1;
        challengePanel.add(translateButton, challengePanelConstraints);
        // CHALLENGE PANEL END

        // if (username==null) {
            frame.add(loginPanel, BorderLayout.CENTER);
        // }
        // else if(username!=null && challenger==null) {
        //     frame.add(topPanel, BorderLayout.NORTH);
        //     frame.add(settingsPanel, BorderLayout.CENTER);
        // }
        // else if (username !=null && challenger!=null) {
        //     frame.add(challengePanel, BorderLayout.CENTER);
        // }

        
        frame.setSize(400,300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }

    public int challengeDialog(String sfidante) {
        int result = JOptionPane.showConfirmDialog(frame, sfidante + " ti sta sfidando!", "Sfida", JOptionPane.YES_NO_OPTION);
        this.challenger = sfidante;
        this.challengerLabel.setText("Sfida contro " + this.challenger);
        return result;
        // int result;
        // JFrame challengeDialogFrame = new JFrame("Sfida");
        // JLabel challenger = new JLabel(sfidante + "ti sta sfidando!");
        // JButton acceptButton = new JButton("Accetta");
        // acceptButton.addActionListener(new java.awt.event.ActionListener() {
        //     @Override
        //     public void actionPerformed(java.awt.event.ActionEvent evt) {
        //         result = JOptionPane.YES_OPTION;
        //     }
        // });
    }

    public void startChallenge() {
        // matchPoints = 0;
        frame.invalidate();
        frame.getContentPane().removeAll();
        frame.add(challengePanel);
        frame.revalidate();
        frame.repaint();
    }

    public void endChallenge () {
        translationInput.setText("");
        currentWord.setText("carico...");
        currentWord.setForeground(WHITE);
        frame.invalidate();
        frame.getContentPane().removeAll();
        if(this.username!=null) {
            WQClientLink.client.send("online");
            WQClientLink.client.send("userpoints");
            frame.add(topPanel);
            frame.add(settingsPanel);
        }
        else {
            frame.add(loginPanel);
        }
        frame.revalidate();
        frame.repaint();
    }

    /**
     * Imposta il label contenente la parola da tradurre per il round corrente della sfida.
     * @param parola la parola da tradurre
     */
    public void setCurrentWord(String parola) {
        frame.invalidate();
        this.translationInput.setText("");
        this.currentWord.setText(parola);
        this.currentWord.setForeground(MINT_GREEN);
        frame.revalidate();
        frame.repaint();
    }

    /**
     * Aggiorna il valore dei punti.
     * @param punti i punti
     */
    public void setPoints(int punti) {
        this.points = punti;
        this.pointsLogged.setText("Punti: " + Integer.toString(punti));
    }

    /**
     * Imposta username e punti dell'utente collegato con questo client.
     * @param user username
     * @param p punti accumulati fino ad ora
     */    
    public void setUser(String user, int p) {
        this.username = user;
        this.points = p;
        this.userLogged.setText("Utente: " + user);
        this.pointsLogged.setText("Punti: " + Integer.toString(p));
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

    public void challengeResultDialog(String message) {
        JOptionPane.showMessageDialog(frame, message, "Risultato sfida.", JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostraClassifica(String classificaJson) {
        Gson gson2 = new Gson();
        JsonReader reader2 = new JsonReader(new StringReader(classificaJson));
        reader2.setLenient(true);
        Type type2 = new TypeToken<ArrayList<WQUser>>(){}.getType();
        ArrayList<WQUser> classificaArray = gson2.fromJson(reader2, type2);

        JFrame frameClassifica = new JFrame("Classifica amici di " + this.username);
        JPanel panelClassifica = new JPanel();
        JLabel labelClassifica = new JLabel("Classifica amici di " + this.username);

        JTextArea textAreaClassifica = new JTextArea();
        textAreaClassifica.setMargin(new Insets(10,10,10,10));
        textAreaClassifica.setEditable(false);
        int i = 1;
        for(WQUser amico : classificaArray) {
            textAreaClassifica.setText(textAreaClassifica.getText() + i + ") " + amico.username + " - " + amico.points + "punti\n");
            i++;
        }

        panelClassifica.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;
        panelClassifica.add(labelClassifica, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        panelClassifica.add(textAreaClassifica, constraints);
        
        frameClassifica.add(panelClassifica);
        frameClassifica.pack();
        frameClassifica.setLocationRelativeTo(null);
        frameClassifica.setVisible(true);
    }

    public void listaAmici(String listaJson) {
        Gson gson1 = new Gson();
        JsonReader reader1 = new JsonReader(new StringReader(listaJson));
        reader1.setLenient(true);
        Type type1 = new TypeToken<ArrayList<WQUser>>(){}.getType();
        ArrayList<WQUser> listaArray = gson1.fromJson(reader1, type1);

        JFrame frameLista = new JFrame("Lista amici");
        JPanel panelLista = new JPanel();
        JTextArea textAreaLista = new JTextArea();
        textAreaLista.setEditable(false);
        for(WQUser amico : listaArray) {
            textAreaLista.setText(textAreaLista.getText() + amico.username + "\n");
        }
        panelLista.add(new JLabel("Lista amici di " + this.username));
        panelLista.add(textAreaLista);
        frameLista.add(panelLista);
        frameLista.pack();
        frameLista.setLocationRelativeTo(null);
        frameLista.setVisible(true);
    }

}