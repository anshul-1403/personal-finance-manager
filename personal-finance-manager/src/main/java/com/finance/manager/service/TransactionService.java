package com.finance.manager.service;

import com.finance.manager.dto.TransactionRequest;
import com.finance.manager.dto.TransactionResponse;
import com.finance.manager.dto.TransactionUpdateRequest;
import com.finance.manager.entity.Category;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.User;
import com.finance.manager.exception.BadRequestException;
import com.finance.manager.exception.ForbiddenException;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;

    public TransactionService(TransactionRepository transactionRepository, CategoryService categoryService) {
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
    }

    public TransactionResponse createTransaction(TransactionRequest request, User user) {
        if (request.getDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Transaction date cannot be in the future.");
        }

        Category category = categoryService.findValidCategoryForUser(request.getCategory(), user);

        Transaction transaction = new Transaction(
                request.getAmount(),
                request.getDate(),
                category,
                request.getDescription(),
                category.getType(),
                user
        );

        Transaction savedTransaction = transactionRepository.save(transaction);
        return mapToResponse(savedTransaction);
    }

    public List<TransactionResponse> getTransactions(User user, LocalDate startDate, LocalDate endDate, Long categoryId) {
        List<Transaction> transactions = transactionRepository.findByUserIdWithFilters(user.getId(), startDate, endDate, categoryId);
        return transactions.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public TransactionResponse updateTransaction(Long id, TransactionUpdateRequest request, User user) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found."));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Cannot access other user's transaction.");
        }

        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            Category category = categoryService.findValidCategoryForUser(request.getCategory(), user);
            transaction.setCategory(category);
            transaction.setType(category.getType());
        }

        Transaction updatedTransaction = transactionRepository.save(transaction);
        return mapToResponse(updatedTransaction);
    }

    public void deleteTransaction(Long id, User user) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found."));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Cannot access other user's transaction.");
        }

        transactionRepository.delete(transaction);
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getCategory().getName(),
                transaction.getDescription(),
                transaction.getType()
        );
    }
}
