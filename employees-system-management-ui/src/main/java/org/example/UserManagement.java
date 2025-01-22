package org.example;
        import net.miginfocom.swing.MigLayout;
        import javax.swing.*;
        import javax.swing.table.DefaultTableModel;
        import javax.swing.table.TableCellRenderer;
        import java.awt.*;
        import java.awt.event.ActionEvent;
        import java.awt.event.ActionListener;
        import java.util.ArrayList;
        import java.util.List;

public class UserManagement extends JPanel {
    private JTable userTable;
    private DefaultTableModel tableModel;
    private List<User> users;
    private JButton addButton;
    private ImageIcon deleteIcon;
    private ImageIcon editIcon;

    // Components for add user dialog
    private JTextField firstNameField = new JTextField(20);
    private JTextField lastNameField = new JTextField(20);
    private JTextField emailField = new JTextField(20);
    private JComboBox<Role> roleComboBox = new JComboBox<>(Role.values());
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
        users = new ArrayList<>();

        // Sample users
        users.add(new User(1, "Sarah", "Connor", "sarah.connor@example.com", Role.ADMIN));
        users.add(new User(2, "Mike", "Tyson", "mike.tyson@example.com", Role.MANAGER));
        users.add(new User(3, "Emma", "Watson", "emma.watson@example.com", Role.MANAGER));
        users.add(new User(4, "John", "Doe", "john.doe@example.com", Role.HR));

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
        if (row >= 0 && row < users.size()) {
            users.remove(row);
            loadUsersIntoTable();
        }
    }

    private void editUser(int row) {
        if (row >= 0 && row < users.size()) {
            User userToEdit = users.get(row);
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

        // Role
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        addDialog.add(new JLabel("Role:"), gbc);

        gbc.gridx = 1;
        addDialog.add(roleComboBox, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        saveButton.addActionListener(e -> {
            addDialog.dispose();
        });
        cancelButton.addActionListener(e -> addDialog.dispose());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        addDialog.add(buttonPanel, gbc);

        addDialog.pack();
        addDialog.setLocationRelativeTo(this);
        addDialog.setVisible(true);
    }


    private void showEditUserDialog(User user) {
        JDialog editDialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Edit User",
                true
        );
        editDialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Initialize fields with existing user data
        firstNameField.setText(user.getFirstname());
        lastNameField.setText(user.getLastname());
        emailField.setText(user.getEmail());
        roleComboBox.setSelectedItem(user.getRole());

        // First Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        editDialog.add(new JLabel("First Name:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        editDialog.add(firstNameField, gbc);

        // Last Name
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        editDialog.add(new JLabel("Last Name:"), gbc);

        gbc.gridx = 1;
        editDialog.add(lastNameField, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        editDialog.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        editDialog.add(emailField, gbc);

        // Role
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        editDialog.add(new JLabel("Role:"), gbc);

        gbc.gridx = 1;
        editDialog.add(roleComboBox, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        saveButton.addActionListener(e -> {
            // Update user with new values
            user.setFirstname(firstNameField.getText());
            user.setLastname(lastNameField.getText());
            user.setEmail(emailField.getText());
            user.setRole((Role) roleComboBox.getSelectedItem());

            loadUsersIntoTable();
            editDialog.dispose();
        });

        cancelButton.addActionListener(e -> editDialog.dispose());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        editDialog.add(buttonPanel, gbc);

        editDialog.pack();
        editDialog.setLocationRelativeTo(this);
        editDialog.setVisible(true);
    }
    private void loadUsersIntoTable() {
        tableModel.setRowCount(0);
        for (User user : users) {
            tableModel.addRow(new Object[]{
                    user.getId(),
                    user.getFirstname(),
                    user.getLastname(),
                    user.getEmail(),
                    user.getRole().toString(),
                    ""
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(UserManagement::new);
    }
}

