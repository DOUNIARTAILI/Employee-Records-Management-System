package org.example;

import com.google.gson.JsonObject;
import net.miginfocom.swing.MigLayout;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

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
            String email = usernameField.getText();
            String password = new String(passwordField.getPassword());
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("email", email); // Use "username" if your API expects it
            requestBody.addProperty("password", password);
            Authentification.sendAuthRequest(requestBody, "authenticate", (role) -> {
                SwingUtilities.invokeLater(() -> {
                    if (role.equals("ADMIN")) {
                        new AdminDashboard().setVisible(true);
                    } else {
                        new UserDashboard().setVisible(true);
                    }

                    // Close the login window
                    Window loginWindow = SwingUtilities.getWindowAncestor(usernameField);
                    if (loginWindow != null) {
                        loginWindow.dispose();
                    }
                });
            });

        } else if (e.getSource() == cancelButton) {
            frame.dispose();
        }
    }

}

