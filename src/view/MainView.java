package view;

import model.Transaction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MainView extends JFrame {
    private List<Transaction> transactions = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JTable transactionTable;
    private DashboardPanel dashboardPanel;
    private JPanel transactionsPanel;
    private final File saveFile = new File("src/data/transactions.dat");

    public MainView() {
        setTitle("Spendid - Student Finance Tracker");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        Font customFont;
        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/SF-Pro-Display-Medium.ttf")).deriveFont(14f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
        } catch (Exception e) {
            customFont = new Font("SF Pro Display", Font.BOLD, 14);
        }

        try {
            Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/icon.png"));
            setIconImage(icon);
        } catch (Exception e) {
            System.err.println("Icon not found or failed to load.");
        }

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(customFont);
        tabbedPane.setFocusable(false);
        tabbedPane.setBackground(new Color(24, 24, 24));
        tabbedPane.setForeground(Color.WHITE);
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
                    boolean isSelected) {
                g.setColor(isSelected ? new Color(33, 150, 243) : new Color(44, 44, 44));
                g.fillRoundRect(x + 2, y + 2, w - 4, h - 4, 10, 10);
            }

            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
                    boolean isSelected) {
            }
        });
        add(tabbedPane, BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(30, 30, 30));
        JMenu fileMenu = new JMenu("File");
        fileMenu.setForeground(Color.WHITE);
        fileMenu.setFont(customFont);

        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem loadItem = new JMenuItem("Load");
        JMenuItem exportItem = new JMenuItem("Export CSV");

        saveItem.setFont(customFont);
        loadItem.setFont(customFont);
        exportItem.setFont(customFont);

        saveItem.addActionListener(e -> {
            saveTransactions();
            JOptionPane.showMessageDialog(this, "Saved " + transactions.size() + " transactions.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        });

        loadItem.addActionListener(e -> {
            loadTransactions();
            updateTable();
            dashboardPanel.updateDashboard(transactions);
            JOptionPane.showMessageDialog(this, "Loaded " + transactions.size() + " transactions.", "Loaded", JOptionPane.INFORMATION_MESSAGE);
        });

        exportItem.addActionListener(e -> exportToCSV());

        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        fileMenu.add(exportItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        dashboardPanel = new DashboardPanel(transactions);
        tabbedPane.addTab("Dashboard", dashboardPanel);

        transactionsPanel = new JPanel(new BorderLayout());
        transactionsPanel.setBackground(new Color(24, 24, 24));

        String[] columns = { "Type", "Category", "Amount", "Date", "Notes" };
        tableModel = new DefaultTableModel(columns, 0);
        transactionTable = new JTable(tableModel) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        transactionTable.setFont(customFont.deriveFont(13f));
        transactionTable.setRowHeight(26);
        transactionTable.setBackground(new Color(36, 36, 36));
        transactionTable.setForeground(Color.WHITE);
        transactionTable.setSelectionBackground(new Color(60, 60, 60));
        transactionTable.getTableHeader().setFont(customFont);
        transactionTable.getTableHeader().setBackground(new Color(20, 20, 20));
        transactionTable.getTableHeader().setForeground(Color.WHITE);
        transactionTable.setGridColor(new Color(50, 50, 50));

        transactionTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && transactionTable.getSelectedRow() != -1) {
                    int row = transactionTable.getSelectedRow();
                    Transaction t = transactions.get(row);
                    showEditTransactionDialog(row, t);
                }
            }
        });

        transactionsPanel.add(new JScrollPane(transactionTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(24, 24, 24));
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton addBtn = new JButton("Add Transaction");
        JButton deleteBtn = new JButton("Delete Selected");

        styleButton(addBtn, new Color(0x1976D2), customFont);
        styleButton(deleteBtn, new Color(0xD32F2F), customFont);

        addBtn.addActionListener(e -> showAddTransactionDialog());
        deleteBtn.addActionListener(e -> {
            int selectedRow = transactionTable.getSelectedRow();
            if (selectedRow != -1) {
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this transaction?",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    transactions.remove(selectedRow);
                    tableModel.removeRow(selectedRow);
                    dashboardPanel.updateDashboard(transactions);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a transaction to delete.", "No Selection",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        buttonPanel.add(addBtn);
        buttonPanel.add(deleteBtn);
        transactionsPanel.add(buttonPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Transactions", transactionsPanel);

        loadTransactions();
        updateTable();
        dashboardPanel.updateDashboard(transactions);
        setVisible(true);
    }

    private void styleButton(JButton button, Color bgColor, Font font) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(font);
        button.setPreferredSize(new Dimension(160, 36));
    }

    private void showAddTransactionDialog() {
        ButtonGroup typeGroup = new ButtonGroup();
        JRadioButton incomeBtn = new JRadioButton("Income");
        JRadioButton expenseBtn = new JRadioButton("Expense");
        typeGroup.add(incomeBtn);
        typeGroup.add(expenseBtn);

        String[] categories = { "Allowance", "Food", "Transport", "Tuition", "Entertainment", "Misc" };
        JComboBox<String> categoryCombo = new JComboBox<>(categories);

        JTextField amountField = new JTextField();

        amountField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private boolean isUpdating = false;

            private void formatAmount() {
                if (isUpdating)
                    return;

                SwingUtilities.invokeLater(() -> {
                    String text = amountField.getText().replaceAll(",", "").trim();
                    if (text.isEmpty())
                        return;

                    try {
                        long number = Long.parseLong(text);
                        String formatted = String.format("%,d", number);

                        // Only update if different to avoid infinite loop
                        if (!formatted.equals(amountField.getText())) {
                            isUpdating = true;
                            amountField.setText(formatted);
                            isUpdating = false;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                });
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                formatAmount();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                formatAmount();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                formatAmount();
            }
        });

        JTextField dateField = new JTextField(LocalDate.now().toString());
        JTextArea notesArea = new JTextArea(3, 20);

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Type:"));
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.add(incomeBtn);
        typePanel.add(expenseBtn);
        panel.add(typePanel);

        panel.add(new JLabel("Category:"));
        panel.add(categoryCombo);
        panel.add(new JLabel("Amount (₱):"));
        panel.add(amountField);
        panel.add(new JLabel("Date (YYYY-MM-DD):"));
        panel.add(dateField);
        panel.add(new JLabel("Notes (optional):"));
        panel.add(new JScrollPane(notesArea));

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Transaction", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String type = incomeBtn.isSelected() ? "Income" : expenseBtn.isSelected() ? "Expense" : "";
                if (type.isEmpty())
                    throw new IllegalArgumentException("Select a transaction type.");

                String category = (String) categoryCombo.getSelectedItem();

                // Remove commas before parsing
                String rawAmount = amountField.getText().replace(",", "");
                double amount = Double.parseDouble(rawAmount);

                LocalDate date = LocalDate.parse(dateField.getText());
                String notes = notesArea.getText();

                Transaction t = new Transaction(type, category, amount, date, notes);
                transactions.add(t);

                updateTable();
                dashboardPanel.updateDashboard(transactions);
                saveTransactions();

            } catch (Exception ex) {

            }
        }
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        for (Transaction t : transactions) {
            tableModel.addRow(new Object[] {
                    t.getType(), t.getCategory(), String.format("₱%,.2f", t.getAmount()), t.getDate().toString(),
                    t.getNotes()
            });
        }
    }

    private void saveTransactions() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile))) {
            oos.writeObject(transactions);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTransactions() {
        if (saveFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFile))) {
                transactions = (List<Transaction>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(this, "Failed to load.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportToCSV() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("src/data/transactions.csv"))) {
            writer.println("Type,Category,Amount,Date,Notes");
            for (Transaction t : transactions) {
                writer.printf("%s,%s,%.2f,%s,%s\n", t.getType(), t.getCategory(), t.getAmount(), t.getDate(), t.getNotes().replaceAll(",", " "));
            }
            JOptionPane.showMessageDialog(this, "Exported to CSV successfully.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "CSV export failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showEditTransactionDialog(int index, Transaction t) {
        ButtonGroup typeGroup = new ButtonGroup();
        JRadioButton incomeBtn = new JRadioButton("Income");
        JRadioButton expenseBtn = new JRadioButton("Expense");
        if (t.getType().equals("Income"))
            incomeBtn.setSelected(true);
        else
            expenseBtn.setSelected(true);
        typeGroup.add(incomeBtn);
        typeGroup.add(expenseBtn);

        String[] categories = { "Allowance", "Food", "Transport", "Tuition", "Entertainment", "Misc" };
        JComboBox<String> categoryCombo = new JComboBox<>(categories);
        categoryCombo.setSelectedItem(t.getCategory());

        JTextField amountField = new JTextField(String.format("%,.2f", t.getAmount()));
        JTextField dateField = new JTextField(t.getDate().toString());
        JTextArea notesArea = new JTextArea(t.getNotes(), 3, 20);

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Type:"));
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.add(incomeBtn);
        typePanel.add(expenseBtn);
        panel.add(typePanel);

        panel.add(new JLabel("Category:"));
        panel.add(categoryCombo);
        panel.add(new JLabel("Amount (₱):"));
        panel.add(amountField);
        panel.add(new JLabel("Date (YYYY-MM-DD):"));
        panel.add(dateField);
        panel.add(new JLabel("Notes (optional):"));
        panel.add(new JScrollPane(notesArea));

        int result = JOptionPane.showOptionDialog(this, panel, "Edit Transaction",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[] { "Save", "Delete", "Cancel" }, "Save");

        if (result == 0) { // Save
            try {
                String type = incomeBtn.isSelected() ? "Income" : "Expense";
                String category = (String) categoryCombo.getSelectedItem();
                String rawAmount = amountField.getText().replace(",", "");
                double amount = Double.parseDouble(rawAmount);
                LocalDate date = LocalDate.parse(dateField.getText());
                String notes = notesArea.getText();

                Transaction updated = new Transaction(type, category, amount, date, notes);
                transactions.set(index, updated);
                updateTable();
                dashboardPanel.updateDashboard(transactions);
            } catch (Exception ex) {
            }
        } else if (result == 1) { // Delete
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this transaction?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                transactions.remove(index);
                updateTable();
                dashboardPanel.updateDashboard(transactions);
            }
        }
    }

}
