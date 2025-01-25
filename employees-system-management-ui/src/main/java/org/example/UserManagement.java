package org.example;
        import com.google.gson.JsonObject;
        import com.google.gson.JsonParser;
        import net.miginfocom.swing.MigLayout;
        import javax.swing.*;
        import javax.swing.table.DefaultTableModel;
        import javax.swing.table.TableCellRenderer;
        import java.awt.*;
        import java.awt.event.ActionEvent;
        import java.awt.event.ActionListener;
        import java.util.ArrayList;
        import java.util.List;
        import java.net.URI;
        import java.net.http.HttpClient;
        import java.net.http.HttpRequest;
        import java.net.http.HttpResponse;
        import com.fasterxml.jackson.core.type.TypeReference;
        import com.fasterxml.jackson.databind.ObjectMapper;

public class UserManagement extends JPanel {
    private JTable userTable;
    private DefaultTableModel tableModel;
    private List<UserDeserializer> users;
    private JButton addButton;
    private ImageIcon deleteIcon;
    private ImageIcon editIcon;

    // Components for add user dialog
    private JTextField firstNameField = new JTextField(20);
    private JTextField lastNameField = new JTextField(20);
    private JTextField emailField = new JTextField(20);
    private JComboBox<Role> roleComboBox = new JComboBox<>(Role.values());
    private JPasswordField passwordField = new JPasswordField(20);
    private JPasswordField confirmPasswordField = new JPasswordField(20);
    private JButton saveButton = new JButton("Save");
    private JButton cancelButton = new JButton("Cancel");

    public UserManagement() {
        initializeUI();
        loadUsersIntoTable();
    }

    private void initializeUI() {
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[][grow]"));
        // Initialize components
        addButton = new JButton("Add");
//        users = new ArrayList<>();



        String[] columns = {"ID", "First Name", "Last Name", "Email", "Role", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };

        userTable = new JTable(tableModel) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 5 ? JPanel.class : Object.class;
            }
        };
        userTable.setAutoCreateRowSorter(true);
        userTable.setFillsViewportHeight(true);
        userTable.setRowHeight(35);  // Adjust this value as needed


        // Set up action column
        userTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        userTable.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));

        add(addButton, "align right, wrap");
        add(new JScrollPane(userTable), "grow, push, h 300::");


        addButton.addActionListener(e -> showAddUserDialog());



    }

    class ButtonRenderer extends JPanel implements TableCellRenderer {
        private JButton editButton = new JButton("Edit");
        private JButton deleteButton = new JButton("Delete");

        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 1, 0));
            add(editButton);
            add(deleteButton);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 1, 0));
        private JButton editButton = new JButton("Edit");
        private JButton deleteButton = new JButton("Delete");
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            panel.add(editButton);
            panel.add(deleteButton);

            editButton.addActionListener(e -> {
                fireEditingStopped();
                editUser(currentRow);
            });

            deleteButton.addActionListener(e -> {
                fireEditingStopped();
                deleteUser(currentRow);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = row;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    private void deleteUser(int row) {
        // Convert view row to model row to handle sorted/filtered tables
        int modelRow = userTable.convertRowIndexToModel(row);
        int id = -1;

        if (modelRow >= 0 && modelRow < users.size()) {
            UserDeserializer userToDelete = users.get(modelRow);
            id = userToDelete.getId();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid user selection",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int finalId = id;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8080/api/v1/user/" + finalId))
                            .header("Authorization", "Bearer " + Authentification.getJwtToken())
                            .DELETE()  // Removed BodyPublishers
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    SwingUtilities.invokeLater(() -> {
                        if (response.statusCode() == 204) {  // Match API's 204 No Content
                            loadUsersIntoTable();
                            JOptionPane.showMessageDialog(
                                    null,  // Or parent component
                                    "User deleted successfully",
                                    "Success", JOptionPane.INFORMATION_MESSAGE
                            );
                        } else {
                            String errorMessage = "Delete failed";
                            try {
                                // Parse JSON error if available
                                JsonObject errorJson = JsonParser.parseString(response.body()).getAsJsonObject();
                                if (errorJson.has("message")) {
                                    errorMessage = errorJson.get("message").getAsString();
                                }
                            } catch (Exception ignored) {}

                            JOptionPane.showMessageDialog(
                                    null,
                                    errorMessage,
                                    "Error", JOptionPane.ERROR_MESSAGE
                            );
                        }
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Connection error: " + ex.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE
                            )
                    );
                }
                return null;
            }
        }.execute();
    }

    private void editUser(int row) {
        System.out.println("row =======>>>>>> " + row);
        System.out.println("users.size() =======>>>>>> " + users.size());
        if (row >= 0 && row < users.size()) {
            System.out.println("dkhl =======>>>>>> ");
            UserDeserializer userToEdit = users.get(row);
            showEditUserDialog(userToEdit);
        }
    }


    private void showAddUserDialog() {
        // In showAddUserDialog() and similar methods
        JDialog addDialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Add New User",
                true
        );
        addDialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Reset fields
        firstNameField.setText("");
        lastNameField.setText("");
        emailField.setText("");
        roleComboBox.setSelectedIndex(0);

        // First Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        addDialog.add(new JLabel("First Name:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        addDialog.add(firstNameField, gbc);

        // Last Name
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        addDialog.add(new JLabel("Last Name:"), gbc);

        gbc.gridx = 1;
        addDialog.add(lastNameField, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        addDialog.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        addDialog.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3; // Adjust grid indices accordingly
        gbc.anchor = GridBagConstraints.EAST;
        addDialog.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        addDialog.add(passwordField, gbc);

        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy = 4;
        addDialog.add(new JLabel("Confirm Password:"), gbc);

        gbc.gridx = 1;
        addDialog.add(confirmPasswordField, gbc);

        // Role
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        addDialog.add(new JLabel("Role:"), gbc);

        gbc.gridx = 1;
        addDialog.add(roleComboBox, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        saveButton.addActionListener(e -> {
            // Collect data from fields
            String firstname = firstNameField.getText();
            String lastname = lastNameField.getText();
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            Role role = (Role) roleComboBox.getSelectedItem();

            // Validate input
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(addDialog, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create JSON request body
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("firstname", firstname);
            requestBody.addProperty("lastname", lastname);
            requestBody.addProperty("email", email);
            requestBody.addProperty("password", password);
            requestBody.addProperty("role", role.toString());

            // Send registration request
            Authentification.sendAuthRequest(
                    requestBody,
                    "register",
                    (response) -> {
                        addDialog.dispose();
                        loadUsersIntoTable();
                    }
            );
        });
        cancelButton.addActionListener(e -> addDialog.dispose());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        addDialog.add(buttonPanel, gbc);

        addDialog.pack();
        addDialog.setLocationRelativeTo(this);
        addDialog.setVisible(true);
    }


    private void showEditUserDialog(UserDeserializer user) {
        JDialog editDialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Edit User",
                true
        );
        editDialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField editFirstNameField = new JTextField(20);
        JTextField editLastNameField = new JTextField(20);
        JTextField editEmailField = new JTextField(20);
        JPasswordField editPasswordField = new JPasswordField(20);
        JPasswordField editConfirmPasswordField = new JPasswordField(20);
        JComboBox<Role> editRoleComboBox = new JComboBox<>(Role.values());

        editFirstNameField.setText(user.getFirstname());
        editLastNameField.setText(user.getLastname());
        editEmailField.setText(user.getEmail());
        editRoleComboBox.setSelectedItem(user.getRole());
        editPasswordField.setText("");
        editConfirmPasswordField.setText("");

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        editDialog.add(new JLabel("First Name:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        editDialog.add(editFirstNameField, gbc);

        // Last Name
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        editDialog.add(new JLabel("Last Name:"), gbc);

        gbc.gridx = 1;
        editDialog.add(editLastNameField, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        editDialog.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        editDialog.add(editEmailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3; // Adjust grid indices accordingly
        gbc.anchor = GridBagConstraints.EAST;
        editDialog.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        editDialog.add(editPasswordField, gbc);

        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy = 4;
        editDialog.add(new JLabel("Confirm Password:"), gbc);

        gbc.gridx = 1;
        editDialog.add(editConfirmPasswordField, gbc);

        // Role
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        editDialog.add(new JLabel("Role:"), gbc);

        gbc.gridx = 1;
        editDialog.add(editRoleComboBox, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton editSaveButton = new JButton("Save");
        JButton editCancelButton = new JButton("Cancel");
        editSaveButton.addActionListener(e -> {
            String newFirstname = editFirstNameField.getText();
            String newLastname = editLastNameField.getText();
            String newEmail = editEmailField.getText();
            Role newRole = (Role) editRoleComboBox.getSelectedItem();
            String password = new String(editPasswordField.getPassword());
            String confirmPassword = new String(editConfirmPasswordField.getPassword());
            int userId = user.getId();

            if (!password.isEmpty() && !password.equals(confirmPassword)) {
                System.out.println("password ====>"+ password);
                System.out.println("confirmPassword ====>"+ confirmPassword);
                JOptionPane.showMessageDialog(editDialog,
                        "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Build JSON request body
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("firstname", newFirstname);
            requestBody.addProperty("lastname", newLastname);
            requestBody.addProperty("email", newEmail);
            requestBody.addProperty("role", newRole.toString());
            if (!password.isEmpty()) {
                requestBody.addProperty("password", password);
            }

            // Send update request
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        HttpClient client = HttpClient.newHttpClient();
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:8080/api/v1/user/" + userId))
                                .header("Authorization", "Bearer " + Authentification.getJwtToken())
                                .header("Content-Type", "application/json")
                                .PUT(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                                .build();

                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                        if (response.statusCode() == 200) {
                            SwingUtilities.invokeLater(() -> {
                                loadUsersIntoTable();
                                editDialog.dispose();
                            });
                        } else {
                            String errorMessage = response.body();
                            if (response.body().contains("Email already taken!")) {
                                errorMessage = "Email already taken!";
                            }

                            String finalErrorMessage = errorMessage;
                            SwingUtilities.invokeLater(() ->
                                    JOptionPane.showMessageDialog(editDialog,
                                            "Update failed: " + finalErrorMessage,
                                            "Error", JOptionPane.ERROR_MESSAGE));
                        }
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(editDialog,
                                        "Connection error: " + ex.getMessage(),
                                        "Error", JOptionPane.ERROR_MESSAGE));
                    }
                    return null;
                }
            }.execute();
        });



        editCancelButton.addActionListener(e -> editDialog.dispose());
        buttonPanel.add(editSaveButton);
        buttonPanel.add(editCancelButton);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        editDialog.add(buttonPanel, gbc);

        editDialog.pack();
        editDialog.setLocationRelativeTo(this);
        editDialog.setVisible(true);
    }

    private void loadUsersIntoTable() {
        String token = Authentification.getJwtToken();
        if (token == null) {
            JOptionPane.showMessageDialog(null, "Not authenticated!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new SwingWorker<List<UserDeserializer>, Void>() {
            @Override
            protected List<UserDeserializer> doInBackground() throws Exception {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/v1/user"))
                        .header("Authorization", "Bearer " + token)
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("Response Body: " + response.body());
                if (response.statusCode() != 200) {
                    throw new RuntimeException("Failed to fetch users: HTTP " + response.statusCode());
                }

                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(response.body(), new TypeReference<List<UserDeserializer>>() {});
            }

            @Override
            protected void done() {
                try {
                    users = get();
                    tableModel.setRowCount(0); // Clear existing data
                    for (UserDeserializer user : users) {
                        tableModel.addRow(new Object[]{
                                user.getId(),
                                user.getFirstname(),
                                user.getLastname(),
                                user.getEmail(),
                                user.getRole(),
                                ""
                        });
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Error loading users: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(UserManagement::new);
    }
}

