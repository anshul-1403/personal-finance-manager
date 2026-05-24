package com.finance.manager.service;

import com.finance.manager.dto.CategoryRequest;
import com.finance.manager.dto.CategoryResponse;
import com.finance.manager.entity.Category;
import com.finance.manager.entity.TransactionType;
import com.finance.manager.entity.User;
import com.finance.manager.exception.ConflictException;
import com.finance.manager.exception.ForbiddenException;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.exception.BadRequestException;
import com.finance.manager.repository.CategoryRepository;
import com.finance.manager.repository.TransactionRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public CategoryService(CategoryRepository categoryRepository, TransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    @PostConstruct
    public void seedDefaultCategories() {
        seedCategoryIfNotFound("Salary", TransactionType.INCOME);
        seedCategoryIfNotFound("Food", TransactionType.EXPENSE);
        seedCategoryIfNotFound("Rent", TransactionType.EXPENSE);
        seedCategoryIfNotFound("Transportation", TransactionType.EXPENSE);
        seedCategoryIfNotFound("Entertainment", TransactionType.EXPENSE);
        seedCategoryIfNotFound("Healthcare", TransactionType.EXPENSE);
        seedCategoryIfNotFound("Utilities", TransactionType.EXPENSE);
    }

    private void seedCategoryIfNotFound(String name, TransactionType type) {
        if (categoryRepository.findByNameAndIsCustomFalse(name).isEmpty()) {
            Category category = new Category(name, type, false, null);
            categoryRepository.save(category);
        }
    }

    public List<CategoryResponse> getAllCategories(User user) {
        List<Category> categories = categoryRepository.findByUserIdOrIsCustomFalse(user.getId());
        return categories.stream()
                .map(c -> new CategoryResponse(c.getName(), c.getType(), c.isCustom()))
                .collect(Collectors.toList());
    }

    public CategoryResponse createCustomCategory(CategoryRequest request, User user) {
        if (categoryRepository.existsByNameAndUserId(request.getName(), user.getId())) {
            throw new ConflictException("Custom category with name " + request.getName() + " already exists.");
        }
        if (categoryRepository.findByNameAndIsCustomFalse(request.getName()).isPresent()) {
            throw new ConflictException("Category name conflicts with a default category.");
        }

        Category category = new Category(request.getName(), request.getType(), true, user);
        Category savedCategory = categoryRepository.save(category);
        
        return new CategoryResponse(savedCategory.getName(), savedCategory.getType(), savedCategory.isCustom());
    }

    public void deleteCustomCategory(String name, User user) {
        Optional<Category> optionalCategory = categoryRepository.findByNameAndUserId(name, user.getId());
        if (optionalCategory.isEmpty()) {
            Optional<Category> defaultCategory = categoryRepository.findByNameAndIsCustomFalse(name);
            if (defaultCategory.isPresent()) {
                throw new ForbiddenException("Cannot delete a default category.");
            }
            throw new ResourceNotFoundException("Category not found.");
        }

        Category category = optionalCategory.get();
        
        // Check if category is used by transactions
        if (transactionRepository.existsByCategoryId(category.getId())) {
            throw new BadRequestException("Cannot delete category as it is referenced by existing transactions.");
        }
        
        categoryRepository.delete(category);
    }

    public Category findValidCategoryForUser(String categoryName, User user) {
        Optional<Category> customCategory = categoryRepository.findByNameAndUserId(categoryName, user.getId());
        if (customCategory.isPresent()) {
            return customCategory.get();
        }
        return categoryRepository.findByNameAndIsCustomFalse(categoryName)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryName));
    }
}
