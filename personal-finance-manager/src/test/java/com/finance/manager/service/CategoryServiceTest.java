package com.finance.manager.service;

import com.finance.manager.dto.CategoryRequest;
import com.finance.manager.dto.CategoryResponse;
import com.finance.manager.entity.Category;
import com.finance.manager.entity.TransactionType;
import com.finance.manager.entity.User;
import com.finance.manager.exception.ConflictException;
import com.finance.manager.exception.ForbiddenException;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.repository.CategoryRepository;
import com.finance.manager.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("test@test.com", "password", "Test User", "123456");
        user.setId(1L);
    }

    @Test
    void testGetAllCategories() {
        Category cat1 = new Category("Food", TransactionType.EXPENSE, false, null);
        Category cat2 = new Category("Salary", TransactionType.INCOME, false, null);
        when(categoryRepository.findByUserIdOrIsCustomFalse(user.getId())).thenReturn(List.of(cat1, cat2));

        List<CategoryResponse> categories = categoryService.getAllCategories(user);
        
        assertEquals(2, categories.size());
        assertEquals("Food", categories.get(0).getName());
    }

    @Test
    void testCreateCustomCategory_Success() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Bonus");
        request.setType(TransactionType.INCOME);

        when(categoryRepository.existsByNameAndUserId("Bonus", 1L)).thenReturn(false);
        when(categoryRepository.findByNameAndIsCustomFalse("Bonus")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category c = invocation.getArgument(0);
            c.setId(10L);
            return c;
        });

        CategoryResponse response = categoryService.createCustomCategory(request, user);

        assertNotNull(response);
        assertEquals("Bonus", response.getName());
        assertTrue(response.getIsCustom());
    }

    @Test
    void testCreateCustomCategory_Conflict() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Food");
        request.setType(TransactionType.EXPENSE);

        when(categoryRepository.existsByNameAndUserId("Food", 1L)).thenReturn(false);
        
        Category defaultFood = new Category("Food", TransactionType.EXPENSE, false, null);
        when(categoryRepository.findByNameAndIsCustomFalse("Food")).thenReturn(Optional.of(defaultFood));

        assertThrows(ConflictException.class, () -> categoryService.createCustomCategory(request, user));
    }

    @Test
    void testDeleteCustomCategory_Success() {
        Category customCat = new Category("Bonus", TransactionType.INCOME, true, user);
        customCat.setId(5L);

        when(categoryRepository.findByNameAndUserId("Bonus", 1L)).thenReturn(Optional.of(customCat));
        when(transactionRepository.existsByCategoryId(5L)).thenReturn(false);

        categoryService.deleteCustomCategory("Bonus", user);

        verify(categoryRepository, times(1)).delete(customCat);
    }

    @Test
    void testDeleteCustomCategory_ForbiddenDefault() {
        when(categoryRepository.findByNameAndUserId("Food", 1L)).thenReturn(Optional.empty());
        
        Category defaultFood = new Category("Food", TransactionType.EXPENSE, false, null);
        when(categoryRepository.findByNameAndIsCustomFalse("Food")).thenReturn(Optional.of(defaultFood));

        assertThrows(ForbiddenException.class, () -> categoryService.deleteCustomCategory("Food", user));
    }
}
