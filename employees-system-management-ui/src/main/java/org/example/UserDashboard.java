package org.example;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class UserDashboard extends JFrame{
    public UserDashboard() {
        setTitle("User Dashboard");

        // Add EmployeeManagement directly to the frame
        EmployeeManagement employeeManagement = new EmployeeManagement();
        add(employeeManagement);

        // Frame settings
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        setVisible(true);
    }
}
