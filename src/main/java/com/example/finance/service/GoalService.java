package com.example.finance.service;

import com.example.finance.dto.GoalRequest;
import com.example.finance.dto.GoalResponse;
import com.example.finance.dto.GoalUpdateRequest;
import com.example.finance.entity.GoalEntity;
import com.example.finance.entity.TransactionEntity;
import com.example.finance.entity.UserEntity;
import com.example.finance.exception.BadRequestException;
import com.example.finance.exception.NotFoundException;
import com.example.finance.repository.GoalRepository;
import com.example.finance.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public GoalService(GoalRepository goalRepository,
                       TransactionRepository transactionRepository,
                       UserService userService) {
        this.goalRepository = goalRepository;
        this.transactionRepository = transactionRepository;
        this.userService = userService;
    }

    public GoalResponse createGoal(GoalRequest request) {
        UserEntity currentUser = userService.getCurrentUser();
        LocalDate targetDate = parseDate(request.getTargetDate());
        LocalDate startDate = request.getStartDate() != null && !request.getStartDate().isBlank()
                ? parseDate(request.getStartDate())
                : LocalDate.now();
        if (!targetDate.isAfter(LocalDate.now())) {
            throw new BadRequestException("Target date must be a future date");
        }
        if (startDate.isAfter(targetDate)) {
            throw new BadRequestException("Start date cannot be after target date");
        }
        GoalEntity goal = new GoalEntity();
        goal.setGoalName(request.getGoalName());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(targetDate);
        goal.setStartDate(startDate);
        goal.setUser(currentUser);
        goal = goalRepository.save(goal);
        return buildResponse(goal);
    }

    public List<GoalResponse> getGoals() {
        UserEntity currentUser = userService.getCurrentUser();
        return goalRepository.findByUser(currentUser).stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    public GoalResponse getGoal(Long id) {
        GoalEntity goal = findByIdAndCurrentUser(id);
        return buildResponse(goal);
    }

    public GoalResponse updateGoal(Long id, GoalUpdateRequest request) {
        GoalEntity goal = findByIdAndCurrentUser(id);
        if (request.getTargetAmount() != null) {
            if (request.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Target amount must be positive");
            }
            goal.setTargetAmount(request.getTargetAmount());
        }
        if (request.getTargetDate() != null && !request.getTargetDate().isBlank()) {
            LocalDate targetDate = parseDate(request.getTargetDate());
            if (!targetDate.isAfter(LocalDate.now())) {
                throw new BadRequestException("Target date must be a future date");
            }
            goal.setTargetDate(targetDate);
        }
        if (request.getGoalName() != null && !request.getGoalName().isBlank()) {
            goal.setGoalName(request.getGoalName());
        }
        goalRepository.save(goal);
        return buildResponse(goal);
    }

    public void deleteGoal(Long id) {
        GoalEntity goal = findByIdAndCurrentUser(id);
        goalRepository.delete(goal);
    }

    private GoalEntity findByIdAndCurrentUser(Long id) {
        UserEntity currentUser = userService.getCurrentUser();
        return goalRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new NotFoundException("Goal not found"));
    }

    private GoalResponse buildResponse(GoalEntity goal) {
        BigDecimal currentProgress = calculateProgress(goal);
        BigDecimal remaining = goal.getTargetAmount().subtract(currentProgress).max(BigDecimal.ZERO);
        double percentage;
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) == 0) {
            percentage = 0.0;
        } else {
            percentage = currentProgress.multiply(BigDecimal.valueOf(100))
                    .divide(goal.getTargetAmount(), 2, RoundingMode.HALF_UP)
                    .doubleValue();
        }
        return new GoalResponse(
                goal.getId(),
                goal.getGoalName(),
                goal.getTargetAmount(),
                goal.getTargetDate().toString(),
                goal.getStartDate().toString(),
                currentProgress,
                percentage,
                remaining
        );
    }

    private BigDecimal calculateProgress(GoalEntity goal) {
        List<TransactionEntity> transactions = transactionRepository.findByUserAndDateGreaterThanEqual(goal.getUser(), goal.getStartDate());
        BigDecimal income = transactions.stream()
                .filter(tx -> tx.getCategory().getType().name().equals("INCOME"))
                .map(TransactionEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expense = transactions.stream()
                .filter(tx -> tx.getCategory().getType().name().equals("EXPENSE"))
                .map(TransactionEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return income.subtract(expense).max(BigDecimal.ZERO);
    }

    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (Exception ex) {
            throw new BadRequestException("Invalid date format: " + date);
        }
    }
}
