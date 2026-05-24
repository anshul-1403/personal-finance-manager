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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CategoryService categoryService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new UserEntity();
        user.setId(1L);
        user.setUsername("test@example.com");
    }

    @Test
    void getAllCategories_success() {
        when(userService.getCurrentUser()).thenReturn(user);
        
        CategoryEntity defaultCat = new CategoryEntity();
        defaultCat.setName("Salary");
        defaultCat.setType(CategoryType.INCOME);
        defaultCat.setCustom(false);

        CategoryEntity customCat = new CategoryEntity();
        customCat.setName("CustomFood");
        customCat.setType(CategoryType.EXPENSE);
        customCat.setCustom(true);
        customCat.setUser(user);

        when(categoryRepository.findAll()).thenReturn(Collections.singletonList(defaultCat));
        when(categoryRepository.findByUser(user)).thenReturn(Collections.singletonList(customCat));

        List<CategoryDto> result = categoryService.getAllCategories();

        assertEquals(2, result.size());
        assertEquals("Salary", result.get(0).getName());
        assertFalse(result.get(0).isCustom());
        assertEquals("CustomFood", result.get(1).getName());
        assertTrue(result.get(1).isCustom());
    }

    @Test
    void createCategory_success() {
        when(userService.getCurrentUser()).thenReturn(user);
        CategoryDto request = new CategoryDto("CustomRent", CategoryType.EXPENSE, true);

        when(categoryRepository.existsByNameAndUser("CustomRent", user)).thenReturn(false);
        when(categoryRepository.findByNameAndTypeAndUserIsNull("CustomRent", CategoryType.EXPENSE)).thenReturn(Optional.empty());

        CategoryDto result = categoryService.createCategory(request);

        assertNotNull(result);
        assertEquals("CustomRent", result.getName());
        assertEquals(CategoryType.EXPENSE, result.getType());
        assertTrue(result.isCustom());
        verify(categoryRepository).save(any(CategoryEntity.class));
    }

    @Test
    void createCategory_missingType_throwsBadRequestException() {
        CategoryDto request = new CategoryDto("CustomRent", null, true);

        assertThrows(BadRequestException.class, () -> categoryService.createCategory(request));
    }

    @Test
    void createCategory_alreadyExists_throwsConflictException() {
        when(userService.getCurrentUser()).thenReturn(user);
        CategoryDto request = new CategoryDto("CustomRent", CategoryType.EXPENSE, true);

        when(categoryRepository.existsByNameAndUser("CustomRent", user)).thenReturn(true);

        assertThrows(ConflictException.class, () -> categoryService.createCategory(request));
    }

    @Test
    void createCategory_overrideBuiltIn_throwsConflictException() {
        when(userService.getCurrentUser()).thenReturn(user);
        CategoryDto request = new CategoryDto("Salary", CategoryType.INCOME, true);

        when(categoryRepository.existsByNameAndUser("Salary", user)).thenReturn(false);
        
        CategoryEntity defaultCat = new CategoryEntity();
        defaultCat.setName("Salary");
        defaultCat.setType(CategoryType.INCOME);
        
        when(categoryRepository.findByNameAndTypeAndUserIsNull("Salary", CategoryType.INCOME)).thenReturn(Optional.of(defaultCat));

        assertThrows(ConflictException.class, () -> categoryService.createCategory(request));
    }

    @Test
    void deleteCategory_success() {
        when(userService.getCurrentUser()).thenReturn(user);
        
        CategoryEntity customCat = new CategoryEntity();
        customCat.setName("CustomRent");
        customCat.setCustom(true);
        customCat.setUser(user);

        when(categoryRepository.findByNameAndUser("CustomRent", user)).thenReturn(Optional.of(customCat));
        when(transactionRepository.existsByCategory(customCat)).thenReturn(false);

        categoryService.deleteCategory("CustomRent");

        verify(categoryRepository).delete(customCat);
    }

    @Test
    void deleteCategory_notFound_throwsNotFoundException() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(categoryRepository.findByNameAndUser("CustomRent", user)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.deleteCategory("CustomRent"));
    }

    @Test
    void deleteCategory_defaultCategory_throwsBadRequestException() {
        when(userService.getCurrentUser()).thenReturn(user);
        
        CategoryEntity defaultCat = new CategoryEntity();
        defaultCat.setName("Salary");
        defaultCat.setCustom(false);

        when(categoryRepository.findByNameAndUser("Salary", user)).thenReturn(Optional.of(defaultCat));

        assertThrows(BadRequestException.class, () -> categoryService.deleteCategory("Salary"));
    }

    @Test
    void deleteCategory_referencedByTransactions_throwsConflictException() {
        when(userService.getCurrentUser()).thenReturn(user);
        
        CategoryEntity customCat = new CategoryEntity();
        customCat.setName("CustomRent");
        customCat.setCustom(true);
        customCat.setUser(user);

        when(categoryRepository.findByNameAndUser("CustomRent", user)).thenReturn(Optional.of(customCat));
        when(transactionRepository.existsByCategory(customCat)).thenReturn(true);

        assertThrows(ConflictException.class, () -> categoryService.deleteCategory("CustomRent"));
    }

    @Test
    void requireCategoryByName_custom_success() {
        when(userService.getCurrentUser()).thenReturn(user);
        
        CategoryEntity customCat = new CategoryEntity();
        customCat.setName("CustomRent");
        customCat.setUser(user);

        when(categoryRepository.findByNameAndUser("CustomRent", user)).thenReturn(Optional.of(customCat));

        CategoryEntity result = categoryService.requireCategoryByName("CustomRent");

        assertEquals(customCat, result);
    }

    @Test
    void requireCategoryByName_default_success() {
        when(userService.getCurrentUser()).thenReturn(user);
        
        CategoryEntity defaultCat = new CategoryEntity();
        defaultCat.setName("Salary");

        when(categoryRepository.findByNameAndUser("Salary", user)).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUserIsNull("Salary")).thenReturn(Optional.of(defaultCat));

        CategoryEntity result = categoryService.requireCategoryByName("Salary");

        assertEquals(defaultCat, result);
    }

    @Test
    void requireCategoryByName_notFound_throwsNotFoundException() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(categoryRepository.findByNameAndUser("Salary", user)).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUserIsNull("Salary")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.requireCategoryByName("Salary"));
    }

    @Test
    void getCategoryById_success() {
        CategoryEntity defaultCat = new CategoryEntity();
        defaultCat.setId(5L);
        defaultCat.setName("Salary");

        when(categoryRepository.findById(5L)).thenReturn(Optional.of(defaultCat));

        CategoryEntity result = categoryService.getCategoryById(5L);

        assertEquals(defaultCat, result);
    }

    @Test
    void getCategoryById_notFound_throwsNotFoundException() {
        when(categoryRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.getCategoryById(5L));
    }
}
