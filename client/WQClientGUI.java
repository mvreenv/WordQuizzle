package client;

import common.WQUser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

/**
 * Classe che gestisce l'interfaccia grafica del Client di WordQuizzle.
 * @author Marina Pierotti
 */

public class WQClientGUI {

    /**
     * Palette di colori per l'interfaccia.
     */
    public static final Color DARK_BLUE = Color.decode("#252A36");
    public static final Color LIGHT_BLUE = Color.decode("#425563");
    public static final Color MINT_GREEN = Color.decode("#80E0A7");
    public static final Color WHITE = Color.WHITE;

    /**
     * Frame per la costruzione della GUI del client WordQuizzle.
     */
    private JFrame frame;

    /**
     * Pannello di login coi suoi componenti.
     */
    private JPanel loginPanel;
    private JTextField usernameInput; // campo per inserire lo username
    private JPasswordField passwordInput; // campo per inserire la password
    private JButton loginButton; // se premuto tenta il login col contenuto di usernameInput e passwordInput
    private JButton registerButton; // se premuto tenta di registrare un nuovo utente con usernameInput e passwordInput

    /**
     * Pannello in alto alla finestra che mostra informazioni sullo username e il punteggio totale.
     */
    private JPanel topPanel;
    private String username; // username dell'utente loggato
    private int points; // punteggio totale dell'utente loggato
    private JLabel userLogged; // label per il nome 
    private JLabel pointsLogged; // label per i punti

    /**
     * Panello che mostra le operazioni svolgibili dall'utente dopo il login.
     */
    private JPanel settingsPanel;
    private JButton addFriendButton; // bottone per aggiungere un amico
    private JButton challengeFriendButton; // bottone per sfidare un amico
    private JButton showLeaderboardButton; // bottone per vedere la classifica coi propri amici
    private JButton showFriendListButton; // bottone per vedere la lista degli amici (anche offline)
    private JButton logoutButton; // bottone per eseguire il logout
    private JTextArea textAreaAmiciOnline;

    /**
     * Username dell'utente sfidato.
     */
    public String challenger;

    /**
     * Pannello dell'interfaccia della sfida coi suoi componenti.
     */
    private JPanel challengePanel;
    private JLabel challengerLabel; // label col nome dell'aversario
    private JLabel currentWord; // parola da tradurre nel round corrente della sfida
    private JTextField translationInput; // campo per inserire la parola tradotta
    private JButton translateButton; // bottone per inviare il contenuto di translationInput

    /**
     * Costruttore.
     */
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

        // Inizializzo la schermata di login e registrazione
        loginPanel = new JPanel();
        loginPanel.setBackground(LIGHT_BLUE);
        loginPanel.setLayout(new GridBagLayout());
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
                String user = usernameInput.getText();
                String pw = String.copyValueOf(passwordInput.getPassword());
                if(user!=null && !user.isEmpty() && !user.isBlank() && pw!=null && !pw.isEmpty() && !pw.isBlank()) {
                    int n = WQClientLink.client.login(user, pw);
                    if (n==0) { // login avvenuto con successo
                        try { Thread.sleep(500); }
                        catch (InterruptedException e ) {}
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
                        WQClientGUIPopup.showMessageDialog(frame, "Password errata.", "Errore login");
                    }
                    else if (n==-2) { // utente non registrato
                        WQClientGUIPopup.showMessageDialog(frame, "Username non riconosciuto.", "Errore login");
                    }
                    else if (n==-3) { // l'utente è già loggato 
                        WQClientGUIPopup.showMessageDialog(frame, "Utente già loggato.", "Errore login");
                    }
                    else if (n==-4) { // errore generico (server offline)
                        WQClientGUIPopup.showMessageDialog(frame, "Il Server è offline.", "Errore login");
                    }
                }
                else {
                    WQClientGUIPopup.showMessageDialog(frame, "Riempire i campi per favore.", "Errore login.");
                }
            }
        });

        registerButton = new JButton("Registrati");
        registerButton.setBackground(DARK_BLUE);
        registerButton.setForeground(WHITE);
        registerButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String user = usernameInput.getText();
                String pw = String.copyValueOf(passwordInput.getPassword());
                if(user!=null && !user.isEmpty() && !user.isBlank()) {
                    int n = WQClientLink.client.registra_utente(user, pw);
                    if (n==0) {
                        System.out.println(">> CLIENT >> Registrazione di " + user + " avvenuta con successo.");
                        WQClientGUIPopup.showMessageDialog(frame, "Registrazione avvenuta con successo.", "Esito registrazione");
                        usernameInput.setText("");
                        passwordInput.setText("");
                    }
                    else if (n==-1) {
                        System.out.println(">> CLIENT >> Username già registrato.");
                        WQClientGUIPopup.showMessageDialog(frame, "Username già registrato, registrazione non eseguita.", "Esito registrazione");
                        usernameInput.setText("");
                        passwordInput.setText("");
                    }
                    else if (n==-2) {
                        System.out.println(">> CLIENT >> Password vuota, registrazione non eseguita.");
                        WQClientGUIPopup.showMessageDialog(frame, "Password vuota, registrazione non eseguita.", "Esito registrazione"); 
                    }
                    else if (n==-3) {
                        System.out.println(">> CLIENT >> Errore di connessione al server.");
                        WQClientGUIPopup.showMessageDialog(frame, "Errore di connessione al Server.", "Esito registrazione");
                    }
                }
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
        // Fine inizializzazione schermata di login e registrazione
        
        // Inizializzo il pannello con le informazioni dell'utente
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
        // Fine inizializzazione pannello con informazioni utente

        // Inizializzo la schermata principale
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
                    try { Thread.sleep(130);}
                    catch (InterruptedException e ) { e.printStackTrace(); }
                    int n = WQClientLink.client.send("addfriend " + username + " " + friendName);
                    if (n==1) WQClientGUIPopup.showMessageDialog(frame, friendName + " aggiunto alla tua lista amici.", "Amico");
                    else if (n==0) WQClientGUIPopup.showMessageDialog(frame, "Errore formattazione dati.", "Errore amicizia.");
                    else if (n==-1) WQClientGUIPopup.showMessageDialog(frame, friendName + " non è un utente registrato.", "Errore amicizia.");
                    else if (n==-2) WQClientGUIPopup.showMessageDialog(frame, "Sei già amico di " + friendName + ".", "Errore amicizia.");
                    else if (n==-3) WQClientGUIPopup.showMessageDialog(frame, "Non puoi aggiungere te stesso come amico.", "Errore amicizia.");
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
                if (challenger==null) {
                    String friendName = JOptionPane.showInputDialog(frame, "Chi vuoi sfidare?", null);
                    int n = WQClientLink.client.send("challenge " + friendName);
                    // if (n==1) WQClientGUIPopup.showMessageDialog(frame, "Sfida inviata.", "Invio sfida.");
                    if (n==0) WQClientGUIPopup.showMessageDialog(frame, "Errore invio sfida.", "Invio sfida.");
                    challenger = friendName;
                    challengerLabel.setText("Sfida contro " + friendName);
                }
                else {
                    WQClientGUIPopup.showMessageDialog(frame, "Stai già aspettando una risposta ad una sfida.", "Errore sfida.");
                }
                
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
                if (n==0) WQClientGUIPopup.showMessageDialog(frame, "Errore classifica.", "Errore.");
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
                if (n==0) WQClientGUIPopup.showMessageDialog(frame, "Errore lista amici.", "Errore.");
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
        // Fine inizializzazione schermata principale

        // Inizializzo la schermata di sfida
        challengePanel = new JPanel();
        challengePanel.setBackground(LIGHT_BLUE);
        challengePanel.setLayout(new GridBagLayout());
        GridBagConstraints challengePanelConstraints = new GridBagConstraints();
        challengePanelConstraints.fill = GridBagConstraints.HORIZONTAL;
        challengePanelConstraints.weightx = 0.10;

        challengerLabel = new JLabel("Sfida contro " + challenger);
        challengerLabel.setForeground(WHITE);
        challengePanelConstraints.gridx = 0;
        challengePanelConstraints.gridy = 0;
        challengePanelConstraints.insets.left = 30;
        challengePanel.add(challengerLabel, challengePanelConstraints);

        Font challengeFont = new Font("challenge", Font.TRUETYPE_FONT, 40);
        currentWord = new JLabel("carico...");
        currentWord.setFont(challengeFont);
        currentWord.setForeground(WHITE);
        currentWord.setBackground(Color.GREEN);
        challengePanelConstraints.gridx = 0;
        challengePanelConstraints.gridy = 1;
        challengePanelConstraints.insets.top = 40;
        challengePanelConstraints.insets.left = 30;
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
        challengePanelConstraints.insets.left = 20;
        challengePanelConstraints.insets.right = 20;
        challengePanelConstraints.gridwidth = 3;
        challengePanel.add(translationInput, challengePanelConstraints);

        translateButton = new JButton("Traduci");
        translateButton.setMargin(new Insets(10,10,10,10));
        translateButton.setBackground(DARK_BLUE);
        translateButton.setForeground(MINT_GREEN);
        translateButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // pulisco la stringa da inviare
                String translation = translationInput.getText().toLowerCase().replaceAll(" ", "");
                
                // se ho scritto qualcosa invio la parola
                if(translation.length()!=0) WQClientLink.client.send("challengeanswer " + translation);
                // se non ho scritto niente invio il messaggio per dire che non ho risposto
                else WQClientLink.client.send("challengeanswer -1");

                // pulisco il campo di input
                translationInput.setText("");
            }
        });

        translationInput.addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e){
                if(e.getKeyCode()==KeyEvent.VK_ENTER) translateButton.doClick();
            }
        });

        challengePanelConstraints.gridx = 1;
        challengePanelConstraints.gridy = 3;
        challengePanelConstraints.insets.top = 10;
        challengePanelConstraints.insets.left = 0;
        challengePanelConstraints.insets.right = 20;
        challengePanelConstraints.gridwidth = 1;
        challengePanel.getLayout().preferredLayoutSize(frame);
        challengePanel.add(translateButton, challengePanelConstraints);
        // Fine inizializzazione della schermata di sfida

        // Panello visibile all'avvio del Client
        frame.add(loginPanel, BorderLayout.CENTER);

        frame.setSize(400,300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public int challengeDialog(String sfidante) {
        int result = WQClientGUIPopup.showChallengeDialog(frame, sfidante);
        if (result==JOptionPane.YES_OPTION); {
            this.challenger = sfidante;
            this.challengerLabel.setText("Sfida contro " + this.challenger);
        }
        return result;
    }

    /**
     * Rende visibile la schermata di sfida.
     */
    public void startChallenge() {
        frame.invalidate();
        frame.getContentPane().removeAll();
        frame.add(challengePanel);
        frame.revalidate();
        frame.repaint();
    }

    /**
     * Rende visibile la schermata principale quando termina una sfida.
     */
    public void endChallenge () {
        translationInput.setText("");
        currentWord.setText("carico...");
        currentWord.setForeground(WHITE);
        frame.invalidate();
        frame.getContentPane().removeAll();
        if(this.username!=null) {
            WQClientLink.client.send("online");
            WQClientLink.client.send("userpoints");
            frame.add(topPanel, BorderLayout.NORTH);
            frame.add(settingsPanel, BorderLayout.CENTER);
        }
        else {
            frame.add(loginPanel);
        }
        frame.revalidate();
        frame.repaint();
    }

    /**
     * Restituisce la finestra principale della GUI.
     * @return La finestra della GUI.
     */
    public JFrame getFrame() {
        return this.frame;
    }

    /**
     * Imposta la parola da tradurre per il round corrente della sfida e aggiorna la finestra.
     * @param parola La parola da tradurre.
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
     * Imposta il valore dei punti.
     * @param punti I punti da impostare.
     */
    public void setPoints(int punti) {
        this.points = punti;
        this.pointsLogged.setText("Punti: " + Integer.toString(punti));
    }

    /**
     * Imposta username e punti dell'utente collegato con questo client.
     * @param user Lo username che si vuole impostare.
     * @param p I punti che si vogliono impostare.
     */    
    public void setUser(String user, int p) {
        this.username = user;
        this.points = p;
        this.userLogged.setText("Utente: " + user);
        this.pointsLogged.setText("Punti: " + Integer.toString(p));
    }

    /**
     * Aggiorna la lista degli amici online.
     * @param friends Nomi degli amici online separati da uno spazio (" ").
     */
    public void updateOnlineFriends(String friends) {
        textAreaAmiciOnline.setText("");
        String[] lista = friends.split(" ");
        for (int i=0; i<lista.length; i++) {
            textAreaAmiciOnline.append(lista[i] + "\n");
        }
    }

    /**
     * Mostra una finestra di dialogo con il risultato della sfida.
     * @param outcome Riceve "challengewon" se la sfida è stata vinta, "challengelost" se è stata persa, o "challengetie" se c'è stato un pareggio.
     * @param newPoints Punteggio aggiornato dell'utente ricevuto dal Server.
     */
    public void challengeResultDialog(String outcome, int newPoints) {
        this.challenger = null;
        // vittoria
        if (outcome=="challengewon") {
            WQClientGUIPopup.showMessageDialog(frame, "Hai vinto la sfida!\nHai guadagnato " + (newPoints - this.points) + " punti.\nAdesso sei a " + newPoints + " punti.", "Risultato sfida.");
        }
        // sconfitta
        else if (outcome=="challengelost") {
            WQClientGUIPopup.showMessageDialog(frame, "Hai perso la sfida!\nHai guadagnato " + (newPoints - this.points) + " punti.\nAdesso sei a " + newPoints + " punti.", "Risultato sfida.");
        }
        // pareggio
        else if (outcome=="challengetie") {
            WQClientGUIPopup.showMessageDialog(frame, "Hai pareggiato!\nHai guadagnato " + (newPoints - this.points) + " punti.\nAdesso sei a " + newPoints + " punti.", "Risultato sfida.");
        }
    }

    /**
     * Apre una finestra aggiuntiva in cui viene mostrata la classifica degli amici dell'utente.
     * @param classificaJson La classifica ricevuta dal server in formato JSON.
     */
    public void mostraClassifica(String classificaJson) {
        Gson gson2 = new Gson();
        JsonReader reader2 = new JsonReader(new StringReader(classificaJson));
        reader2.setLenient(true);
        Type type2 = new TypeToken<ArrayList<WQUser>>(){}.getType();
        ArrayList<WQUser> classificaArray = gson2.fromJson(reader2, type2);

        // se l'array ottenuto non è vuoto
        if (classificaArray!=null) {

            JFrame frameClassifica = new JFrame("Classifica amici di " + this.username);
            JPanel panelClassifica = new JPanel();
            panelClassifica.setBackground(LIGHT_BLUE);
            JLabel labelClassifica = new JLabel("Classifica amici di " + this.username);
            labelClassifica.setForeground(MINT_GREEN);

            JTextArea textAreaClassifica = new JTextArea();
            textAreaClassifica.setBackground(DARK_BLUE);
            textAreaClassifica.setForeground(WHITE);
            textAreaClassifica.setMargin(new Insets(10,10,0,10));
            textAreaClassifica.setEditable(false);
                int i = 1;
                for(WQUser amico : classificaArray) {
                    textAreaClassifica.setText(textAreaClassifica.getText() + i + ") " + amico.username + " - " + amico.points + " punti\n");
                    i++;
                }
            panelClassifica.setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();

            constraints.insets.left = 10;
            constraints.insets.right = 10;
            constraints.insets.top = 10;
            constraints.insets.bottom = 10;

            constraints.gridx = 0;
            constraints.gridy = 0;
            panelClassifica.add(labelClassifica, constraints);

            constraints.gridx = 0;
            constraints.gridy = 1;
            panelClassifica.add(textAreaClassifica, constraints);
            
            frameClassifica.add(panelClassifica);
            frameClassifica.pack();
            frameClassifica.setLocation(frame.getX() + frame.getWidth()/2 - frameClassifica.getWidth()/2, frame.getY() + frame.getHeight()/2 - frameClassifica.getHeight()/2);
            frameClassifica.setVisible(true);
        }
        else {
            WQClientGUIPopup.showMessageDialog(frame, "Non hai ancora amici.", "Classifica amici di " + this.username);
            // JOptionPane.showMessageDialog(frame, "Non hai ancora amici.", "Classifica amici di " + this.username, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Apre una finestra aggiuntiva in cui viene mostrata la lista completa degli amici (anche offline) dell'utente.
     * @param listaJson La lista degli amici ricevuta dal server in formato JSON.
     */
    public void listaAmici(String listaJson) {
        Gson gson1 = new Gson();
        JsonReader reader1 = new JsonReader(new StringReader(listaJson));
        reader1.setLenient(true);
        Type type1 = new TypeToken<ArrayList<WQUser>>(){}.getType();
        ArrayList<WQUser> listaArray = gson1.fromJson(reader1, type1);

        // se l'array ottenuto non è vuoto
        if(listaArray!=null) {
            JFrame frameLista = new JFrame("Lista amici");
            JPanel panelLista = new JPanel();
            panelLista.setBackground(LIGHT_BLUE);

            JLabel labelLista = new JLabel("Lista amici di " + this.username);
            labelLista.setForeground(MINT_GREEN);

            JTextArea textAreaLista = new JTextArea();
            textAreaLista.setMargin(new Insets(10,10,0,10));
            textAreaLista.setBackground(DARK_BLUE);
            textAreaLista.setForeground(WHITE);
            textAreaLista.setEditable(false);
            for(WQUser amico : listaArray) {
                textAreaLista.setText(textAreaLista.getText() + "- " + amico.username + "\n");
            }

            panelLista.setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();

            constraints.insets.left = 10;
            constraints.insets.right = 10;
            constraints.insets.top = 10;
            constraints.insets.bottom = 10;

            constraints.gridx = 0;
            constraints.gridy = 0;
            panelLista.add(labelLista, constraints);
            constraints.gridx = 0;
            constraints.gridy = 1;
            panelLista.add(textAreaLista, constraints);
            frameLista.add(panelLista);
            frameLista.pack();
            frameLista.setLocation(frame.getX() + frame.getWidth()/2 - frameLista.getWidth()/2, frame.getY() + frame.getHeight()/2 - frameLista.getHeight()/2);
            frameLista.setVisible(true);
        }

        else {
            WQClientGUIPopup.showMessageDialog(frame, "Non hai ancora amici.", "Classifica amici di " + this.username);
            // JOptionPane.showMessageDialog(frame, "Non hai ancora amici.", "Lista amici di " + this.username, JOptionPane.INFORMATION_MESSAGE);
        } 
        
    }

}