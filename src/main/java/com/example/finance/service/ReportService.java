package com.example.finance.service;

import com.example.finance.dto.ReportMonthlyResponse;
import com.example.finance.dto.ReportYearlyResponse;
import com.example.finance.entity.TransactionEntity;
import com.example.finance.entity.UserEntity;
import com.example.finance.exception.BadRequestException;
import com.example.finance.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public ReportService(TransactionRepository transactionRepository, UserService userService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
    }

    public ReportMonthlyResponse getMonthlyReport(int year, int month) {
        if (month < 1 || month > 12) {
            throw new BadRequestException("Month must be between 1 and 12");
        }
        UserEntity currentUser = userService.getCurrentUser();
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        List<TransactionEntity> transactions = transactionRepository.findByUserAndDateBetween(currentUser, start, end);
        Map<String, BigDecimal> incomeByCategory = new HashMap<>();
        Map<String, BigDecimal> expenseByCategory = new HashMap<>();
        for (TransactionEntity tx : transactions) {
            if (tx.getCategory().getType().name().equals("INCOME")) {
                incomeByCategory.merge(tx.getCategory().getName(), tx.getAmount(), BigDecimal::add);
            } else {
                expenseByCategory.merge(tx.getCategory().getName(), tx.getAmount(), BigDecimal::add);
            }
        }
        BigDecimal netSavings = incomeByCategory.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .subtract(expenseByCategory.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));
        return new ReportMonthlyResponse(month, year, incomeByCategory, expenseByCategory, netSavings);
    }

    public ReportYearlyResponse getYearlyReport(int year) {
        UserEntity currentUser = userService.getCurrentUser();
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        List<TransactionEntity> transactions = transactionRepository.findByUserAndDateBetween(currentUser, start, end);
        Map<String, BigDecimal> incomeByCategory = new HashMap<>();
        Map<String, BigDecimal> expenseByCategory = new HashMap<>();
        for (TransactionEntity tx : transactions) {
            if (tx.getCategory().getType().name().equals("INCOME")) {
                incomeByCategory.merge(tx.getCategory().getName(), tx.getAmount(), BigDecimal::add);
            } else {
                expenseByCategory.merge(tx.getCategory().getName(), tx.getAmount(), BigDecimal::add);
            }
        }
        BigDecimal netSavings = incomeByCategory.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .subtract(expenseByCategory.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));
        return new ReportYearlyResponse(year, incomeByCategory, expenseByCategory, netSavings);
    }
}
