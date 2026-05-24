package com.finance.manager.dto;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public class TransactionUpdateRequest {

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal amount;

    private String category;

    private String description;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
