package com.shopease.productmanagement.controller;

import com.shopease.common.ApiResponse;
import com.shopease.productmanagement.dto.ProductDtos.CategoryRequestDto;
import com.shopease.productmanagement.entity.Category;
import com.shopease.productmanagement.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CatalogService catalogService;

    public CategoryController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.getAllCategories()));
    }

    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<Category>>> getCategoryTree() {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.getRootCategories()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> getCategoryById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(catalogService.getCategoryById(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Category>> createCategory(@Valid @RequestBody CategoryRequestDto dto) {
        try {
            Category category = catalogService.createCategory(dto);
            return ResponseEntity.ok(ApiResponse.ok("Category created successfully", category));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
