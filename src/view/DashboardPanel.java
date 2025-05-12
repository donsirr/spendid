package view;

import model.Transaction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardPanel extends JPanel {
    private List<Transaction> transactions;
    private Font customFont;
    private final Map<Rectangle, String> barTooltips = new HashMap<>();
    private final Color darkBackground = new Color(30, 30, 30);
    private final Color cardBackground = new Color(45, 45, 45);

    public DashboardPanel(List<Transaction> transactions) {
        this.transactions = transactions;
        setLayout(null); // we'll position manually
        setBackground(darkBackground);
        loadFont();

        ToolTipManager.sharedInstance().registerComponent(this);

        // Handle tooltips on hover
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

    private void loadFont() {
        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader()
                    .getResourceAsStream("fonts/SF-Pro-Display-Medium.ttf")).deriveFont(18f);
        } catch (Exception e) {
            customFont = getFont().deriveFont(Font.BOLD, 18f); // fallback
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
                .mapToDouble(t -> t.getAmount()).sum();
        double totalExpenses = transactions.stream().filter(t -> t.getType().equals("Expense"))
                .mapToDouble(t -> t.getAmount()).sum();
        double savingsGoal = 10000; // static for now

        // --- Metric Cards ---
        int cardWidth = width / 4;
        int cardHeight = 80;
        int spacing = 30;
        int topMargin = 20;

        drawMetricCard(g2, spacing, topMargin, cardWidth, cardHeight, "Total Income", totalIncome, new Color(0, 200, 0));
        drawMetricCard(g2, spacing * 2 + cardWidth, topMargin, cardWidth, cardHeight, "Total Expenses", totalExpenses, new Color(220, 20, 60));
        drawMetricCard(g2, spacing * 3 + cardWidth * 2, topMargin, cardWidth, cardHeight, "Savings Goal", savingsGoal, new Color(255, 215, 0));

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

        double maxAmount = Collections.max(categoryTotals.values());
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

            // Tooltip mapping
            barTooltips.put(bar, entry.getKey() + ": ₱" + String.format("%,.2f", amount));

            // Draw category label
            g2.setColor(Color.WHITE);
            g2.setFont(getFont().deriveFont(Font.PLAIN, 12f));
            FontMetrics fm = g2.getFontMetrics();
            int labelX = x + (barWidth - fm.stringWidth(entry.getKey())) / 2;
            g2.drawString(entry.getKey(), labelX, chartTop + chartHeight + 15);

            x += barWidth + gap;
        }
    }

    private void drawMetricCard(Graphics2D g2, int x, int y, int w, int h, String title, double amount, Color borderColor) {
        g2.setColor(borderColor);
        g2.fillRoundRect(x - 2, y - 2, w + 4, h + 4, 12, 12); // outer shadow

        g2.setColor(cardBackground);
        g2.fillRoundRect(x, y, w, h, 12, 12);

        g2.setColor(Color.WHITE);
        g2.setFont(customFont.deriveFont(Font.PLAIN, 14f));
        g2.drawString(title, x + 15, y + 25);

        g2.setFont(customFont.deriveFont(Font.BOLD, 20f));
        g2.drawString("₱" + String.format("%,.2f", amount), x + 15, y + 55);
    }
}
