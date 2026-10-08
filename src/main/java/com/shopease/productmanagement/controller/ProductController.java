
package com.shopease.productmanagement.controller;

import com.shopease.common.ApiResponse;
import com.shopease.productmanagement.dto.ProductDtos.ProductRequestDto;
import com.shopease.productmanagement.entity.Product;
import com.shopease.productmanagement.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final CatalogService catalogService;

    public ProductController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock
    ) {
        List<Product> products = catalogService.searchProducts(keyword, categoryId, minPrice, maxPrice, inStock);
        return ResponseEntity.ok(ApiResponse.ok(products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable Integer id) {
        try {
            Product product = catalogService.getProductById(id);
            return ResponseEntity.ok(ApiResponse.ok(product));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Product>> createProduct(@Valid @RequestBody ProductRequestDto dto) {
        try {
            Product product = catalogService.createProduct(dto);
            return ResponseEntity.ok(ApiResponse.ok("Product created successfully", product));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create product: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(@PathVariable Integer id, @RequestBody ProductRequestDto dto) {
        try {
            Product product = catalogService.updateProduct(id, dto);
            return ResponseEntity.ok(ApiResponse.ok("Product updated successfully", product));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update product: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Integer id) {
        try {
            catalogService.deleteProduct(id);
            return ResponseEntity.ok(ApiResponse.ok("Product deleted successfully", null));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete product: " + e.getMessage()));
        }
    }
}
