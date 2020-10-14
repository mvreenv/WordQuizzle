package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


/**
 * Classe che gestisce l'interfaccia grafica del client di WordQuizzle.
 * @author Marina Pierotti
 */

public class WQClientGUI extends JFrame implements ActionListener, DocumentListener {

    /**
     * Valore impostato di default.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Pannello di login coi suoi componenti
     */
    private JPanel loginPanel;
    private JTextField usernameInput; // campo per inserire lo username
    private JPasswordField passwordInput; // campo per inserire la password
    private JButton loginButton; // se premuto tenta il login col contenuto di usernameInput e passwordInput
    private JButton registerButton; // se premuto tenta di registrare un nuovo utente con usernameInput e passwordInput
    
    /**
     * Pannello in alto alla finestra che mostra informazioni sullo username e il punteggio
     */
    private JPanel topPanel;
    private String username; // username dell'utente loggato
    private int points; // punteggio totale dell'utente loggato

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
    private JButton showScoreButton; // bottone per vedere il proprio punteggio
    private JButton showLeaderboardButton; // bottone per vedere la classifica coi propri amici
    private JButton logoutButton; // bottone per eseguire il logout

    /**
     * Pannello dell'interfaccia della sfida
     */
    private JPanel challengePanel;
    private JTextField translationInput; // campo per inserire la parola tradotta
    private JButton translateButton; // bottone per inviare il contenuto di translationInput

    public WQClientGUI() {

        super();

        // username = "Marina";
        // points = 420;
        // challenger = "Vale";

        this.setTitle("WordQuizzle");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setLayout(new BorderLayout());

        // LOGIN PANEL START
        loginPanel = new JPanel();
        loginPanel.setLayout(new GridBagLayout()); // GridLayout(int rows, int cols, int hgap, int vgap)
        GridBagConstraints loginPanelConstraints = new GridBagConstraints();

        JLabel usernameLabel = new JLabel("Username");
        usernameInput = new JTextField();
        usernameInput.getDocument().addDocumentListener(this);
        usernameLabel.setLabelFor(usernameInput);

        JLabel passwordLabel = new JLabel("Password");
        passwordInput = new JPasswordField();
        passwordInput.getDocument().addDocumentListener(this);
        passwordLabel.setLabelFor(passwordInput);

        loginButton = new JButton("Login");
        loginButton.addActionListener(this);

        registerButton = new JButton("Register");
        registerButton.addActionListener(this);

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

        JLabel userLogged = new JLabel("Player: " + username);
        JLabel pointsLogged = new JLabel("Total score: " + points);

        topPanel.add(BorderLayout.WEST, userLogged);
        topPanel.add(BorderLayout.EAST, pointsLogged);
        // TOP PANEL END

        // SETTINGS PANEL START
        settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridBagLayout());
        GridBagConstraints settingsPanelConstraints = new GridBagConstraints();

        addFriendButton = new JButton("Add Friend");
        addFriendButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String friendName = JOptionPane.showInputDialog(settingsPanel,
                        "Who do you want to add as a friend?", null);
            }
        });
        settingsPanelConstraints.gridx = 0;
        settingsPanelConstraints.gridy = 0;
        settingsPanelConstraints.insets.bottom = 10;
        settingsPanel.add(addFriendButton, settingsPanelConstraints);

        challengeFriendButton = new JButton("Challenge Friend");
        settingsPanelConstraints.gridx = 0;
        settingsPanelConstraints.gridy = 1;
        settingsPanel.add(challengeFriendButton, settingsPanelConstraints);

        showScoreButton = new JButton("Show my score");
        settingsPanelConstraints.gridx = 0;
        settingsPanelConstraints.gridy = 2;
        settingsPanel.add(showScoreButton, settingsPanelConstraints);

        showLeaderboardButton = new JButton("Show Leaderboard");
        settingsPanelConstraints.gridx = 0;
        settingsPanelConstraints.gridy = 3;
        settingsPanel.add(showLeaderboardButton, settingsPanelConstraints);
        
        logoutButton = new JButton("Logout");
        settingsPanelConstraints.gridx = 0;
        settingsPanelConstraints.gridy = 4;
        settingsPanelConstraints.insets.top = 0;
        settingsPanelConstraints.insets.bottom = 0;
        settingsPanel.add(logoutButton, settingsPanelConstraints);

        JTextArea settingsTextArea = new JTextArea("scrivi qui");
        settingsTextArea.setAutoscrolls(true);
        settingsTextArea.setEditable(true);
        settingsTextArea.setLineWrap(true);
        JScrollPane settingsScrollPanel = new JScrollPane(settingsTextArea);
        settingsScrollPanel.setPreferredSize(new Dimension(150,170));
        settingsPanelConstraints.gridx = 1;
        settingsPanelConstraints.gridy = 0;
        settingsPanelConstraints.gridheight = 5;
        settingsPanelConstraints.insets.left = 10;
        settingsPanel.getLayout().preferredLayoutSize(this);
        settingsPanel.add(settingsScrollPanel, settingsPanelConstraints);
        
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
        translationInput.getDocument().addDocumentListener(this);
        translationInput.setHorizontalAlignment(JTextField.CENTER);
        translationInput.setFont(challengeFont);
        challengePanelConstraints.gridx = 0;
        challengePanelConstraints.gridy = 3;
        challengePanelConstraints.insets.top = 20;
        challengePanelConstraints.gridwidth = 3;
        challengePanel.add(translationInput, challengePanelConstraints);

        translateButton = new JButton("Translate");
        translateButton.addActionListener(this);
        challengePanelConstraints.gridx = 2;
        challengePanelConstraints.gridy = 4;
        challengePanelConstraints.insets.top = 10;
        challengePanelConstraints.gridwidth = 1;
        challengePanel.add(translateButton, challengePanelConstraints);
        // CHALLENGE PANEL END

        
        if(username==null) {    
            this.add(loginPanel, BorderLayout.CENTER);
        } 
        else { 
            this.add(topPanel, BorderLayout.NORTH);
            this.add(settingsPanel, BorderLayout.CENTER);
        } 

        if(challenger!=null){
            this.add(challengePanel, BorderLayout.CENTER);
        }

        this.setSize(400,300);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

    }

    /**
     * @return contenuto del campo di inserimento username
     */
    public String getUsername(){
        return usernameInput.getText();
    }

    /**
     * @return contenuto del campo di inserimento password
     */
    public char[] getPassword(){
        return passwordInput.getPassword();
    }

    /**
     * Metodo da implementare per ActionListener.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        
        if(e.getSource().equals(addFriendButton)){

        }

        if(e.getSource().equals(loginButton)){
            loginPanel.setBackground(Color.red);
        }
        
        if(e.getSource().equals(registerButton)){    
            loginPanel.setBackground(Color.blue);
        }
        
        if(e.getSource().equals(translateButton) && translationInput.getText().equals("bottle")){
            challengePanel.setBackground(Color.green);
        }

    }

    /**
     * Metodo da implementare per DocumentListener.
     */
    @Override
    public void insertUpdate(DocumentEvent e) {

    }

    /**
     * Metodo da implementare per DocumentListener.
     */
    @Override
    public void removeUpdate(DocumentEvent e) {

    }

    /**
     * Metodo da implementare per DocumentListener.
     */
    @Override
    public void changedUpdate(DocumentEvent e) {

    }

}