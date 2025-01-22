package org.example;

        import net.miginfocom.swing.MigLayout;
        import javax.swing.*;
        import javax.swing.table.DefaultTableModel;
        import javax.swing.table.TableCellRenderer;
        import java.awt.*;
        import java.util.ArrayList;
        import java.util.Date;
        import java.util.List;

public class EmployeeManagement extends JPanel {
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private List<Employee> employees;
    private JButton addButton;

    // Components for add employee dialog
    private JTextField fullNameField = new JTextField(20);
    private JTextField jobTitleField = new JTextField(20);
    private JTextField departmentField = new JTextField(20);
    private JTextField hireDateField = new JTextField(10);
    private JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Full-Time", "Part-Time", "Freelancer"});
    private JTextField contactField = new JTextField(20);
    private JTextField addressField = new JTextField(30);
    private JButton saveButton = new JButton("Save");
    private JButton cancelButton = new JButton("Cancel");

    public EmployeeManagement() {
        initializeUI();
        loadEmployeesIntoTable();
    }

    private void initializeUI() {
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[][grow]"));
        addButton = new JButton("Add Employee");
        employees = new ArrayList<>();

        // Sample employees
        employees.add(new Employee(1L, "John Smith", "Software Engineer", "IT",
                new Date(), "Active", "john.smith@company.com", "123 Main St"));
        employees.add(new Employee(2L, "Jane Doe", "HR Manager", "Human Resources",
                new Date(), "Active", "jane.doe@company.com", "456 Oak Ave"));

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
        if (row >= 0 && row < employees.size()) {
            employees.remove(row);
            loadEmployeesIntoTable();
        }
    }

    private void editEmployee(int row) {
        if (row >= 0 && row < employees.size()) {
            Employee employee = employees.get(row);
            showEditEmployeeDialog(employee);
        }
    }

    private void showAddEmployeeDialog() {
        // In showAddUserDialog() and similar methods
        JDialog addDialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Add New Employee",
                true
        );
        addDialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Reset fields
        fullNameField.setText("");
        jobTitleField.setText("");
        departmentField.setText("");
        hireDateField.setText("");
        statusCombo.setSelectedIndex(0);
        contactField.setText("");
        addressField.setText("");

        // Full Name
        addLabelAndField(addDialog, gbc, 0, "Full Name:", fullNameField);
        // Job Title
        addLabelAndField(addDialog, gbc, 1, "Job Title:", jobTitleField);
        // Department
        addLabelAndField(addDialog, gbc, 2, "Department:", departmentField);
        // Hire Date
        addLabelAndField(addDialog, gbc, 3, "Hire Date:", hireDateField);
        // Status
        addLabelAndCombo(addDialog, gbc, 4, "Status:", statusCombo);
        // Contact
        addLabelAndField(addDialog, gbc, 5, "Contact:", contactField);
        // Address
        addLabelAndField(addDialog, gbc, 6, "Address:", addressField);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        saveButton.addActionListener(e -> {
            // Create new employee
            Employee newEmployee = new Employee(
                    (long) (employees.size() + 1),
                    fullNameField.getText(),
                    jobTitleField.getText(),
                    departmentField.getText(),
                    new Date(), // Should parse date from field
                    (String) statusCombo.getSelectedItem(),
                    contactField.getText(),
                    addressField.getText()
            );
            employees.add(newEmployee);
            loadEmployeesIntoTable();
            addDialog.dispose();
        });
        cancelButton.addActionListener(e -> addDialog.dispose());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        addDialog.add(buttonPanel, gbc);

        addDialog.pack();
        addDialog.setLocationRelativeTo(this);
        addDialog.setVisible(true);
    }

    private void showEditEmployeeDialog(Employee employee) {
        JDialog editDialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Edit Employee",
                true
        );
        editDialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Initialize fields
        fullNameField.setText(employee.getFullName());
        jobTitleField.setText(employee.getJobTitle());
        departmentField.setText(employee.getDepartment());
        hireDateField.setText(employee.getHireDate().toString());
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

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        saveButton.addActionListener(e -> {
            employee.setFullName(fullNameField.getText());
            employee.setJobTitle(jobTitleField.getText());
            employee.setDepartment(departmentField.getText());
            // Should parse date properly
            employee.setEmploymentStatus((String) statusCombo.getSelectedItem());
            employee.setContactInformation(contactField.getText());
            employee.setAddress(addressField.getText());
            loadEmployeesIntoTable();
            editDialog.dispose();
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
        tableModel.setRowCount(0);
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
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EmployeeManagement::new);
    }
}