package view;

import model.Transaction;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class AddTransactionDialog extends JDialog {
    private boolean confirmed = false;
    private Transaction transaction;

    public AddTransactionDialog(JFrame parent) {
        super(parent, "Add Transaction", true);
        setLayout(new BorderLayout());
        setSize(400, 350);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ButtonGroup typeGroup = new ButtonGroup();
        JRadioButton incomeBtn = new JRadioButton("Income");
        JRadioButton expenseBtn = new JRadioButton("Expense");
        typeGroup.add(incomeBtn);
        typeGroup.add(expenseBtn);

        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.add(incomeBtn);
        typePanel.add(expenseBtn);

        String[] categories = {"Allowance", "Food", "Transport", "Tuition", "Entertainment", "Misc"};
        JComboBox<String> categoryCombo = new JComboBox<>(categories);
        JTextField amountField = new JTextField();
        JTextField dateField = new JTextField(LocalDate.now().toString());
        JTextArea notesArea = new JTextArea(3, 20);

        panel.add(new JLabel("Type:"));
        panel.add(typePanel);
        panel.add(new JLabel("Category:"));
        panel.add(categoryCombo);
        panel.add(new JLabel("Amount (₱):"));
        panel.add(amountField);
        panel.add(new JLabel("Date (YYYY-MM-DD):"));
        panel.add(dateField);
        panel.add(new JLabel("Notes:"));
        panel.add(new JScrollPane(notesArea));

        add(panel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add");
        JButton cancelButton = new JButton("Cancel");

        addButton.addActionListener(e -> {
            try {
                String type = incomeBtn.isSelected() ? "Income" : expenseBtn.isSelected() ? "Expense" : "";
                if (type.isEmpty()) throw new IllegalArgumentException("Please select a type.");

                String category = (String) categoryCombo.getSelectedItem();
                double amount = Double.parseDouble(amountField.getText().replace(",", ""));
                LocalDate date = LocalDate.parse(dateField.getText());
                String notes = notesArea.getText();

                transaction = new Transaction(type, category, amount, date, notes);
                confirmed = true;
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Transaction getTransaction() {
        return transaction;
    }
}