package view;

import model.Transaction;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class ChartPanel extends JPanel {
    private List<Transaction> transactions;

    public ChartPanel(List<Transaction> transactions) {
        this.transactions = transactions;
        setPreferredSize(new Dimension(400, 300));
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (transactions == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double income = 0, expense = 0;
        for (Transaction t : transactions) {
            if (t.getDate().getMonth() == LocalDate.now().getMonth() &&
                t.getDate().getYear() == LocalDate.now().getYear()) {
                if (t.getType().equalsIgnoreCase("Income")) income += t.getAmount();
                else if (t.getType().equalsIgnoreCase("Expense")) expense += t.getAmount();
            }
        }
        double savings = income - expense;

        double max = Math.max(income, Math.max(expense, savings));
        if (max == 0) max = 1; // avoid divide by zero

        int width = getWidth();
        int height = getHeight();
        int barWidth = width / 6;
        int xStart = width / 6;

        int[] values = {(int) income, (int) expense, (int) savings};
        String[] labels = {"Income", "Expense", "Savings"};
        Color[] colors = {Color.GREEN, Color.RED, Color.BLUE};

        for (int i = 0; i < 3; i++) {
            int barHeight = (int) ((values[i] / max) * (height - 60));
            int x = xStart + i * (barWidth + 20);
            int y = height - barHeight - 30;

            g2.setColor(colors[i]);
            g2.fillRect(x, y, barWidth, barHeight);

            g2.setColor(Color.BLACK);
            g2.drawString(labels[i], x + 5, height - 10);
            g2.drawString(String.format("₱%,.2f", (double) values[i]), x + 5, y - 5);
        }
    }
}
