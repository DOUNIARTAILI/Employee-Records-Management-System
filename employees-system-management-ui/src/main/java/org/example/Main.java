package org.example;

import javax.swing.*;
import net.miginfocom.swing.MigLayout;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        LoginUi loginPanel = new LoginUi(frame, new MigLayout("wrap 2"));

        frame.add(loginPanel);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}