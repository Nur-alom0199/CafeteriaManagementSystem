import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.table.DefaultTableModel;
import static java.sql.DriverManager.getConnection;

public class CafeteriaManagementSystem extends JFrame {
    private static Connection conn;
    private static final String ADMIN_PASSWORD  = "admin123"; // Secure this in production
    private int custId = 1;
    private float totalIncome = 0;
    private int totalCustomers = 0;
    public CafeteriaManagementSystem() {
        setTitle("Cafeteria Management System");
        setSize(1400, 1000);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        // Add an image to the top (logo)
        JLabel logoLabel = new JLabel();
        try {
            ImageIcon logoIcon = new ImageIcon("cafe.png");
            logoLabel.setIcon(logoIcon);
        } catch (Exception e) {
            System.out.println("Logo not found!");
        }
        logoLabel.setHorizontalAlignment(JLabel.CENTER);
        add(logoLabel, BorderLayout.NORTH); // Add logo at the top



        // Main Menu Buttons
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));

        JButton foodPartBtn = new JButton("Food Part");
        JButton adminPanelBtn = new JButton("Admin Panel");
        JButton manageEmployeesBtn = new JButton("Manage Employees");
        JButton exitBtn = new JButton("Exit");

        // Setting button fonts and colors
        Font buttonFont = new Font("Arial", Font.BOLD, 50);
        foodPartBtn.setFont(buttonFont);
        adminPanelBtn.setFont(buttonFont);
        manageEmployeesBtn.setFont(buttonFont);
        exitBtn.setFont(buttonFont);

        foodPartBtn.setBackground(Color.GREEN);
        adminPanelBtn.setBackground(Color.orange);
        manageEmployeesBtn.setBackground(new Color(255, 255, 255));
        manageEmployeesBtn.setForeground(Color.BLACK);
        exitBtn.setBackground(Color.RED);
        exitBtn.setForeground(Color.WHITE);

        foodPartBtn.addActionListener(e -> showFoodPart());
        adminPanelBtn.addActionListener(e -> promptAdminPassword());
        manageEmployeesBtn.addActionListener(e -> showEmployeeManager());
        exitBtn.addActionListener(e -> System.exit(0));

        mainPanel.add(Box.createVerticalStrut(10)); // Adding space between buttons
        mainPanel.add(foodPartBtn);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(adminPanelBtn);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(manageEmployeesBtn);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(exitBtn);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 400));
        add(mainPanel, BorderLayout.CENTER);
        setVisible(true);  // <-- This is very important!
        revalidate();  // <-- Ensures that the components are laid out correctly
        repaint();
        // Initialize the database connection
        connectToDatabase();
    }


    // Establish database connection
    private void connectToDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/cafeteria"; // Update with your DB name
            String username = "root"; // Your MySQL username
            String password = ""; // Your MySQL password (blank if none)

            conn = getConnection(url, username, password);
            System.out.println("Database connected successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Cannot connect to database.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Show Food Part Menu
    private void showFoodPart() {
        JFrame foodFrame = new JFrame("Food Part");
        foodFrame.setSize(600, 600);
        foodFrame.setLocationRelativeTo(null);
        // Load the image
        ImageIcon imageIcon = new ImageIcon("item.png");
        if (imageIcon.getImageLoadStatus() == MediaTracker.ERRORED) {
            System.out.println("Image failed to load.");
        }
        JLabel imageLabel = new JLabel(imageIcon);

        // Create buttons with custom fonts and colors
        JButton foodListBtn = new JButton("Food List");
        foodListBtn.setFont(new Font("Arial", Font.BOLD, 30));
        foodListBtn.setBackground(new Color(24, 204, 0, 219));
        foodListBtn.setForeground(Color.BLACK);
        foodListBtn.setOpaque(true);
        foodListBtn.setBorderPainted(false);

        JButton orderBtn = new JButton("Order Food");
        orderBtn.setFont(new Font("Arial", Font.BOLD, 30));
        orderBtn.setBackground(new Color(0, 204, 163, 255));
        orderBtn.setForeground(Color.BLACK); // White text
        orderBtn.setOpaque(true);
        orderBtn.setBorderPainted(false);

        // Add action listeners to buttons
        foodListBtn.addActionListener(e -> displayMenu());
        orderBtn.addActionListener(e -> placeOrder());

        // Create a panel with GridBagLayout to center the buttons
        JPanel overlayPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        overlayPanel.add(foodListBtn, gbc);

        gbc.gridy = 1;
        overlayPanel.add(orderBtn, gbc);

        overlayPanel.setOpaque(false); // Transparent background for the overlay panel

        // Create a layered panel to hold the image and buttons overlay
        JLayeredPane layeredPane = new JLayeredPane();
        imageLabel.setBounds(0, 0, 600, 400); // Set bounds for the image
        overlayPanel.setBounds(0, 0, 600, 400); // Set bounds for the overlay

        layeredPane.add(imageLabel, Integer.valueOf(0)); // Background image layer
        layeredPane.add(overlayPanel, Integer.valueOf(1)); // Buttons overlay layer

        // Add layered pane to main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        setLocationRelativeTo(null);
        mainPanel.add(layeredPane, BorderLayout.CENTER);

        foodFrame.add(mainPanel);
        foodFrame.setVisible(true);
    }
    // Fetch and display the menu from the database
    private void displayMenu() {
        JFrame menuFrame = new JFrame("Menu");
        menuFrame.setSize(600, 600);

        String[] columnNames = {"ID", "Name", "Price ($)", "Rating"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0); // Initialize table model

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM items")) {
            while (rs.next()) {
                int id = rs.getInt("food_id");
                String name = rs.getString("name");
                float price = rs.getFloat("price");
                float rating = rs.getFloat("rating");

                Object[] row = {id, name, price, rating};
                tableModel.addRow(row); // Add row to the table model
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching menu.", "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTable menuTable = new JTable(tableModel);
        menuTable.setFont(new Font("Arial", Font.PLAIN, 16));
        menuTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(menuTable);
        menuFrame.add(scrollPane);

        menuFrame.setVisible(true);
    }

    // Place an order and save it to the database
    private void placeOrder() {
        JFrame orderFrame = new JFrame("Place Order");
        orderFrame.setSize(500, 400);

        // Labels
        JLabel nameLabel = new JLabel("      Your Name: ");
        JLabel dateLabel = new JLabel("      Today's Date (yyyy-mm-dd): ");
        JLabel foodIdLabel = new JLabel("      Food ID: ");
        JLabel quantityLabel = new JLabel("     Quantity: ");

        // Text Fields
        JTextField nameField = new JTextField(10);
        JTextField dateField = new JTextField(10);
        JTextField foodIdField = new JTextField(5);
        JTextField quantityField = new JTextField(5);

        // Buttons with custom colors
        JButton addButton = new JButton("Add Item");
        addButton.setFont(new Font("Arial", Font.BOLD, 25));
        addButton.setBackground(new Color(0, 153, 76)); // Green background
        addButton.setForeground(Color.BLACK);
        addButton.setOpaque(true);
        addButton.setBorderPainted(false);

        JButton submitButton = new JButton("Submit Order");
        submitButton.setFont(new Font("Arial", Font.BOLD, 25));
        submitButton.setBackground(new Color(0, 102, 204)); // Blue background
        submitButton.setForeground(Color.BLACK);
        submitButton.setOpaque(true);
        submitButton.setBorderPainted(false);

        // Panel layout setup
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 2, 10, 10));
        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(dateLabel);
        panel.add(dateField);
        panel.add(foodIdLabel);
        panel.add(foodIdField);
        panel.add(quantityLabel);
        panel.add(quantityField);
        panel.add(addButton);
        panel.add(submitButton);

        // Order information storage
        ArrayList<int[]> orderedItems = new ArrayList<>();
        float[] totalAmount = {0};

        // Action Listener for Add Item button
        addButton.addActionListener(e -> {
            try {
                int foodId = Integer.parseInt(foodIdField.getText());
                int quantity = Integer.parseInt(quantityField.getText());
                orderedItems.add(new int[]{foodId, quantity});

                String query = "SELECT price FROM items WHERE food_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, foodId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            float price = rs.getFloat("price");
                            totalAmount[0] += price * quantity;
                        }
                    }
                }
                JOptionPane.showMessageDialog(orderFrame, "Item added! Total: $" + totalAmount[0]);
            } catch (NumberFormatException | SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(orderFrame, "Invalid input or error fetching price.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
// Action Listener for Submit Order button
        submitButton.addActionListener(e -> {
            String name = nameField.getText();
            String date = dateField.getText();

            try {
                String query = "INSERT INTO order_history (customer_id, food_id, quantity, total_amount, order_date) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    for (int[] item : orderedItems) {
                        int foodId = item[0];
                        int quantity = item[1];

                        stmt.setInt(1, custId);
                        stmt.setInt(2, foodId);
                        stmt.setInt(3, quantity);
                        stmt.setFloat(4, totalAmount[0]);
                        stmt.setString(5, date);
                        stmt.executeUpdate();
                    }
                }
                totalIncome += totalAmount[0];
                totalCustomers++;
                custId++;
                JOptionPane.showMessageDialog(orderFrame, "Order submitted! Total: $" + totalAmount[0]);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(orderFrame, "Error submitting order.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        orderFrame.add(panel);
        orderFrame.setVisible(true);
    }

    // Prompt for Admin password
    private void promptAdminPassword() {
        // Create a password field
        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setEchoChar('*');

        // Create a custom dialog
        JDialog dialog = new JDialog((Frame) null, "Enter Admin Password", true);
        dialog.setLayout(new BorderLayout());

        // Create a panel to hold the password field
        JPanel panel = new JPanel(new FlowLayout());
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        // Create the OK and Cancel buttons
        JButton okButton = new JButton("OK");
        okButton.setBackground(Color.GREEN);
        okButton.setForeground(Color.BLACK);
        okButton.setFocusPainted(false);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(Color.RED);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);

        // Action listener for the OK button
        okButton.addActionListener(e -> {
            // Get the entered password
            char[] password = passwordField.getPassword();
            String passwordStr = new String(password);

            // Check if the password is correct
            if (passwordStr.equals(ADMIN_PASSWORD)) {
                showAdminPanel();
            } else {
                JOptionPane.showMessageDialog(dialog, "Incorrect Password.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            }

            // Clear the password
            passwordField.setText("");
            Arrays.fill(password, '0');
            dialog.dispose();
        });

        // Action listener for the Cancel button
        cancelButton.addActionListener(e -> dialog.dispose());

        // Create a panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        // Add panels to the dialog
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
    // Show Admin Panel
    private void showAdminPanel() {
        JFrame adminFrame = new JFrame("Admin Panel");
        adminFrame.setSize(600, 600);
        // adminFrame.setLocationRelativeTo(null);

        ImageIcon imageIcon = new ImageIcon("item.png");
        if (imageIcon.getImageLoadStatus() == MediaTracker.ERRORED) {
            System.out.println("Image failed to load.");
        }
        JLabel imageLabel = new JLabel(imageIcon);


        // Create buttons with custom colors and bold text
        JButton totalIncomeBtn = new JButton("View Total Income");
        totalIncomeBtn.setFont(new Font("Arial", Font.BOLD, 25));
        totalIncomeBtn.setBackground(new Color(0, 153, 255, 208)); // Light blue
        totalIncomeBtn.setForeground(Color.BLACK);
        totalIncomeBtn.setOpaque(true);
        totalIncomeBtn.setBorderPainted(false);

        JButton totalCustomersBtn = new JButton("View Total Customers");
        totalCustomersBtn.setFont(new Font("Arial", Font.BOLD, 25));
        totalCustomersBtn.setBackground(new Color(0, 204, 102)); // Light green
        totalCustomersBtn.setForeground(Color.BLACK);
        totalCustomersBtn.setOpaque(true);
        totalCustomersBtn.setBorderPainted(false);

        JButton addDishBtn = new JButton("Add New Dish");
        addDishBtn.setFont(new Font("Arial", Font.BOLD, 25));
        addDishBtn.setBackground(new Color(157, 255, 0)); // Orange
        addDishBtn.setForeground(Color.BLACK);
        addDishBtn.setOpaque(true);
        addDishBtn.setBorderPainted(false);

        JButton viewOrdersBtn = new JButton("View Orders");
        viewOrdersBtn.setFont(new Font("Arial", Font.BOLD, 25));
        viewOrdersBtn.setBackground(new Color(204, 0, 92, 142)); // Pink
        viewOrdersBtn.setForeground(Color.BLACK);
        viewOrdersBtn.setOpaque(true);
        viewOrdersBtn.setBorderPainted(false);

        // Action listeners for buttons
        totalIncomeBtn.addActionListener(e -> JOptionPane.showMessageDialog(adminFrame, "Total Income: " + totalIncome));
        totalCustomersBtn.addActionListener(e -> JOptionPane.showMessageDialog(adminFrame, "Total Customers: " + totalCustomers));
        addDishBtn.addActionListener(e -> addNewDish());
        viewOrdersBtn.addActionListener(e -> viewOrderHistory());

        // Create a panel with GridBagLayout to center the buttons over the image
        JPanel overlayPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Padding between buttons
        gbc.fill = GridBagConstraints.HORIZONTAL; // Buttons will take up full horizontal space

        gbc.gridx = 0;
        gbc.gridy = 0;
        overlayPanel.add(totalIncomeBtn, gbc);

        gbc.gridy = 1;
        overlayPanel.add(totalCustomersBtn, gbc);

        gbc.gridy = 2;
        overlayPanel.add(addDishBtn, gbc);

        gbc.gridy = 3;
        overlayPanel.add(viewOrdersBtn, gbc);

        overlayPanel.setOpaque(false); // Transparent background

        // Create a layered panel to hold the image and buttons overlay
        JLayeredPane layeredPane = new JLayeredPane();
        imageLabel.setBounds(0, 0, 600, 400); // Set bounds for the image
        overlayPanel.setBounds(0, 0, 600, 400); // Set bounds for the overlay

        layeredPane.add(imageLabel, Integer.valueOf(0)); // Background image
        layeredPane.add(overlayPanel, Integer.valueOf(1)); // Buttons overlay

        // Add layered pane to main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(layeredPane, BorderLayout.CENTER);

        adminFrame.add(mainPanel);
        adminFrame.setVisible(true);
    }

    // Add a new dish to the menu
    private void addNewDish() {
        JFrame addDishFrame = new JFrame("Add New Dish");
        addDishFrame.setSize(400, 300);

        // Labels and text fields for inputs
        JLabel nameLabel = new JLabel("Dish Name:");
        JTextField nameField = new JTextField(20);
        JLabel priceLabel = new JLabel("Price:");
        JTextField priceField = new JTextField(10);
        JLabel ratingLabel = new JLabel("Rating (1-5):");
        JTextField ratingField = new JTextField(5);

        // Customized Add Dish button
        JButton addButton = new JButton("Add Dish");
        addButton.setBackground(new Color(34, 139, 34)); // Forest green
        addButton.setForeground(Color.BLACK); // White text
        addButton.setFocusPainted(false); // No focus outline
        addButton.setFont(new Font("Arial", Font.BOLD, 14));

        // Action listener for the Add Dish button
        addButton.addActionListener(e -> {
            try {
                String name = nameField.getText();
                float price = Float.parseFloat(priceField.getText());
                float rating = Float.parseFloat(ratingField.getText());

                String query = "INSERT INTO items (name, price, rating) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, name);
                    stmt.setFloat(2, price);
                    stmt.setFloat(3, rating);
                    stmt.executeUpdate();
                }
                JOptionPane.showMessageDialog(addDishFrame, "Dish added successfully!");
            } catch (NumberFormatException | SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(addDishFrame, "Error adding dish. Check input values.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Layout configuration
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Padding

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(priceLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(priceField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(ratingLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(ratingField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(addButton, gbc);

        // Add the panel to the frame and show
        addDishFrame.add(panel);
        addDishFrame.setVisible(true);
    }

    public void viewOrderHistory() {
        JFrame ordersFrame = new JFrame("Order History");
        ordersFrame.setSize(800, 400);
        ordersFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Table model to hold data
        DefaultTableModel tableModel = new DefaultTableModel();
        JTable ordersTable = new JTable(tableModel);

        // Define table columns
        tableModel.addColumn("Order ID");
        tableModel.addColumn("Customer ID");
        tableModel.addColumn("Food ID");
        tableModel.addColumn("Quantity");
        tableModel.addColumn("Total Amount");
        tableModel.addColumn("Order Date");

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM order_history")) {
            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                int customerId = rs.getInt("customer_id");
                int foodId = rs.getInt("food_id");
                int quantity = rs.getInt("quantity");
                float total = rs.getFloat("total_amount");
                String date = rs.getString("order_date");

                // Add row to the table model
                tableModel.addRow(new Object[]{orderId, customerId, foodId, quantity, total, date});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(ordersFrame, "Error fetching order history.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        // Add table to scroll pane and frame
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        ordersFrame.add(scrollPane, BorderLayout.CENTER);

        ordersFrame.setVisible(true);
    }

        public void showEmployeeManager() {
            JFrame employeeFrame = new JFrame("Employee Manager");
            employeeFrame.setSize(800, 600);
            employeeFrame.setLocationRelativeTo(null);
            employeeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JPanel inputPanel = createInputPanel();
            JTextField[] inputFields = extractInputFields(inputPanel);

            JPanel buttonPanel = createButtonPanel(employeeFrame, inputFields);

            employeeFrame.setLayout(new BorderLayout());
            employeeFrame.add(inputPanel, BorderLayout.CENTER);
            employeeFrame.add(buttonPanel, BorderLayout.SOUTH);
            employeeFrame.setVisible(true);
        }

        private JPanel createInputPanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(6, 1, 10, 10));

            String[] fieldNames = {
                    "Employee ID (auto-generated)", "Employee Name", "Role (Manager/Waiter/etc.)",
                    "Salary", "Working Hours (per week)", "Password"
            };

            for (int i = 0; i < fieldNames.length; i++) {
                JTextField textField = i == 0 ? new JTextField() : (i == 5 ? new JPasswordField() : new JTextField());
                textField.setBorder(BorderFactory.createTitledBorder(fieldNames[i]));
                textField.setEditable(i != 0); // Make ID field non-editable
                panel.add(textField);
            }

            return panel;
        }

        private JTextField[] extractInputFields(JPanel panel) {
            return Arrays.stream(panel.getComponents())
                    .filter(c -> c instanceof JTextField)
                    .toArray(JTextField[]::new);
        }

        private JPanel createButtonPanel(JFrame frame, JTextField[] fields) {
            JPanel panel = new JPanel(new FlowLayout());

            JButton addBtn = createButton("Add Employee", Color.GREEN, Color.WHITE, e -> addEmployee(frame, fields));
            JButton updateBtn = createButton("Update Employee", Color.ORANGE, Color.BLACK, e -> updateEmployee(frame, fields));
            JButton removeBtn = createButton("Remove Employee", Color.RED, Color.WHITE, e -> removeEmployee(frame, fields));
            JButton listBtn = createButton("Show Employee List", Color.BLUE, Color.WHITE, e -> showEmployeeList(frame, fields));

            panel.add(addBtn);
            panel.add(updateBtn);
            panel.add(removeBtn);
            panel.add(listBtn);

            return panel;
        }

        private JButton createButton(String text, Color bgColor, Color fgColor, ActionListener action) {
            JButton button = new JButton(text);
            button.setBackground(bgColor);
            button.setForeground(fgColor);
            button.addActionListener(action);
            return button;
        }

        private boolean validateInputs(JFrame frame, JTextField[] fields, boolean includeId) {
            for (int i = (includeId ? 0 : 1); i < fields.length; i++) {
                if (fields[i].getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please fill out all fields!", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            return true;
        }

        private void addEmployee(JFrame frame, JTextField[] fields) {
            if (!validateInputs(frame, fields, false)) return;

            String query = "INSERT INTO employees (name, role, salary, working_hours, password) VALUES (?, ?, ?, ?, ?)";
            executeEmployeeQuery(frame, query, fields, false);
            JOptionPane.showMessageDialog(frame, "Employee Added Successfully!");
        }

        private void updateEmployee(JFrame frame, JTextField[] fields) {
            if (!validateInputs(frame, fields, true)) return;

            String query = "UPDATE employees SET name = ?, role = ?, salary = ?, working_hours = ?, password = ? WHERE employee_id = ?";
            executeEmployeeQuery(frame, query, fields, true);
            JOptionPane.showMessageDialog(frame, "Employee Updated Successfully!");
        }

        private void removeEmployee(JFrame frame, JTextField[] fields) {
            String idStr = fields[0].getText().trim();

            if (idStr.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please select an employee to remove!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String query = "DELETE FROM employees WHERE employee_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, Integer.parseInt(idStr));
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(frame, "Employee Removed Successfully!");
            } catch (SQLException ex) {
                handleSQLException(frame, ex);
            }
        }

        private void showEmployeeList(JFrame frame, JTextField[] fields) {
            try {
                String query = "SELECT * FROM employees";
                try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                    JFrame listFrame = new JFrame("Employee List");
                    listFrame.setSize(600, 400);
                    listFrame.setLocationRelativeTo(null);

                    DefaultTableModel tableModel = new DefaultTableModel();
                    JTable employeeTable = new JTable(tableModel);

                    String[] columns = {"ID", "Name", "Role", "Salary", "Working Hours"};
                    for (String column : columns) tableModel.addColumn(column);

                    while (rs.next()) {
                        tableModel.addRow(new Object[]{
                                rs.getInt("employee_id"), rs.getString("name"), rs.getString("role"),
                                rs.getBigDecimal("salary"), rs.getInt("working_hours")
                        });
                    }

                    employeeTable.getSelectionModel().addListSelectionListener(event -> {
                        if (!event.getValueIsAdjusting() && employeeTable.getSelectedRow() != -1) {
                            for (int i = 0; i < fields.length - 1; i++) {
                                fields[i].setText(tableModel.getValueAt(employeeTable.getSelectedRow(), i).toString());
                            }
                        }
                    });

                    JScrollPane scrollPane = new JScrollPane(employeeTable);
                    listFrame.add(scrollPane);
                    listFrame.setVisible(true);
                }
            } catch (SQLException ex) {
                handleSQLException(frame, ex);
            }
        }

        private void executeEmployeeQuery(JFrame frame, String query, JTextField[] fields, boolean includeId) {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                int i = 1;
                for (; i < fields.length; i++) {
                    stmt.setString(i, fields[i].getText());
                }
                if (includeId) stmt.setInt(i, Integer.parseInt(fields[0].getText()));
                stmt.executeUpdate();
            } catch (SQLException ex) {
                handleSQLException(frame, ex);
            }
        }

        private void handleSQLException(JFrame frame, SQLException ex) {
            JOptionPane.showMessageDialog(frame, "SQL Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(CafeteriaManagementSystem::new);
    }
}
