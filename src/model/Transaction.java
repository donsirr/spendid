package model;

import java.io.Serializable;
import java.time.LocalDate;

public class Transaction implements Serializable {
    private String type;
    private String category;
    private double amount;
    private LocalDate date;
    private String notes;

    public Transaction(String type, String category, double amount, LocalDate date, String notes) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.notes = notes;
    }

    public String getType() { return type; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public String getNotes() { return notes; }
}
