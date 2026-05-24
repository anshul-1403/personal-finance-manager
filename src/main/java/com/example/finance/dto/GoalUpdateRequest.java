package com.example.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public class GoalUpdateRequest {

    private String goalName;

    @DecimalMin(value = "0.01", inclusive = true, message = "Target amount must be positive")
    private BigDecimal targetAmount;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Target date must use YYYY-MM-DD format")
    private String targetDate;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Start date must use YYYY-MM-DD format")
    private String startDate;

    public String getGoalName() {
        return goalName;
    }

    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public String getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(String targetDate) {
        this.targetDate = targetDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
}
