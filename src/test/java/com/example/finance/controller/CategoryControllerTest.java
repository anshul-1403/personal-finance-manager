package com.example.finance.controller;

import com.example.finance.dto.CategoryDto;
import com.example.finance.entity.CategoryType;
import com.example.finance.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void getAllCategories_success() throws Exception {
        CategoryDto dto = new CategoryDto("Salary", CategoryType.INCOME, false);
        when(categoryService.getAllCategories()).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories[0].name").value("Salary"));
    }

    @Test
    @WithMockUser
    void createCategory_success() throws Exception {
        CategoryDto request = new CategoryDto("Food", CategoryType.EXPENSE, true);
        when(categoryService.createCategory(any(CategoryDto.class))).thenReturn(request);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Food"));
    }

    @Test
    @WithMockUser
    void deleteCategory_success() throws Exception {
        doNothing().when(categoryService).deleteCategory("Food");

        mockMvc.perform(delete("/api/categories/Food"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));
    }
}
