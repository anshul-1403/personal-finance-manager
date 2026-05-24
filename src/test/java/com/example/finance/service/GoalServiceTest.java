package com.example.finance.service;

import com.example.finance.dto.GoalRequest;
import com.example.finance.dto.GoalResponse;
import com.example.finance.dto.GoalUpdateRequest;
import com.example.finance.entity.CategoryEntity;
import com.example.finance.entity.CategoryType;
import com.example.finance.entity.GoalEntity;
import com.example.finance.entity.TransactionEntity;
import com.example.finance.entity.UserEntity;
import com.example.finance.exception.BadRequestException;
import com.example.finance.exception.NotFoundException;
import com.example.finance.repository.GoalRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private GoalService goalService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new UserEntity();
        user.setId(1L);
        user.setUsername("test@example.com");
    }

    @Test
    void createGoal_success() {
        when(userService.getCurrentUser()).thenReturn(user);

        GoalRequest request = new GoalRequest();
        request.setGoalName("Emergency Fund");
        request.setTargetAmount(BigDecimal.valueOf(10000));
        request.setTargetDate(LocalDate.now().plusMonths(6).toString());
        request.setStartDate(LocalDate.now().toString());

        GoalEntity savedGoal = new GoalEntity();
        savedGoal.setId(20L);
        savedGoal.setGoalName("Emergency Fund");
        savedGoal.setTargetAmount(BigDecimal.valueOf(10000));
        savedGoal.setTargetDate(LocalDate.now().plusMonths(6));
        savedGoal.setStartDate(LocalDate.now());
        savedGoal.setUser(user);

        when(goalRepository.save(any(GoalEntity.class))).thenReturn(savedGoal);
        when(transactionRepository.findByUserAndDateGreaterThanEqual(eq(user), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        GoalResponse result = goalService.createGoal(request);

        assertNotNull(result);
        assertEquals(20L, result.getId());
        assertEquals("Emergency Fund", result.getGoalName());
        assertEquals(BigDecimal.valueOf(10000), result.getTargetAmount());
        assertEquals(BigDecimal.ZERO, result.getCurrentProgress());
        assertEquals(0.0, result.getProgressPercentage());
        assertEquals(BigDecimal.valueOf(10000), result.getRemainingAmount());
        
        verify(goalRepository).save(any(GoalEntity.class));
    }

    @Test
    void createGoal_targetDateInPast_throwsBadRequestException() {
        when(userService.getCurrentUser()).thenReturn(user);

        GoalRequest request = new GoalRequest();
        request.setGoalName("Emergency Fund");
        request.setTargetAmount(BigDecimal.valueOf(10000));
        request.setTargetDate(LocalDate.now().minusDays(1).toString());

        assertThrows(BadRequestException.class, () -> goalService.createGoal(request));
    }

    @Test
    void createGoal_startDateAfterTargetDate_throwsBadRequestException() {
        when(userService.getCurrentUser()).thenReturn(user);

        GoalRequest request = new GoalRequest();
        request.setGoalName("Emergency Fund");
        request.setTargetAmount(BigDecimal.valueOf(10000));
        request.setTargetDate(LocalDate.now().plusDays(2).toString());
        request.setStartDate(LocalDate.now().plusDays(5).toString());

        assertThrows(BadRequestException.class, () -> goalService.createGoal(request));
    }

    @Test
    void createGoal_autofillStartDate() {
        when(userService.getCurrentUser()).thenReturn(user);

        GoalRequest request = new GoalRequest();
        request.setGoalName("Emergency Fund");
        request.setTargetAmount(BigDecimal.valueOf(10000));
        request.setTargetDate(LocalDate.now().plusMonths(6).toString());
        request.setStartDate(null); // null should trigger autofill with today's date

        GoalEntity savedGoal = new GoalEntity();
        savedGoal.setId(20L);
        savedGoal.setGoalName("Emergency Fund");
        savedGoal.setTargetAmount(BigDecimal.valueOf(10000));
        savedGoal.setTargetDate(LocalDate.now().plusMonths(6));
        savedGoal.setStartDate(LocalDate.now());
        savedGoal.setUser(user);

        when(goalRepository.save(any(GoalEntity.class))).thenReturn(savedGoal);
        when(transactionRepository.findByUserAndDateGreaterThanEqual(eq(user), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        GoalResponse result = goalService.createGoal(request);

        assertNotNull(result);
        assertEquals(LocalDate.now().toString(), result.getStartDate());
    }

    @Test
    void getGoals_success() {
        when(userService.getCurrentUser()).thenReturn(user);

        GoalEntity goal = new GoalEntity();
        goal.setId(20L);
        goal.setGoalName("Emergency Fund");
        goal.setTargetAmount(BigDecimal.valueOf(10000));
        goal.setStartDate(LocalDate.now());
        goal.setTargetDate(LocalDate.now().plusMonths(6));
        goal.setUser(user);

        when(goalRepository.findByUser(user)).thenReturn(Collections.singletonList(goal));
        when(transactionRepository.findByUserAndDateGreaterThanEqual(eq(user), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        List<GoalResponse> result = goalService.getGoals();

        assertEquals(1, result.size());
        assertEquals("Emergency Fund", result.get(0).getGoalName());
    }

    @Test
    void getGoal_success() {
        when(userService.getCurrentUser()).thenReturn(user);

        GoalEntity goal = new GoalEntity();
        goal.setId(20L);
        goal.setGoalName("Emergency Fund");
        goal.setTargetAmount(BigDecimal.valueOf(10000));
        goal.setStartDate(LocalDate.now());
        goal.setTargetDate(LocalDate.now().plusMonths(6));
        goal.setUser(user);

        when(goalRepository.findByIdAndUser(20L, user)).thenReturn(Optional.of(goal));
        when(transactionRepository.findByUserAndDateGreaterThanEqual(eq(user), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        GoalResponse result = goalService.getGoal(20L);

        assertNotNull(result);
        assertEquals("Emergency Fund", result.getGoalName());
    }

    @Test
    void getGoal_notFound_throwsNotFoundException() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(goalRepository.findByIdAndUser(20L, user)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> goalService.getGoal(20L));
    }

    @Test
    void updateGoal_success() {
        when(userService.getCurrentUser()).thenReturn(user);

        GoalEntity existingGoal = new GoalEntity();
        existingGoal.setId(20L);
        existingGoal.setGoalName("Emergency Fund");
        existingGoal.setTargetAmount(BigDecimal.valueOf(10000));
        existingGoal.setStartDate(LocalDate.now());
        existingGoal.setTargetDate(LocalDate.now().plusMonths(6));
        existingGoal.setUser(user);

        when(goalRepository.findByIdAndUser(20L, user)).thenReturn(Optional.of(existingGoal));
        when(transactionRepository.findByUserAndDateGreaterThanEqual(eq(user), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        GoalUpdateRequest request = new GoalUpdateRequest();
        request.setGoalName("New Name");
        request.setTargetAmount(BigDecimal.valueOf(15000));
        request.setTargetDate(LocalDate.now().plusMonths(12).toString());

        GoalResponse result = goalService.updateGoal(20L, request);

        assertNotNull(result);
        assertEquals("New Name", existingGoal.getGoalName());
        assertEquals(BigDecimal.valueOf(15000), existingGoal.getTargetAmount());
        assertEquals(LocalDate.now().plusMonths(12), existingGoal.getTargetDate());
    }

    @Test
    void updateGoal_negativeTargetAmount_throwsBadRequestException() {
        when(userService.getCurrentUser()).thenReturn(user);

        GoalEntity existingGoal = new GoalEntity();
        existingGoal.setId(20L);
        existingGoal.setTargetAmount(BigDecimal.valueOf(10000));
        existingGoal.setUser(user);

        when(goalRepository.findByIdAndUser(20L, user)).thenReturn(Optional.of(existingGoal));

        GoalUpdateRequest request = new GoalUpdateRequest();
        request.setTargetAmount(BigDecimal.valueOf(-1));

        assertThrows(BadRequestException.class, () -> goalService.updateGoal(20L, request));
    }

    @Test
    void updateGoal_pastTargetDate_throwsBadRequestException() {
        when(userService.getCurrentUser()).thenReturn(user);

        GoalEntity existingGoal = new GoalEntity();
        existingGoal.setId(20L);
        existingGoal.setTargetAmount(BigDecimal.valueOf(10000));
        existingGoal.setUser(user);

        when(goalRepository.findByIdAndUser(20L, user)).thenReturn(Optional.of(existingGoal));

        GoalUpdateRequest request = new GoalUpdateRequest();
        request.setTargetDate(LocalDate.now().minusDays(1).toString());

        assertThrows(BadRequestException.class, () -> goalService.updateGoal(20L, request));
    }

    @Test
    void deleteGoal_success() {
        when(userService.getCurrentUser()).thenReturn(user);

        GoalEntity existingGoal = new GoalEntity();
        existingGoal.setId(20L);
        existingGoal.setUser(user);

        when(goalRepository.findByIdAndUser(20L, user)).thenReturn(Optional.of(existingGoal));

        goalService.deleteGoal(20L);

        verify(goalRepository).delete(existingGoal);
    }

    @Test
    void progressCalculation_withTransactions() {
        when(userService.getCurrentUser()).thenReturn(user);

        GoalEntity goal = new GoalEntity();
        goal.setId(20L);
        goal.setGoalName("Emergency Fund");
        goal.setTargetAmount(BigDecimal.valueOf(1000));
        goal.setStartDate(LocalDate.of(2024, 1, 1));
        goal.setTargetDate(LocalDate.of(2024, 6, 1));
        goal.setUser(user);

        when(goalRepository.findByIdAndUser(20L, user)).thenReturn(Optional.of(goal));

        CategoryEntity incomeCat = new CategoryEntity();
        incomeCat.setType(CategoryType.INCOME);
        incomeCat.setName("Salary");

        CategoryEntity expenseCat = new CategoryEntity();
        expenseCat.setType(CategoryType.EXPENSE);
        expenseCat.setName("Food");

        TransactionEntity tx1 = new TransactionEntity();
        tx1.setAmount(BigDecimal.valueOf(1500));
        tx1.setCategory(incomeCat);

        TransactionEntity tx2 = new TransactionEntity();
        tx2.setAmount(BigDecimal.valueOf(600));
        tx2.setCategory(expenseCat);

        when(transactionRepository.findByUserAndDateGreaterThanEqual(user, LocalDate.of(2024, 1, 1)))
                .thenReturn(Arrays.asList(tx1, tx2));

        GoalResponse result = goalService.getGoal(20L);

        // net savings = 1500 - 600 = 900
        assertEquals(BigDecimal.valueOf(900), result.getCurrentProgress());
        assertEquals(90.0, result.getProgressPercentage());
        assertEquals(BigDecimal.valueOf(100), result.getRemainingAmount());
    }
}
