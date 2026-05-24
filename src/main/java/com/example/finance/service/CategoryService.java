package com.example.finance.service;

import com.example.finance.dto.CategoryDto;
import com.example.finance.entity.CategoryEntity;
import com.example.finance.entity.CategoryType;
import com.example.finance.entity.UserEntity;
import com.example.finance.exception.BadRequestException;
import com.example.finance.exception.ConflictException;
import com.example.finance.exception.NotFoundException;
import com.example.finance.repository.CategoryRepository;
import com.example.finance.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public CategoryService(CategoryRepository categoryRepository,
                           TransactionRepository transactionRepository,
                           UserService userService) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.userService = userService;
    }

    public List<CategoryDto> getAllCategories() {
        UserEntity currentUser = userService.getCurrentUser();
        List<CategoryEntity> defaults = categoryRepository.findAll().stream()
                .filter(category -> !category.isCustom())
                .toList();
        List<CategoryEntity> custom = categoryRepository.findByUser(currentUser);
        List<CategoryDto> combined = new ArrayList<>();
        combined.addAll(defaults.stream()
                .map(category -> new CategoryDto(category.getName(), category.getType(), false))
                .toList());
        combined.addAll(custom.stream()
                .map(category -> new CategoryDto(category.getName(), category.getType(), true))
                .toList());
        return combined;
    }

    public CategoryDto createCategory(CategoryDto request) {
        UserEntity currentUser = userService.getCurrentUser();
        if (request.getType() == null) {
            throw new BadRequestException("Category type is required");
        }
        if (categoryRepository.existsByNameAndUser(request.getName(), currentUser)) {
            throw new ConflictException("Custom category name must be unique per user");
        }
        if (categoryRepository.findByNameAndTypeAndUserIsNull(request.getName(), request.getType()).isPresent()) {
            throw new ConflictException("Cannot override built-in category");
        }
        CategoryEntity category = new CategoryEntity();
        category.setName(request.getName());
        category.setType(request.getType());
        category.setCustom(true);
        category.setUser(currentUser);
        categoryRepository.save(category);
        return new CategoryDto(category.getName(), category.getType(), true);
    }

    public void deleteCategory(String name) {
        UserEntity currentUser = userService.getCurrentUser();
        CategoryEntity category = categoryRepository.findByNameAndUser(name, currentUser)
                .orElseThrow(() -> new NotFoundException("Category not found or cannot be deleted"));
        if (!category.isCustom()) {
            throw new BadRequestException("Default categories cannot be deleted");
        }
        if (transactionRepository.existsByCategory(category)) {
            throw new ConflictException("Category currently referenced by transactions cannot be deleted");
        }
        categoryRepository.delete(category);
    }

    public CategoryEntity requireCategoryByName(String name) {
        UserEntity currentUser = userService.getCurrentUser();
        return categoryRepository.findByNameAndUser(name, currentUser)
                .or(() -> categoryRepository.findByNameAndUserIsNull(name))
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }

    public CategoryEntity getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }
}
