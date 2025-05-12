package view;

import model.Transaction;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardPanel extends JPanel {
    private List<Transaction> transactions;
    private Font customFont;
    private final Map<Rectangle, String> barTooltips = new HashMap<>();
    private final Color darkBackground = new Color(30, 30, 30);
    private final Color cardBackground = new Color(45, 45, 45);
    private double savingsGoal = 10000;
    private final String SETTINGS_PATH = "settings.json";
    private JLabel editIconLabel;

    public DashboardPanel(List<Transaction> transactions) {
        this.transactions = transactions;
        setLayout(null);
        setBackground(darkBackground);
        loadFont();
        loadSettings();

        ToolTipManager.sharedInstance().registerComponent(this);
        setupMouseTooltip();
        addEditButton();
    }

    private void setupMouseTooltip() {
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                for (Map.Entry<Rectangle, String> entry : barTooltips.entrySet()) {
                    if (entry.getKey().contains(e.getPoint())) {
                        setToolTipText(entry.getValue());
                        return;
                    }
                }
                setToolTipText(null);
            }
        });
    }

    private void addEditButton() {
        ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource("img/pencil.png"));
        Image scaled = icon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH);
        editIconLabel = new JLabel(new ImageIcon(scaled));
        editIconLabel.setToolTipText("Edit savings goal");
        editIconLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        editIconLabel.setBounds(3, 0, 16, 16); // will be positioned dynamically later
        editIconLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String input = JOptionPane.showInputDialog(DashboardPanel.this, "Enter new savings goal:", savingsGoal);
                if (input != null && !input.isBlank()) {
                    try {
                        savingsGoal = Double.parseDouble(input);
                        saveSettings();
                        repaint();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(DashboardPanel.this, "Invalid number.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        add(editIconLabel);
    }

    private void loadFont() {
        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader()
                    .getResourceAsStream("fonts/SF-Pro-Display-Medium.ttf")).deriveFont(18f);
        } catch (Exception e) {
            customFont = getFont().deriveFont(Font.BOLD, 18f);
        }
    }

    private void saveSettings() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SETTINGS_PATH))) {
            JSONObject obj = new JSONObject();
            obj.put("savingsGoal", savingsGoal);
            writer.write(obj.toString(2));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSettings() {
        File file = new File(SETTINGS_PATH);
        if (file.exists()) {
            try {
                String json = new String(java.nio.file.Files.readAllBytes(file.toPath()));
                JSONObject obj = new JSONObject(json);
                savingsGoal = obj.optDouble("savingsGoal", 10000);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateDashboard(List<Transaction> transactions) {
        this.transactions = transactions;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (customFont == null) loadFont();

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        double totalIncome = transactions.stream().filter(t -> t.getType().equals("Income"))
                .mapToDouble(Transaction::getAmount).sum();
        double totalExpenses = transactions.stream().filter(t -> t.getType().equals("Expense"))
                .mapToDouble(Transaction::getAmount).sum();

        // --- Metric Cards ---
        int cardWidth = width / 4;
        int cardHeight = 80;
        int spacing = 30;
        int topMargin = 20;

        drawMetricCard(g2, spacing, topMargin, cardWidth, cardHeight, "Total Income", totalIncome, new Color(0, 200, 0));
        drawMetricCard(g2, spacing * 2 + cardWidth, topMargin, cardWidth, cardHeight, "Total Expenses", totalExpenses, new Color(220, 20, 60));
        drawMetricCard(g2, spacing * 3 + cardWidth * 2, topMargin, cardWidth, cardHeight, "Savings Goal", savingsGoal, new Color(255, 215, 0));

        // Position edit icon beside "Savings Goal"
        int iconX = spacing * 3 + cardWidth * 2 + 110;
        int iconY = topMargin + 8;
        editIconLabel.setBounds(iconX, iconY, 16, 16);

        // --- Bar Chart ---
        int chartTop = topMargin + cardHeight + 40;
        int chartLeft = 50;
        int chartWidth = width - 100;
        int chartHeight = height - chartTop - 40;

        List<Transaction> expenseTx = transactions.stream()
                .filter(t -> t.getType().equals("Expense"))
                .collect(Collectors.toList());

        Map<String, Double> categoryTotals = new HashMap<>();
        for (Transaction t : expenseTx) {
            categoryTotals.merge(t.getCategory(), t.getAmount(), Double::sum);
        }

        if (categoryTotals.isEmpty()) return;

        double maxAmount = Math.max(Collections.max(categoryTotals.values()), savingsGoal);
        int barWidth = 50;
        int gap = 30;

        int totalBars = categoryTotals.size();
        int totalWidth = (barWidth + gap) * totalBars - gap;
        int startX = chartLeft + (chartWidth - totalWidth) / 2;

        int x = startX;
        barTooltips.clear();

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            double amount = entry.getValue();
            int barHeight = (int) ((amount / maxAmount) * chartHeight);
            int y = chartTop + chartHeight - barHeight;

            Rectangle bar = new Rectangle(x, y, barWidth, barHeight);
            g2.setColor(new Color(100, 181, 246));
            g2.fill(bar);

            barTooltips.put(bar, entry.getKey() + ": ₱" + String.format("%,.2f", amount));

            g2.setColor(Color.WHITE);
            g2.setFont(getFont().deriveFont(Font.PLAIN, 12f));
            FontMetrics fm = g2.getFontMetrics();
            int labelX = x + (barWidth - fm.stringWidth(entry.getKey())) / 2;
            g2.drawString(entry.getKey(), labelX, chartTop + chartHeight + 15);

            x += barWidth + gap;
        }

        // Draw savings goal as yellow bar
        int goalHeight = (int) ((savingsGoal / maxAmount) * chartHeight);
        int y = chartTop + chartHeight - goalHeight;
        g2.setColor(new Color(255, 215, 0, 180));
        g2.fillRect(chartLeft, y, chartWidth, 4);
    }

    private void drawMetricCard(Graphics2D g2, int x, int y, int w, int h, String title, double amount, Color borderColor) {
        g2.setColor(borderColor);
        g2.fillRoundRect(x - 2, y - 2, w + 4, h + 4, 12, 12);

        g2.setColor(cardBackground);
        g2.fillRoundRect(x, y, w, h, 12, 12);

        g2.setColor(Color.WHITE);
        g2.setFont(customFont.deriveFont(Font.PLAIN, 14f));
        g2.drawString(title, x + 15, y + 25);

        g2.setFont(customFont.deriveFont(Font.BOLD, 20f));
        g2.drawString("₱" + String.format("%,.2f", amount), x + 15, y + 55);
    }
}
