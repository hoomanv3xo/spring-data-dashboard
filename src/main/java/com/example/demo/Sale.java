package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String label;
    private double amount;
    private Double amount2;   // optional second value, used for the scatter plot and correlation

    protected Sale() {}

    public Sale(String label, double amount) {
        this(label, amount, null);
    }

    public Sale(String label, double amount, Double amount2) {
        this.label = label;
        this.amount = amount;
        this.amount2 = amount2;
    }

    public Long getId() { return id; }
    public String getLabel() { return label; }
    public double getAmount() { return amount; }
    public Double getAmount2() { return amount2; }
}

