package client;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class WQClientGUIPopup {

    public static final int CHALLENGE_INPUT = 19;
    public static final int ADDFRIEND_INPUT = 29;

    private static int result = JOptionPane.DEFAULT_OPTION;
    private static String input;

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

    public static String showInputDialog(JFrame parent, int inputType) {

        input = "";

        JFrame inputFrame = new JFrame();
        inputFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        inputFrame.setBackground(WQClientGUI.DARK_BLUE);

        JLabel inputLabel = new JLabel();
        inputLabel.setForeground(WQClientGUI.WHITE);

        JTextField inputField = new JTextField();
        inputField.setBackground(WQClientGUI.DARK_BLUE);
        inputField.setForeground(WQClientGUI.WHITE);
        inputField.setCaretColor(WQClientGUI.MINT_GREEN);

        JButton okButton = new JButton();
        okButton.setBackground(WQClientGUI.DARK_BLUE);
        okButton.setForeground(WQClientGUI.MINT_GREEN);

        okButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                input = inputField.getText();
                inputFrame.setVisible(false);
            }
        });

        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e){
                if(e.getKeyCode()==KeyEvent.VK_ENTER) okButton.doClick();
            }
        });

        if (inputType==CHALLENGE_INPUT) {
            inputFrame.setTitle("Sfida amico");
            inputLabel.setText("Chi vuoi sfidare?");
            okButton.setText("Sfida");
        }
        else if (inputType==ADDFRIEND_INPUT) {
            inputFrame.setTitle("Aggiungi amico");
            inputLabel.setText("Chi vuoi aggiungere come amico?");
            okButton.setText("Aggiungi");
        }
        else {
            System.out.println("Errore");
            return null; // se scrivo bene le chiamate del metodo non succede mai
        }

        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(WQClientGUI.LIGHT_BLUE);
        inputPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        
        constraints.insets.left = 10;
        constraints.insets.right = 10;
        constraints.insets.top = 10;
        constraints.insets.bottom = 0;

        constraints.gridx = 0;
        constraints.gridy = 0;
        inputPanel.add(inputLabel, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(inputField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.insets.bottom = 10;
        constraints.anchor = GridBagConstraints.ABOVE_BASELINE_TRAILING;
        constraints.fill = GridBagConstraints.NONE;
        inputPanel.add(okButton, constraints);

        inputFrame.add(inputPanel);
        inputFrame.pack();
        if (parent!=null) {
            inputFrame.setLocation(parent.getX() + parent.getWidth()/2 - inputFrame.getWidth()/2, parent.getY() + parent.getHeight()/2 - inputFrame.getHeight()/2);
        }
        else {
            inputFrame.setLocationRelativeTo(null);
        }
        inputFrame.setVisible(true);

        
        Timer timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(!inputFrame.isVisible()) {
                    input = inputField.getText();
                    // inputFrame.setVisible(false);
                    inputFrame.dispose();
                }
            }
        });
        timer.setRepeats(true);
        timer.start();

        // do {
        //     try { Thread.sleep(500); }
        //     catch (InterruptedException e) { System.out.println("Errore thread."); }
        //     // inputFrame.invalidate();
        //     // inputFrame.revalidate();
        //     // inputFrame.repaint();
        // // } while (inputFrame.isVisible());

        // while(inputFrame.isVisible()) {
        //     inputFrame.invalidate();
        //     inputFrame.revalidate();
        //     inputFrame.repaint();
        // }

        System.out.println(input);
        return input;
        // return null;

    }

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

        do {
            try { Thread.sleep(500);} 
            catch (InterruptedException e) {}
        } while (challengeFrame.isVisible());

        // challengeFrame.dispose();
        return result;

    }

    public static void main(String args[]) {



    }
    
}
