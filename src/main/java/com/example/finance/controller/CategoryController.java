package com.example.finance.controller;

import com.example.finance.dto.ApiResponse;
import com.example.finance.dto.CategoryDto;
import com.example.finance.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<Map<String, List<CategoryDto>>> getAllCategories() {
        List<CategoryDto> categories = categoryService.getAllCategories();
        Map<String, List<CategoryDto>> payload = new HashMap<>();
        payload.put("categories", categories);
        return ResponseEntity.ok(payload);
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto request) {
        CategoryDto category = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable("name") String name) {
        categoryService.deleteCategory(name);
        return ResponseEntity.ok(new ApiResponse("Category deleted successfully"));
    }
}
