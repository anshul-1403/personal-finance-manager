package com.example.finance.service;

import com.example.finance.dto.TransactionRequest;
import com.example.finance.dto.TransactionResponse;
import com.example.finance.dto.TransactionUpdateRequest;
import com.example.finance.entity.CategoryEntity;
import com.example.finance.entity.CategoryType;
import com.example.finance.entity.TransactionEntity;
import com.example.finance.entity.UserEntity;
import com.example.finance.exception.BadRequestException;
import com.example.finance.exception.NotFoundException;
import com.example.finance.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserService userService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private TransactionService transactionService;

    private UserEntity user;
    private CategoryEntity category;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new UserEntity();
        user.setId(1L);
        user.setUsername("test@example.com");

        category = new CategoryEntity();
        category.setName("Salary");
        category.setType(CategoryType.INCOME);
    }

    @Test
    void createTransaction_success() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(categoryService.requireCategoryByName("Salary")).thenReturn(category);

        TransactionRequest request = new TransactionRequest();
        request.setAmount(BigDecimal.valueOf(1000));
        request.setDate("2024-01-01");
        request.setCategory("Salary");
        request.setDescription("Bonus");

        TransactionEntity savedTx = new TransactionEntity();
        savedTx.setId(10L);
        savedTx.setAmount(BigDecimal.valueOf(1000));
        savedTx.setDate(LocalDate.of(2024, 1, 1));
        savedTx.setCategory(category);
        savedTx.setDescription("Bonus");
        savedTx.setUser(user);

        when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(savedTx);

        TransactionResponse result = transactionService.createTransaction(request);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(BigDecimal.valueOf(1000), result.getAmount());
        assertEquals("2024-01-01", result.getDate());
        assertEquals("Salary", result.getCategory());
        assertEquals("Bonus", result.getDescription());
        assertEquals("INCOME", result.getType());

        verify(transactionRepository).save(any(TransactionEntity.class));
    }

    @Test
    void createTransaction_invalidDateFormat_throwsBadRequestException() {
        TransactionRequest request = new TransactionRequest();
        request.setDate("invalid-date");

        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(request));
    }

    @Test
    void createTransaction_futureDate_throwsBadRequestException() {
        when(userService.getCurrentUser()).thenReturn(user);
        
        TransactionRequest request = new TransactionRequest();
        request.setDate(LocalDate.now().plusDays(1).toString());

        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(request));
    }

    @Test
    void getTransactions_allFilters() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(categoryService.requireCategoryByName("Salary")).thenReturn(category);

        TransactionEntity tx = new TransactionEntity();
        tx.setId(10L);
        tx.setAmount(BigDecimal.valueOf(1000));
        tx.setDate(LocalDate.of(2024, 1, 1));
        tx.setCategory(category);
        tx.setUser(user);

        when(transactionRepository.findByUserAndCategoryAndDateBetweenOrderByDateDesc(
                eq(user), eq(category), eq(LocalDate.of(2024, 1, 1)), eq(LocalDate.of(2024, 1, 31))
        )).thenReturn(Collections.singletonList(tx));

        List<TransactionResponse> result = transactionService.getTransactions("2024-01-01", "2024-01-31", "Salary");

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
    }

    @Test
    void getTransactions_onlyDates() {
        when(userService.getCurrentUser()).thenReturn(user);

        TransactionEntity tx = new TransactionEntity();
        tx.setId(10L);
        tx.setAmount(BigDecimal.valueOf(1000));
        tx.setDate(LocalDate.of(2024, 1, 1));
        tx.setCategory(category);
        tx.setUser(user);

        when(transactionRepository.findByUserAndDateBetweenOrderByDateDesc(
                eq(user), eq(LocalDate.of(2024, 1, 1)), eq(LocalDate.of(2024, 1, 31))
        )).thenReturn(Collections.singletonList(tx));

        List<TransactionResponse> result = transactionService.getTransactions("2024-01-01", "2024-01-31", null);

        assertEquals(1, result.size());
    }

    @Test
    void getTransactions_onlyCategory() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(categoryService.requireCategoryByName("Salary")).thenReturn(category);

        TransactionEntity tx = new TransactionEntity();
        tx.setId(10L);
        tx.setAmount(BigDecimal.valueOf(1000));
        tx.setDate(LocalDate.of(2024, 1, 1));
        tx.setCategory(category);
        tx.setUser(user);

        when(transactionRepository.findByUserAndCategoryOrderByDateDesc(
                eq(user), eq(category)
        )).thenReturn(Collections.singletonList(tx));

        List<TransactionResponse> result = transactionService.getTransactions(null, null, "Salary");

        assertEquals(1, result.size());
    }

    @Test
    void getTransactions_noFilters() {
        when(userService.getCurrentUser()).thenReturn(user);

        TransactionEntity tx = new TransactionEntity();
        tx.setId(10L);
        tx.setAmount(BigDecimal.valueOf(1000));
        tx.setDate(LocalDate.of(2024, 1, 1));
        tx.setCategory(category);
        tx.setUser(user);

        when(transactionRepository.findByUserOrderByDateDesc(eq(user))).thenReturn(Collections.singletonList(tx));

        List<TransactionResponse> result = transactionService.getTransactions(null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void updateTransaction_success() {
        when(userService.getCurrentUser()).thenReturn(user);
        
        TransactionEntity existingTx = new TransactionEntity();
        existingTx.setId(10L);
        existingTx.setAmount(BigDecimal.valueOf(1000));
        existingTx.setDate(LocalDate.of(2024, 1, 1));
        existingTx.setCategory(category);
        existingTx.setUser(user);

        when(transactionRepository.findById(10L)).thenReturn(Optional.of(existingTx));
        
        CategoryEntity newCategory = new CategoryEntity();
        newCategory.setName("Food");
        newCategory.setType(CategoryType.EXPENSE);
        when(categoryService.requireCategoryByName("Food")).thenReturn(newCategory);

        TransactionUpdateRequest request = new TransactionUpdateRequest();
        request.setAmount(BigDecimal.valueOf(500));
        request.setDescription("New Description");
        request.setCategory("Food");

        TransactionResponse result = transactionService.updateTransaction(10L, request);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(500), existingTx.getAmount());
        assertEquals("New Description", existingTx.getDescription());
        assertEquals(newCategory, existingTx.getCategory());
        verify(transactionRepository).save(existingTx);
    }

    @Test
    void updateTransaction_notFound_throwsNotFoundException() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(transactionRepository.findById(10L)).thenReturn(Optional.empty());

        TransactionUpdateRequest request = new TransactionUpdateRequest();

        assertThrows(NotFoundException.class, () -> transactionService.updateTransaction(10L, request));
    }

    @Test
    void updateTransaction_differentUser_throwsNotFoundException() {
        when(userService.getCurrentUser()).thenReturn(user);

        UserEntity otherUser = new UserEntity();
        otherUser.setId(2L);

        TransactionEntity existingTx = new TransactionEntity();
        existingTx.setId(10L);
        existingTx.setUser(otherUser);

        when(transactionRepository.findById(10L)).thenReturn(Optional.of(existingTx));

        TransactionUpdateRequest request = new TransactionUpdateRequest();

        assertThrows(NotFoundException.class, () -> transactionService.updateTransaction(10L, request));
    }

    @Test
    void updateTransaction_negativeAmount_throwsBadRequestException() {
        when(userService.getCurrentUser()).thenReturn(user);
        
        TransactionEntity existingTx = new TransactionEntity();
        existingTx.setId(10L);
        existingTx.setAmount(BigDecimal.valueOf(1000));
        existingTx.setUser(user);

        when(transactionRepository.findById(10L)).thenReturn(Optional.of(existingTx));

        TransactionUpdateRequest request = new TransactionUpdateRequest();
        request.setAmount(BigDecimal.valueOf(-50));

        assertThrows(BadRequestException.class, () -> transactionService.updateTransaction(10L, request));
    }

    @Test
    void deleteTransaction_success() {
        when(userService.getCurrentUser()).thenReturn(user);
        
        TransactionEntity existingTx = new TransactionEntity();
        existingTx.setId(10L);
        existingTx.setUser(user);

        when(transactionRepository.findById(10L)).thenReturn(Optional.of(existingTx));

        transactionService.deleteTransaction(10L);

        verify(transactionRepository).delete(existingTx);
    }

    @Test
    void deleteTransaction_notFound_throwsNotFoundException() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(transactionRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> transactionService.deleteTransaction(10L));
    }
}
