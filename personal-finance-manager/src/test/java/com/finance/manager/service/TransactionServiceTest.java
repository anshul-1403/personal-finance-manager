package com.finance.manager.service;

import com.finance.manager.dto.TransactionRequest;
import com.finance.manager.dto.TransactionResponse;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Category salaryCategory;

    @BeforeEach
    void setUp() {
        user = new User("test@test.com", "password", "Test User", "123456");
        user.setId(1L);

        salaryCategory = new Category("Salary", TransactionType.INCOME, false, null);
        salaryCategory.setId(10L);
    }

    @Test
    void testCreateTransaction() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("5000"));
        request.setDate(LocalDate.now());
        request.setCategory("Salary");
        request.setDescription("Jan Salary");

        when(categoryService.findValidCategoryForUser("Salary", user)).thenReturn(salaryCategory);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(100L);
            return t;
        });

        TransactionResponse response = transactionService.createTransaction(request, user);

        assertNotNull(response);
        assertEquals(new BigDecimal("5000"), response.getAmount());
        assertEquals("Salary", response.getCategory());
        assertEquals(TransactionType.INCOME, response.getType());
    }

    @Test
    void testGetTransactions() {
        Transaction t1 = new Transaction(new BigDecimal("5000"), LocalDate.now(), salaryCategory, "Jan Salary", TransactionType.INCOME, user);
        t1.setId(1L);
        when(transactionRepository.findByUserIdWithFilters(1L, null, null, null)).thenReturn(List.of(t1));

        List<TransactionResponse> transactions = transactionService.getTransactions(user, null, null, null);

        assertEquals(1, transactions.size());
        assertEquals(new BigDecimal("5000"), transactions.get(0).getAmount());
    }
}
