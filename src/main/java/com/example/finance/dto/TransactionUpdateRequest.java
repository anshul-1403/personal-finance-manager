package com.example.finance.dto;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public class TransactionUpdateRequest {

    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be a positive value")
    private BigDecimal amount;

    private String category;

    private String description;

    private String date; // accepted but ignored during update

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
