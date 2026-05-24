package com.example.finance.service;

import com.example.finance.dto.ReportMonthlyResponse;
import com.example.finance.dto.ReportYearlyResponse;
import com.example.finance.entity.CategoryEntity;
import com.example.finance.entity.CategoryType;
import com.example.finance.entity.TransactionEntity;
import com.example.finance.entity.UserEntity;
import com.example.finance.exception.BadRequestException;
import com.example.finance.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ReportService reportService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new UserEntity();
        user.setId(1L);
        user.setUsername("test@example.com");
    }

    @Test
    void getMonthlyReport_success() {
        when(userService.getCurrentUser()).thenReturn(user);

        CategoryEntity incomeCat = new CategoryEntity();
        incomeCat.setType(CategoryType.INCOME);
        incomeCat.setName("Salary");

        CategoryEntity expenseCat = new CategoryEntity();
        expenseCat.setType(CategoryType.EXPENSE);
        expenseCat.setName("Food");

        TransactionEntity tx1 = new TransactionEntity();
        tx1.setAmount(BigDecimal.valueOf(5000));
        tx1.setCategory(incomeCat);

        TransactionEntity tx2 = new TransactionEntity();
        tx2.setAmount(BigDecimal.valueOf(1200));
        tx2.setCategory(expenseCat);

        when(transactionRepository.findByUserAndDateBetween(eq(user), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(tx1, tx2));

        ReportMonthlyResponse result = reportService.getMonthlyReport(2024, 1);

        assertNotNull(result);
        assertEquals(1, result.getMonth());
        assertEquals(2024, result.getYear());
        assertEquals(BigDecimal.valueOf(5000), result.getTotalIncome().get("Salary"));
        assertEquals(BigDecimal.valueOf(1200), result.getTotalExpenses().get("Food"));
        assertEquals(BigDecimal.valueOf(3800), result.getNetSavings());
    }

    @Test
    void getMonthlyReport_invalidMonth_throwsBadRequestException() {
        assertThrows(BadRequestException.class, () -> reportService.getMonthlyReport(2024, 0));
        assertThrows(BadRequestException.class, () -> reportService.getMonthlyReport(2024, 13));
    }

    @Test
    void getYearlyReport_success() {
        when(userService.getCurrentUser()).thenReturn(user);

        CategoryEntity incomeCat = new CategoryEntity();
        incomeCat.setType(CategoryType.INCOME);
        incomeCat.setName("Salary");

        CategoryEntity expenseCat = new CategoryEntity();
        expenseCat.setType(CategoryType.EXPENSE);
        expenseCat.setName("Food");

        TransactionEntity tx1 = new TransactionEntity();
        tx1.setAmount(BigDecimal.valueOf(60000));
        tx1.setCategory(incomeCat);

        TransactionEntity tx2 = new TransactionEntity();
        tx2.setAmount(BigDecimal.valueOf(15000));
        tx2.setCategory(expenseCat);

        when(transactionRepository.findByUserAndDateBetween(eq(user), eq(LocalDate.of(2024, 1, 1)), eq(LocalDate.of(2024, 12, 31))))
                .thenReturn(Arrays.asList(tx1, tx2));

        ReportYearlyResponse result = reportService.getYearlyReport(2024);

        assertNotNull(result);
        assertEquals(2024, result.getYear());
        assertEquals(BigDecimal.valueOf(60000), result.getTotalIncome().get("Salary"));
        assertEquals(BigDecimal.valueOf(15000), result.getTotalExpenses().get("Food"));
        assertEquals(BigDecimal.valueOf(45000), result.getNetSavings());
    }
}
