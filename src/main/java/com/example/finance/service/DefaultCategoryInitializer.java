package com.example.finance.service;

import com.example.finance.entity.CategoryEntity;
import com.example.finance.entity.CategoryType;
import com.example.finance.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultCategoryInitializer {

    private final CategoryRepository categoryRepository;

    public DefaultCategoryInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @PostConstruct
    public void initializeDefaults() {
        addDefault("Salary", CategoryType.INCOME);
        addDefault("Food", CategoryType.EXPENSE);
        addDefault("Rent", CategoryType.EXPENSE);
        addDefault("Transportation", CategoryType.EXPENSE);
        addDefault("Entertainment", CategoryType.EXPENSE);
        addDefault("Healthcare", CategoryType.EXPENSE);
        addDefault("Utilities", CategoryType.EXPENSE);
    }

    private void addDefault(String name, CategoryType type) {
        boolean exists = categoryRepository.findByNameAndTypeAndUserIsNull(name, type).isPresent();
        if (!exists) {
            CategoryEntity category = new CategoryEntity();
            category.setName(name);
            category.setType(type);
            category.setCustom(false);
            category.setUser(null);
            categoryRepository.save(category);
        }
    }
}
