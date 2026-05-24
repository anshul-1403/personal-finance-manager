package com.example.finance.service;

import com.example.finance.entity.CategoryEntity;
import com.example.finance.entity.CategoryType;
import com.example.finance.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DefaultCategoryInitializerTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private DefaultCategoryInitializer defaultCategoryInitializer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void initializeDefaults_savesWhenNotExist() {
        when(categoryRepository.findByNameAndTypeAndUserIsNull(anyString(), any(CategoryType.class)))
                .thenReturn(Optional.empty());

        defaultCategoryInitializer.initializeDefaults();

        verify(categoryRepository, times(7)).save(any(CategoryEntity.class));
    }

    @Test
    void initializeDefaults_doesNotSaveWhenExist() {
        when(categoryRepository.findByNameAndTypeAndUserIsNull(anyString(), any(CategoryType.class)))
                .thenReturn(Optional.of(new CategoryEntity()));

        defaultCategoryInitializer.initializeDefaults();

        verify(categoryRepository, never()).save(any(CategoryEntity.class));
    }
}
