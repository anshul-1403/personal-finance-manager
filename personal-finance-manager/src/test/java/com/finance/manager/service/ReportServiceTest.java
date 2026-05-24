package com.finance.manager.service;

import com.finance.manager.dto.MonthlyReportResponse;
import com.finance.manager.dto.YearlyReportResponse;
import com.finance.manager.entity.Category;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.TransactionType;
import com.finance.manager.entity.User;
import com.finance.manager.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ReportService reportService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("test@test.com", "password", "Test", "1234");
        user.setId(1L);
    }

    @Test
    void testGetMonthlyReport() {
        Category salary = new Category("Salary", TransactionType.INCOME, false, null);
        Category rent = new Category("Rent", TransactionType.EXPENSE, false, null);

        Transaction t1 = new Transaction(new BigDecimal("5000"), LocalDate.of(2024, 1, 15), salary, "Salary", TransactionType.INCOME, user);
        Transaction t2 = new Transaction(new BigDecimal("1500"), LocalDate.of(2024, 1, 20), rent, "Rent", TransactionType.EXPENSE, user);

        when(transactionRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(t1, t2));

        MonthlyReportResponse report = reportService.getMonthlyReport(user, 2024, 1);

        assertNotNull(report);
        assertEquals(2024, report.getYear());
        assertEquals(1, report.getMonth());
        assertEquals(new BigDecimal("5000"), report.getTotalIncome().get("Salary"));
        assertEquals(new BigDecimal("1500"), report.getTotalExpenses().get("Rent"));
        assertEquals(new BigDecimal("3500"), report.getNetSavings());
    }

    @Test
    void testGetYearlyReport() {
        Category salary = new Category("Salary", TransactionType.INCOME, false, null);
        Category rent = new Category("Rent", TransactionType.EXPENSE, false, null);

        Transaction t1 = new Transaction(new BigDecimal("60000"), LocalDate.of(2024, 6, 15), salary, "Yearly Salary", TransactionType.INCOME, user);
        Transaction t2 = new Transaction(new BigDecimal("18000"), LocalDate.of(2024, 12, 20), rent, "Yearly Rent", TransactionType.EXPENSE, user);

        when(transactionRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(t1, t2));

        YearlyReportResponse report = reportService.getYearlyReport(user, 2024);

        assertNotNull(report);
        assertEquals(2024, report.getYear());
        assertEquals(new BigDecimal("60000"), report.getTotalIncome().get("Salary"));
        assertEquals(new BigDecimal("18000"), report.getTotalExpenses().get("Rent"));
        assertEquals(new BigDecimal("42000"), report.getNetSavings());
    }
}
