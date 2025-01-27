package org.example;
        import com.google.gson.JsonObject;
        import com.google.gson.JsonParser;
        import net.miginfocom.swing.MigLayout;
        import javax.swing.*;
        import javax.swing.event.DocumentEvent;
        import javax.swing.event.DocumentListener;
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
    private JButton editSaveButton = new JButton("Save");

    private JLabel firstNameErrorLabel = new JLabel();
    private JLabel lastNameErrorLabel = new JLabel();
    private JLabel emailErrorLabel = new JLabel();
    private JLabel passwordErrorLabel = new JLabel();
    private JLabel confirmPasswordErrorLabel = new JLabel();

    public UserManagement() {
        initializeUI();
        loadUsersIntoTable();
        initializeErrorLabels();
    }

    private void initializeErrorLabels() {
        Font errorFont = new Font("SansSerif", Font.PLAIN, 10);
        Color errorColor = Color.RED;

        for (JLabel errorLabel : List.of(
                firstNameErrorLabel, lastNameErrorLabel, emailErrorLabel,
                passwordErrorLabel, confirmPasswordErrorLabel
        )) {
            errorLabel.setFont(errorFont);
            errorLabel.setForeground(errorColor);
            errorLabel.setText(" ");
        }
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
        clearAllErrors();
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
        gbc.gridy++;
        addDialog.add(firstNameErrorLabel, gbc);

        // Last Name
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        addDialog.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        addDialog.add(lastNameField, gbc);
        gbc.gridy++;
        addDialog.add(lastNameErrorLabel, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        addDialog.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        addDialog.add(emailField, gbc);
        gbc.gridy++;
        gbc.gridx = 1;
        addDialog.add(emailErrorLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy++; // Adjust grid indices accordingly
        gbc.anchor = GridBagConstraints.EAST;
        addDialog.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        addDialog.add(passwordField, gbc);
        gbc.gridy++;
        gbc.gridx = 1;
        addDialog.add(passwordErrorLabel, gbc);

        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy++;
        addDialog.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        addDialog.add(confirmPasswordField, gbc);
        gbc.gridy++;
        gbc.gridx = 1;
        addDialog.add(confirmPasswordErrorLabel, gbc);

        // Role
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        addDialog.add(new JLabel("Role:"), gbc);

        gbc.gridx = 1;
        addDialog.add(roleComboBox, gbc);
        addValidationListeners();

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
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        addDialog.add(buttonPanel, gbc);

        addDialog.pack();
        addDialog.setLocationRelativeTo(this);
        addDialog.setVisible(true);
        saveButton.setEnabled(false);
    }
    private void clearAllErrors() {
        for (JLabel label : List.of(firstNameErrorLabel, lastNameErrorLabel,
                emailErrorLabel, passwordErrorLabel, confirmPasswordErrorLabel)) {
            label.setText(" ");
        }
    }
    private void addValidationListeners() {
        // First Name
        firstNameField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { validateFirstName(); }
            public void insertUpdate(DocumentEvent e) { validateFirstName(); }
            public void removeUpdate(DocumentEvent e) { validateFirstName(); }
        });

        // Last Name
        lastNameField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { validateLastName(); }
            public void insertUpdate(DocumentEvent e) { validateLastName(); }
            public void removeUpdate(DocumentEvent e) { validateLastName(); }
        });

        // Email
        emailField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { validateEmail(); }
            public void insertUpdate(DocumentEvent e) { validateEmail(); }
            public void removeUpdate(DocumentEvent e) { validateEmail(); }
        });

        // Password & Confirm Password
        DocumentListener passwordListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { validatePasswords(); }
            public void insertUpdate(DocumentEvent e) { validatePasswords(); }
            public void removeUpdate(DocumentEvent e) { validatePasswords(); }
        };
        passwordField.getDocument().addDocumentListener(passwordListener);
        confirmPasswordField.getDocument().addDocumentListener(passwordListener);
    }
    private void validateFirstName() {
        String text = firstNameField.getText().trim();
        if (text.isEmpty()) {
            showError(firstNameErrorLabel, "First name is required");
        }
        else if (text.length() > 20) {
            showError(firstNameErrorLabel, "First name must be less than 20 characters");
        }
        else {
            clearError(firstNameErrorLabel);
        }
        updateSaveButtonState();
    }

    private void validateLastName() {
        String text = lastNameField.getText().trim();
        if (text.isEmpty()) {
            showError(lastNameErrorLabel, "Last name is required");
        }
        else if (text.length() > 20) {
            showError(firstNameErrorLabel, "Last name must be less than 20 characters");
        }
        else {
            clearError(lastNameErrorLabel);
        }
        updateSaveButtonState();
    }

    private void validateEmail() {
        String email = emailField.getText().trim();
        String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        if (email.isEmpty()) {
            showError(emailErrorLabel, "Email is required");
        } else if (!email.matches(regex)) {
            showError(emailErrorLabel, "Invalid email format");
        } else {
            clearError(emailErrorLabel);
        }
        updateSaveButtonState();
    }

    private void validatePasswords() {
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());

        // Password length
        if (password.length() < 8) {
            showError(passwordErrorLabel, "Password must be ≥8 characters");
        } else {
            clearError(passwordErrorLabel);
        }

        // Password match
        if (!password.equals(confirm)) {
            showError(confirmPasswordErrorLabel, "Passwords don't match");
        } else {
            clearError(confirmPasswordErrorLabel);
        }
        updateSaveButtonState();
    }

    private void showError(JLabel errorLabel, String message) {
        errorLabel.setText(message);
    }

    private void clearError(JLabel errorLabel) {
        errorLabel.setText(" ");
    }

    private boolean isFormValid() {
        return firstNameErrorLabel.getText().trim().isEmpty() &&
                lastNameErrorLabel.getText().trim().isEmpty() &&
                emailErrorLabel.getText().trim().isEmpty() &&
                passwordErrorLabel.getText().trim().isEmpty() &&
                confirmPasswordErrorLabel.getText().trim().isEmpty();
    }

    private void updateSaveButtonState() {
        saveButton.setEnabled(isFormValid());
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

        JLabel firstNameErrorLabel = new JLabel();
        JLabel lastNameErrorLabel = new JLabel();
        JLabel emailErrorLabel = new JLabel();
        JLabel passwordErrorLabel = new JLabel();
        JLabel confirmPasswordErrorLabel = new JLabel();

        Font errorFont = new Font("SansSerif", Font.PLAIN, 10);
        Color errorColor = Color.RED;
        for (JLabel label : List.of(firstNameErrorLabel, lastNameErrorLabel,
                emailErrorLabel, passwordErrorLabel, confirmPasswordErrorLabel)) {
            label.setFont(errorFont);
            label.setForeground(errorColor);
        }

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
        gbc.gridy++;
        editDialog.add(firstNameErrorLabel, gbc);

        // Last Name
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        editDialog.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        editDialog.add(editLastNameField, gbc);
        gbc.gridy++;
        editDialog.add(lastNameErrorLabel, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        editDialog.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        editDialog.add(editEmailField, gbc);
        gbc.gridy++;
        editDialog.add(emailErrorLabel, gbc);


        gbc.gridx = 0;
        gbc.gridy++; // Adjust grid indices accordingly
        gbc.anchor = GridBagConstraints.EAST;
        editDialog.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        editDialog.add(editPasswordField, gbc);
        gbc.gridy++;
        editDialog.add(passwordErrorLabel, gbc);


        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy++;
        editDialog.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        editDialog.add(editConfirmPasswordField, gbc);
        gbc.gridy++;
        editDialog.add(confirmPasswordErrorLabel, gbc);


        // Role
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        editDialog.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        editDialog.add(editRoleComboBox, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton editCancelButton = new JButton("Cancel");

        addEditValidationListeners(
                editFirstNameField, editLastNameField, editEmailField,
                editPasswordField, editConfirmPasswordField,
                firstNameErrorLabel, lastNameErrorLabel,
                emailErrorLabel, passwordErrorLabel, confirmPasswordErrorLabel
        );
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
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        editDialog.add(buttonPanel, gbc);

        editDialog.pack();
        editDialog.setLocationRelativeTo(this);
        editDialog.setVisible(true);
        editSaveButton.setEnabled(false);
    }

    private void addEditValidationListeners(JTextField firstName, JTextField lastName,
                                            JTextField email, JPasswordField password,
                                            JPasswordField confirmPassword,
                                            JLabel EfirstNameError, JLabel ElastNameError,
                                            JLabel EemailError, JLabel EpasswordError,
                                            JLabel EconfirmPasswordError
                                            ) {
        // First Name
        firstName.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { validateEditName(firstName, EfirstNameError); }
            public void insertUpdate(DocumentEvent e) { validateEditName(firstName, EfirstNameError); }
            public void removeUpdate(DocumentEvent e) { validateEditName(firstName, EfirstNameError); }
        });

        // Last Name
        lastName.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { validateEditLastName(lastName, ElastNameError); }
            public void insertUpdate(DocumentEvent e) { validateEditLastName(lastName, ElastNameError); }
            public void removeUpdate(DocumentEvent e) { validateEditLastName(lastName, ElastNameError); }
        });

        // Email
        email.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { validateEditEmail(email, EemailError); }
            public void insertUpdate(DocumentEvent e) { validateEditEmail(email, EemailError); }
            public void removeUpdate(DocumentEvent e) { validateEditEmail(email, EemailError); }
        });

        // Password listeners
        DocumentListener passwordListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { validateEditPasswords(password, confirmPassword, EpasswordError, EconfirmPasswordError); }
            public void insertUpdate(DocumentEvent e) { validateEditPasswords(password, confirmPassword, EpasswordError, EconfirmPasswordError); }
            public void removeUpdate(DocumentEvent e) { validateEditPasswords(password, confirmPassword, EpasswordError, EconfirmPasswordError); }
        };
        password.getDocument().addDocumentListener(passwordListener);
        confirmPassword.getDocument().addDocumentListener(passwordListener);
    }

    private void validateEditName(JTextField firstname, JLabel EfirstNameErrorLabel) {
        String text = firstname.getText().trim();
        if (text.isEmpty()) {
            EfirstNameErrorLabel.setText("First name is required");
        } else if (text.length() > 20) {
            EfirstNameErrorLabel.setText("First name must be less than 20 characters");
        } else {
            EfirstNameErrorLabel.setText(" ");
        }
        updateEditSaveButtonState(editSaveButton, EfirstNameErrorLabel
        );
    }

    private void validateEditLastName(JTextField field, JLabel errorLabel) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            errorLabel.setText("Last name is required");
        } else if (text.length() > 20) {
            errorLabel.setText("Last name must be less than 20 characters");
        } else {
            errorLabel.setText(" ");
        }
        updateEditSaveButtonState(editSaveButton, errorLabel
        );
    }

    private void validateEditEmail(JTextField emailField, JLabel errorLabel) {
        String email = emailField.getText().trim();
        String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        if (email.isEmpty()) {
            errorLabel.setText("Email is required");
        } else if (!email.matches(regex)) {
            errorLabel.setText("Invalid email format");
        } else {
            errorLabel.setText(" ");
        }
        updateEditSaveButtonState(editSaveButton,
                errorLabel);
    }

    private void validateEditPasswords(JPasswordField passwordField,
                                       JPasswordField confirmField,
                                       JLabel passError,
                                       JLabel confirmError) {
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (password.isEmpty() && confirm.isEmpty()) {
            passError.setText(" ");
            confirmError.setText(" ");
            return;
        }

        if (password.length() < 8) {
            passError.setText("≥8 characters required");
        } else {
            passError.setText(" ");
        }

        if (!password.equals(confirm)) {
            confirmError.setText("Passwords must match");
        } else {
            confirmError.setText(" ");
        }
        updateEditSaveButtonState(editSaveButton
                , passError, confirmError);
    }

    private void updateEditSaveButtonState(JButton button, JLabel... errorLabels) {
        boolean isValid = true;
        for (JLabel label : errorLabels) {
            if (!label.getText().trim().isEmpty()) {
                isValid = false;
                break;
            }
        }
        editSaveButton.setEnabled(isValid);
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

