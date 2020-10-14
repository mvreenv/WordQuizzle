package client;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

// DA ELIMINARE, VEDERE SE RECUPERARE ELEMENTI GUI

public class AddFriendWindow extends JFrame implements ActionListener{
    
    /**
     * Valore impostato di default.
     */
    private static final long serialVersionUID = 1L;

    private JPanel addFriendPanel;
    private JTextField addTextField;
    private JButton addButton;

    public AddFriendWindow(String user){

        this.setTitle("Add Friend");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        addFriendPanel = new JPanel();
        addFriendPanel.setPreferredSize(new Dimension(200, 150));
        addFriendPanel.setLayout(new GridBagLayout());
        GridBagConstraints addFriendPanelConstraints = new GridBagConstraints();
        addFriendPanelConstraints.fill = GridBagConstraints.HORIZONTAL;

        addTextField = new JTextField();
        addTextField.setHorizontalAlignment(JTextField.CENTER);
        addFriendPanelConstraints.gridx = 0;
        addFriendPanelConstraints.gridy = 0;
        addFriendPanelConstraints.insets.bottom = 10;
        addFriendPanel.add(addTextField, addFriendPanelConstraints);

        addButton = new JButton("Add");
        addButton.addActionListener(this);
        addFriendPanelConstraints.gridx = 0;
        addFriendPanelConstraints.gridy = 1;
        addFriendPanelConstraints.insets.left = 30;
        addFriendPanelConstraints.insets.right = 30;
        addFriendPanel.add(addButton, addFriendPanelConstraints);

        this.add(addFriendPanel);
        this.setSize(addFriendPanel.getPreferredSize());

        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getSource().equals(addButton)) {
            this.dispose();
        }

    }


}