package com.finance.manager.service;

import com.finance.manager.dto.GoalRequest;
import com.finance.manager.dto.GoalResponse;
import com.finance.manager.entity.SavingsGoal;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.TransactionType;
import com.finance.manager.entity.User;
import com.finance.manager.repository.SavingsGoalRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GoalServiceTest {

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private GoalService goalService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("test@test.com", "password", "Test User", "123456");
        user.setId(1L);
    }

    @Test
    void testCreateGoal() {
        GoalRequest request = new GoalRequest();
        request.setGoalName("Vacation");
        request.setTargetAmount(new BigDecimal("5000"));
        request.setStartDate(LocalDate.now());
        request.setTargetDate(LocalDate.now().plusMonths(6));

        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenAnswer(invocation -> {
            SavingsGoal g = invocation.getArgument(0);
            g.setId(10L);
            return g;
        });

        // Mock empty transactions for progress calculation
        when(transactionRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        GoalResponse response = goalService.createGoal(request, user);

        assertNotNull(response);
        assertEquals("Vacation", response.getGoalName());
        assertEquals(new BigDecimal("0.00"), response.getProgressPercentage());
    }

    @Test
    void testGetGoalProgress() {
        SavingsGoal goal = new SavingsGoal("MacBook", new BigDecimal("2000"), LocalDate.now().plusMonths(3), LocalDate.now().minusMonths(1), user);
        goal.setId(1L);

        when(savingsGoalRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(goal));

        Transaction t1 = new Transaction(new BigDecimal("3000"), LocalDate.now(), null, "Salary", TransactionType.INCOME, user);
        Transaction t2 = new Transaction(new BigDecimal("1000"), LocalDate.now(), null, "Rent", TransactionType.EXPENSE, user);
        
        when(transactionRepository.findByUserIdAndDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(t1, t2));

        GoalResponse response = goalService.getGoal(1L, user);

        assertNotNull(response);
        // Income = 3000, Expense = 1000 -> Net = 2000. Target = 2000 -> 100%
        assertEquals(new BigDecimal("2000"), response.getCurrentProgress());
        assertEquals(new BigDecimal("100.00"), response.getProgressPercentage());
    }
}
