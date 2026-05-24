package com.example.finance.dto;

import java.math.BigDecimal;

public class GoalResponse {
    private Long id;
    private String goalName;
    private BigDecimal targetAmount;
    private String targetDate;
    private String startDate;
    private BigDecimal currentProgress;
    private double progressPercentage;
    private BigDecimal remainingAmount;

    public GoalResponse() {
    }

    public GoalResponse(Long id, String goalName, BigDecimal targetAmount, String targetDate, String startDate,
                        BigDecimal currentProgress, double progressPercentage, BigDecimal remainingAmount) {
        this.id = id;
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.targetDate = targetDate;
        this.startDate = startDate;
        this.currentProgress = currentProgress;
        this.progressPercentage = progressPercentage;
        this.remainingAmount = remainingAmount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public BigDecimal getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(BigDecimal currentProgress) {
        this.currentProgress = currentProgress;
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }
}
