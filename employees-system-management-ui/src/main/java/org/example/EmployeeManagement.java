package org.example;

        import com.fasterxml.jackson.core.type.TypeReference;
        import com.fasterxml.jackson.databind.ObjectMapper;
        import com.google.gson.JsonObject;
        import com.google.gson.JsonParser;
        import net.miginfocom.swing.MigLayout;
        import javax.swing.*;
        import javax.swing.event.DocumentEvent;
        import javax.swing.event.DocumentListener;
        import javax.swing.table.DefaultTableModel;
        import javax.swing.table.TableCellRenderer;
        import java.awt.*;
        import java.net.URI;
        import java.net.http.HttpClient;
        import java.net.http.HttpRequest;
        import java.net.http.HttpResponse;
        import java.text.ParseException;
        import java.text.SimpleDateFormat;
        import java.util.ArrayList;
        import java.util.Date;
        import java.util.List;

public class EmployeeManagement extends JPanel {
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private List<Employee> employees;
    private JButton addButton;



    public EmployeeManagement() {
        initializeUI();
        loadEmployeesIntoTable();
    }

    private void initializeUI() {
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[][grow]"));
        addButton = new JButton("Add Employee");
        employees = new ArrayList<>();

        String[] columns = {"ID", "Full Name", "Job Title", "Department", "Hire Date", "Status", "Contact", "Address", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8;
            }
        };

        employeeTable = new JTable(tableModel) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 8 ? JPanel.class : Object.class;
            }
        };
        employeeTable.setAutoCreateRowSorter(true);
        employeeTable.setFillsViewportHeight(true);
        employeeTable.setRowHeight(35);

        // Set column widths
        employeeTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        employeeTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        employeeTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        employeeTable.getColumnModel().getColumn(8).setPreferredWidth(150);

        employeeTable.getColumnModel().getColumn(8).setCellRenderer(new ButtonRenderer());
        employeeTable.getColumnModel().getColumn(8).setCellEditor(new ButtonEditor(new JCheckBox()));

        add(addButton, "align right, wrap");
        add(new JScrollPane(employeeTable), "grow, push, h 300::");

        addButton.addActionListener(e -> showAddEmployeeDialog());

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
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
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
                editEmployee(currentRow);
            });

            deleteButton.addActionListener(e -> {
                fireEditingStopped();
                deleteEmployee(currentRow);
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

    private void deleteEmployee(int row) {
        int modelRow = employeeTable.convertRowIndexToModel(row);
        Long id = -1L;

        if (modelRow >= 0 && modelRow < employees.size()) {
            Employee employeeToDelete = employees.get(modelRow);
            id = employeeToDelete.getEmployeeId();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid user selection",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Long finalId = id;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8080/api/v1/employee/" + finalId))
                            .header("Authorization", "Bearer " + Authentification.getJwtToken())
                            .DELETE()
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    SwingUtilities.invokeLater(() -> {
                        if (response.statusCode() == 204) {
                            loadEmployeesIntoTable();
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Employee deleted successfully",
                                    "Success", JOptionPane.INFORMATION_MESSAGE
                            );
                        } else {
                            String errorMessage = "Delete failed";
                            try {
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

    private void editEmployee(int row) {
        if (row >= 0 && row < employees.size()) {
            Employee employee = employees.get(row);
            showEditEmployeeDialog(employee);
        }
    }
    private void initializeErrorLabels(JLabel fullNameErrorLabel, JLabel jobTitleErrorLabel, JLabel departmentErrorLabel,
                                       JLabel hireDateErrorLabel, JLabel contactErrorLabel, JLabel addressErrorLabel) {
        Font errorFont = new Font("SansSerif", Font.PLAIN, 10);
        Color errorColor = Color.RED;

        for (JLabel errorLabel : List.of(
                fullNameErrorLabel, jobTitleErrorLabel, departmentErrorLabel,
                hireDateErrorLabel, contactErrorLabel, addressErrorLabel
        )) {
            errorLabel.setFont(errorFont);
            errorLabel.setForeground(errorColor);
            errorLabel.setText(" ");
        }
    }
//    "hireDate": "2023-10-01"
    private void showAddEmployeeDialog() {
        JTextField fullNameField = new JTextField(20);
        JTextField jobTitleField = new JTextField(20);
        JTextField departmentField = new JTextField(20);
        JTextField hireDateField = new JTextField(10);
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Full-Time", "Part-Time", "Freelancer"});
        JTextField contactField = new JTextField(20);
        JTextField addressField = new JTextField(30);
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        JLabel fullNameErrorLabel = new JLabel();
        JLabel jobTitleErrorLabel = new JLabel();
        JLabel departmentErrorLabel = new JLabel();
        JLabel hireDateErrorLabel = new JLabel();
        JLabel contactErrorLabel = new JLabel();
        JLabel addressErrorLabel = new JLabel();
        initializeErrorLabels(
                fullNameErrorLabel, jobTitleErrorLabel, departmentErrorLabel,
                hireDateErrorLabel, contactErrorLabel, addressErrorLabel
        );

        JDialog addDialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Add New Employee",
                true
        );
        addDialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        fullNameField.setText("");
        jobTitleField.setText("");
        departmentField.setText("");
        hireDateField.setText("");
        statusCombo.setSelectedIndex(0);
        contactField.setText("");
        addressField.setText("");

        addLabelAndField(addDialog, gbc, 0, "Full Name:", fullNameField);
        gbc.gridy++;
        gbc.gridx = 1;
        addDialog.add(fullNameErrorLabel, gbc);
        addLabelAndField(addDialog, gbc, 2, "Job Title:", jobTitleField);
        gbc.gridy++;
        gbc.gridx = 1;
        addDialog.add(jobTitleErrorLabel, gbc);
        addLabelAndField(addDialog, gbc, 4, "Department:", departmentField);
        gbc.gridy++;
        gbc.gridx = 1;
        addDialog.add(departmentErrorLabel, gbc);
        addLabelAndField(addDialog, gbc, 6, "Hire Date:", hireDateField);
        gbc.gridy++;
        gbc.gridx = 1;
        addDialog.add(hireDateErrorLabel, gbc);
        addLabelAndCombo(addDialog, gbc, 8, "Status:", statusCombo);
        addLabelAndField(addDialog, gbc, 9, "Contact:", contactField);
        gbc.gridy++;
        gbc.gridx = 1;
        addDialog.add(contactErrorLabel, gbc);
        addLabelAndField(addDialog, gbc, 11, "Address:", addressField);
        gbc.gridy++;
        gbc.gridx = 1;
        addDialog.add(addressErrorLabel, gbc);
        addValidationListeners(fullNameField, jobTitleField, departmentField, hireDateField
                , contactField, addressField,  saveButton,
                fullNameErrorLabel, jobTitleErrorLabel, departmentErrorLabel,
                hireDateErrorLabel, contactErrorLabel, addressErrorLabel);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        saveButton.addActionListener(e -> {
            String fullName = fullNameField.getText();
            String jobTitle = jobTitleField.getText();
            String department = departmentField.getText();
            String hireDate = hireDateField.getText();
            String employmentStatus = (String) statusCombo.getSelectedItem();
            String contactInformation = contactField.getText();
            String address = addressField.getText();

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("fullName", fullName);
            requestBody.addProperty("jobTitle", jobTitle);
            requestBody.addProperty("department", department);
            requestBody.addProperty("hireDate", hireDate);
            requestBody.addProperty("employmentStatus", employmentStatus);
            requestBody.addProperty("contactInformation", contactInformation);
            requestBody.addProperty("address", address);

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        HttpClient client = HttpClient.newHttpClient();
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:8080/api/v1/employee"))
                                .header("Authorization", "Bearer " + Authentification.getJwtToken())
                                .header("Content-Type", "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                                .build();

                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                        if (response.statusCode() == 200) {
                            SwingUtilities.invokeLater(() -> {
                                loadEmployeesIntoTable();
                                addDialog.dispose();
                            });
                        } else {
                            String finalErrorMessage = "Error: " + response.body();
                            SwingUtilities.invokeLater(() ->
                                    JOptionPane.showMessageDialog(addDialog,
                                            finalErrorMessage,
                                            "Error", JOptionPane.ERROR_MESSAGE));
                        }
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(addDialog,
                                        "Connection error: " + ex.getMessage(),
                                        "Error", JOptionPane.ERROR_MESSAGE));
                    }
                    return null;
                }
            }.execute();
        }); // Fixed: Correct brace placement for ActionListener

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

    private void addValidationListeners(JTextField fullNameField, JTextField jobTitleField, JTextField departmentField, JTextField hireDateField
            , JTextField contactField, JTextField addressField, JButton saveButton,
                                        JLabel fullNameErrorLabel, JLabel jobTitleErrorLabel, JLabel departmentErrorLabel,
                                        JLabel hireDateErrorLabel, JLabel contactErrorLabel, JLabel addressErrorLabel) {
        fullNameField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { validateFullName(fullNameField, fullNameErrorLabel, saveButton); }
            public void insertUpdate(DocumentEvent e) { validateFullName(fullNameField, fullNameErrorLabel, saveButton); }
            public void removeUpdate(DocumentEvent e) { validateFullName(fullNameField, fullNameErrorLabel, saveButton); }
        });

        jobTitleField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { validateJobTitle(jobTitleField, jobTitleErrorLabel, saveButton); }
            public void insertUpdate(DocumentEvent e) { validateJobTitle(jobTitleField, jobTitleErrorLabel, saveButton); }
            public void removeUpdate(DocumentEvent e) { validateJobTitle(jobTitleField, jobTitleErrorLabel, saveButton); }
        });

        departmentField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { validateDepartment(departmentField, departmentErrorLabel, saveButton); }
            public void insertUpdate(DocumentEvent e) { validateDepartment(departmentField, departmentErrorLabel, saveButton); }
            public void removeUpdate(DocumentEvent e) { validateDepartment(departmentField, departmentErrorLabel, saveButton); }
        });
        hireDateField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { validateHireDate(hireDateField, hireDateErrorLabel, saveButton); }
            public void insertUpdate(DocumentEvent e) { validateHireDate(hireDateField, hireDateErrorLabel, saveButton); }
            public void removeUpdate(DocumentEvent e) { validateHireDate(hireDateField, hireDateErrorLabel, saveButton); }
        });

        contactField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { validateContact(contactField, contactErrorLabel, saveButton); }
            public void insertUpdate(DocumentEvent e) { validateContact(contactField, contactErrorLabel, saveButton); }
            public void removeUpdate(DocumentEvent e) { validateContact(contactField, contactErrorLabel, saveButton); }
        });
        addressField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { validateAddress(addressField, addressErrorLabel, saveButton); }
            public void insertUpdate(DocumentEvent e) { validateAddress(addressField, addressErrorLabel, saveButton); }
            public void removeUpdate(DocumentEvent e) { validateAddress(addressField, addressErrorLabel, saveButton); }
        });

    }

    private void validateAddress(JTextField addressField, JLabel addressErrorLabel, JButton saveButton) {
        String text = addressField.getText().trim();
        if (text.isEmpty()) {
            showError(addressErrorLabel, "Address is required");
        }
        else if (text.length() > 100) {
            showError(addressErrorLabel, "Address must be less than 100 characters");
        }
        else {
            clearError(addressErrorLabel);
        }
        updateSaveButtonState(saveButton, addressErrorLabel);
    }

    private void validateContact(JTextField contactField, JLabel contactErrorLabel, JButton saveButton) {
        String text = contactField.getText().trim();
        if (text.isEmpty()) {
            showError(contactErrorLabel, "Contact is required");
        }
        else if (text.length() > 30) {
            showError(contactErrorLabel, "Contact must be less than 30 characters");
        }
        else {
            clearError(contactErrorLabel);
        }
        updateSaveButtonState(saveButton, contactErrorLabel);
    }

    private void validateHireDate(JTextField hireDateField, JLabel hireDateErrorLabel, JButton saveButton) {
        String input = hireDateField.getText().trim();
        if (input.isEmpty()) {
            hireDateErrorLabel.setText(" "); // Clear error
            return;
        }

        if (!DateValidator.isValidDate(input)) {
            showError(hireDateErrorLabel, "Invalid date! Use format yyyy-MM-dd, between 1970-01-01 and today.");
        } else {
            clearError(hireDateErrorLabel);
        }
        updateSaveButtonState(saveButton, hireDateErrorLabel);
    }

    private void validateDepartment(JTextField departmentField, JLabel departmentErrorLabel, JButton saveButton) {
        String text = departmentField.getText().trim();
        if (text.isEmpty()) {
            showError(departmentErrorLabel, "Departement is required");
        }
        else if (text.length() > 30) {
            showError(departmentErrorLabel, "Departement must be less than 30 characters");
        }
        else {
            clearError(departmentErrorLabel);
        }
        updateSaveButtonState(saveButton, departmentErrorLabel);
    }

    private void validateJobTitle(JTextField jobTitleField, JLabel jobTitleErrorLabel, JButton saveButton) {
        String text = jobTitleField.getText().trim();
        if (text.isEmpty()) {
            showError(jobTitleErrorLabel, "Job Title is required");
        }
        else if (text.length() > 30) {
            showError(jobTitleErrorLabel, "Job Title must be less than 30 characters");
        }
        else {
            clearError(jobTitleErrorLabel);
        }
        updateSaveButtonState(saveButton, jobTitleErrorLabel);
    }

    private void validateFullName(JTextField fullNameField, JLabel fullNameErrorLabel, JButton saveButton) {
        String text = fullNameField.getText().trim();
        if (text.isEmpty()) {
            showError(fullNameErrorLabel, "Full name is required");
        }
        else if (text.length() > 30) {
            showError(fullNameErrorLabel, "Full name must be less than 30 characters");
        }
        else {
            clearError(fullNameErrorLabel);
        }
        updateSaveButtonState(saveButton, fullNameErrorLabel);
    }

    private void showError(JLabel errorLabel, String message) {
        errorLabel.setText(message);
    }

    private void clearError(JLabel errorLabel) {
        errorLabel.setText(" ");
    }

    private void updateSaveButtonState(JButton saveButton, JLabel... errorLabels) {
        boolean isValid = true;
        for (JLabel label : errorLabels) {
            if (!label.getText().trim().isEmpty()) {
                isValid = false;
                break;
            }
        }
        saveButton.setEnabled(isValid);
    }
    private void showEditEmployeeDialog(Employee employee) {
        JTextField fullNameField = new JTextField(20);
        JTextField jobTitleField = new JTextField(20);
        JTextField departmentField = new JTextField(20);
        JTextField hireDateField = new JTextField(10);
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Full-Time", "Part-Time", "Freelancer"});
        JTextField contactField = new JTextField(20);
        JTextField addressField = new JTextField(30);
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        JLabel fullNameErrorLabel = new JLabel();
        JLabel jobTitleErrorLabel = new JLabel();
        JLabel departmentErrorLabel = new JLabel();
        JLabel hireDateErrorLabel = new JLabel();
        JLabel contactErrorLabel = new JLabel();
        JLabel addressErrorLabel = new JLabel();
        initializeErrorLabels(
                fullNameErrorLabel, jobTitleErrorLabel, departmentErrorLabel,
                hireDateErrorLabel, contactErrorLabel, addressErrorLabel
        );

        JDialog editDialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Edit Employee",
                true
        );
        editDialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addValidationListeners(fullNameField, jobTitleField, departmentField, hireDateField
                , contactField, addressField,  saveButton,
                fullNameErrorLabel, jobTitleErrorLabel, departmentErrorLabel,
                hireDateErrorLabel, contactErrorLabel, addressErrorLabel);

        fullNameField.setText(employee.getFullName());
        jobTitleField.setText(employee.getJobTitle());
        departmentField.setText(employee.getDepartment());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        hireDateField.setText(dateFormat.format(employee.getHireDate()));
        statusCombo.setSelectedItem(employee.getEmploymentStatus());
        contactField.setText(employee.getContactInformation());
        addressField.setText(employee.getAddress());

        // Add fields (same as add dialog)
        addLabelAndField(editDialog, gbc, 0, "Full Name:", fullNameField);
        addLabelAndField(editDialog, gbc, 1, "Job Title:", jobTitleField);
        addLabelAndField(editDialog, gbc, 2, "Department:", departmentField);
        addLabelAndField(editDialog, gbc, 3, "Hire Date:", hireDateField);
        addLabelAndCombo(editDialog, gbc, 4, "Status:", statusCombo);
        addLabelAndField(editDialog, gbc, 5, "Contact:", contactField);
        addLabelAndField(editDialog, gbc, 6, "Address:", addressField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        saveButton.addActionListener(e -> {
            String newFullName = fullNameField.getText();
            String newJobTitle = jobTitleField.getText();
            String newDepartement = departmentField.getText();
            String status = (String) statusCombo.getSelectedItem();
            String contact = contactField.getText();
            String address = addressField.getText();
            String hireDate = hireDateField.getText();
            Long employeeId = employee.getEmployeeId();
            System.out.println("employeeId " + employeeId);

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("fullName", newFullName);
            requestBody.addProperty("jobTitle", newJobTitle);
            requestBody.addProperty("department", newDepartement);
            requestBody.addProperty("employmentStatus", status);
            requestBody.addProperty("contactInformation", contact);
            requestBody.addProperty("address", address);

            try {
                SimpleDateFormat dateFormatjson = new SimpleDateFormat("yyyy-MM-dd");
                Date parsedDate = dateFormatjson.parse(hireDateField.getText().trim());
                String formattedHireDate = dateFormatjson.format(parsedDate);
                requestBody.addProperty("hireDate", formattedHireDate);
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(editDialog, "Invalid date format! Use YYYY-MM-DD.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Send update request
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        HttpClient client = HttpClient.newHttpClient();
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:8080/api/v1/employee/" + employeeId))
                                .header("Authorization", "Bearer " + Authentification.getJwtToken())
                                .header("Content-Type", "application/json")
                                .PUT(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                                .build();

                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                        if (response.statusCode() == 200) {
                            SwingUtilities.invokeLater(() -> {
                                loadEmployeesIntoTable();
                                editDialog.dispose();
                            });
                        } else {

                            String finalErrorMessage = "faild updating employee!";
                            SwingUtilities.invokeLater(() ->
                                    JOptionPane.showMessageDialog(editDialog,
                                            finalErrorMessage,
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
        cancelButton.addActionListener(e -> editDialog.dispose());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        editDialog.add(buttonPanel, gbc);

        editDialog.pack();
        editDialog.setLocationRelativeTo(this);
        editDialog.setVisible(true);
    }

    private void addLabelAndField(JDialog dialog, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        dialog.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        dialog.add(field, gbc);
    }

    private void addLabelAndCombo(JDialog dialog, GridBagConstraints gbc, int row, String label, JComboBox<?> combo) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        dialog.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        dialog.add(combo, gbc);
    }


    private void loadEmployeesIntoTable() {
        String token = Authentification.getJwtToken();
        if (token == null) {
            JOptionPane.showMessageDialog(null, "Not authenticated!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new SwingWorker<List<Employee>, Void>() {
            @Override
            protected List<Employee> doInBackground() throws Exception {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/v1/employee"))
                        .header("Authorization", "Bearer " + token)
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("Response Body: " + response.body());
                if (response.statusCode() != 200) {
                    throw new RuntimeException("Failed to fetch employees: HTTP " + response.statusCode());
                }

                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(response.body(), new TypeReference<List<Employee>>() {});
            }

            @Override
            protected void done() {
                try {
                    employees = get();
                    tableModel.setRowCount(0); // Clear existing data
                    for (Employee employee : employees) {
                        tableModel.addRow(new Object[]{
                                employee.getEmployeeId(),
                                employee.getFullName(),
                                employee.getJobTitle(),
                                employee.getDepartment(),
                                employee.getHireDate(),
                                employee.getEmploymentStatus(),
                                employee.getContactInformation(),
                                employee.getAddress(),
                                ""
                        });
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Error loading employees: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EmployeeManagement::new);
    }
}