package org.example;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginUi extends JPanel implements ActionListener{
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;
    private JFrame frame;

    public LoginUi(JFrame frame, LayoutManager layout) {
        super(layout);
        this.frame = frame;
        initializeComponents();
        addComponentsToPanel();
        addActionListeners();
    }

    private void initializeComponents() {
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");
        cancelButton = new JButton("Cancel");
    }

    private void addComponentsToPanel() {
        if (getLayout() instanceof MigLayout) {
            add(new JLabel("Username:"), "align right");
            add(usernameField, "align left, growx");
            add(new JLabel("Password:"), "align right");
            add(passwordField, "align left, growx");
            add(loginButton, "span, center");
            add(cancelButton, "span, center");
        }
    }
    private void addActionListeners() {
        loginButton.addActionListener(this);
        cancelButton.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (username.equals("admin") && password.equals("admin")) {
                new AdminDashboard();
                frame.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Username or password incorrect!");
            }
        } else if (e.getSource() == cancelButton) {
            frame.dispose();
        }
    }
}
