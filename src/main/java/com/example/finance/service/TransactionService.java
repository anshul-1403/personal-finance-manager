package com.example.finance.service;

import com.example.finance.dto.TransactionRequest;
import com.example.finance.dto.TransactionResponse;
import com.example.finance.dto.TransactionUpdateRequest;
import com.example.finance.entity.CategoryEntity;
import com.example.finance.entity.TransactionEntity;
import com.example.finance.entity.UserEntity;
import com.example.finance.exception.BadRequestException;
import com.example.finance.exception.NotFoundException;
import com.example.finance.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final CategoryService categoryService;

    public TransactionService(TransactionRepository transactionRepository,
                              UserService userService,
                              CategoryService categoryService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    public TransactionResponse createTransaction(TransactionRequest request) {
        UserEntity currentUser = userService.getCurrentUser();
        LocalDate transactionDate = parseDate(request.getDate());
        validateDateNotFuture(transactionDate);
        CategoryEntity category = categoryService.requireCategoryByName(request.getCategory());
        TransactionEntity transaction = new TransactionEntity();
        transaction.setAmount(request.getAmount());
        transaction.setDate(transactionDate);
        transaction.setCategory(category);
        transaction.setDescription(request.getDescription());
        transaction.setUser(currentUser);
        transaction = transactionRepository.save(transaction);
        return toResponse(transaction);
    }

    public List<TransactionResponse> getTransactions(String startDate, String endDate, String categoryName) {
        UserEntity currentUser = userService.getCurrentUser();
        LocalDate start = startDate != null ? parseDate(startDate) : null;
        LocalDate end = endDate != null ? parseDate(endDate) : null;
        List<TransactionEntity> results;
        if (start != null && end != null && categoryName != null) {
            CategoryEntity category = categoryService.requireCategoryByName(categoryName);
            results = transactionRepository.findByUserAndCategoryAndDateBetweenOrderByDateDesc(currentUser, category, start, end);
        } else if (start != null && end != null) {
            results = transactionRepository.findByUserAndDateBetweenOrderByDateDesc(currentUser, start, end);
        } else if (categoryName != null) {
            CategoryEntity category = categoryService.requireCategoryByName(categoryName);
            results = transactionRepository.findByUserAndCategoryOrderByDateDesc(currentUser, category);
        } else {
            results = transactionRepository.findByUserOrderByDateDesc(currentUser);
        }
        return results.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public TransactionResponse updateTransaction(Long id, TransactionUpdateRequest request) {
        UserEntity currentUser = userService.getCurrentUser();
        TransactionEntity transaction = transactionRepository.findById(id)
                .filter(tx -> tx.getUser().getId().equals(currentUser.getId()))
                .orElseThrow(() -> new NotFoundException("Transaction not found"));
        // Date field is intentionally ignored during update - date cannot be changed
        if (request.getAmount() != null) {
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Amount must be a positive value");
            }
            transaction.setAmount(request.getAmount());
        }
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            CategoryEntity category = categoryService.requireCategoryByName(request.getCategory());
            transaction.setCategory(category);
        }
        transactionRepository.save(transaction);
        return toResponse(transaction);
    }

    public void deleteTransaction(Long id) {
        UserEntity currentUser = userService.getCurrentUser();
        TransactionEntity transaction = transactionRepository.findById(id)
                .filter(tx -> tx.getUser().getId().equals(currentUser.getId()))
                .orElseThrow(() -> new NotFoundException("Transaction not found"));
        transactionRepository.delete(transaction);
    }

    public TransactionResponse toResponse(TransactionEntity entity) {
        return new TransactionResponse(
                entity.getId(),
                entity.getAmount(),
                entity.getDate().toString(),
                entity.getCategory().getName(),
                entity.getDescription(),
                entity.getCategory().getType().name()
        );
    }

    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (Exception ex) {
            throw new BadRequestException("Invalid date format: " + date);
        }
    }

    private void validateDateNotFuture(LocalDate date) {
        if (date.isAfter(LocalDate.now())) {
            throw new BadRequestException("Transaction date cannot be in the future");
        }
    }
}
