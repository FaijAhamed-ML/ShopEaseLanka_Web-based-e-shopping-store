package com.shopease.productmanagement.service;

import com.shopease.inventorymanagement.entity.Inventory;
import com.shopease.inventorymanagement.repository.InventoryRepository;
import com.shopease.ordermanagement.entity.OrderItem;
import com.shopease.ordermanagement.repository.OrderItemRepository;
import com.shopease.productmanagement.dto.ProductDtos.*;
import com.shopease.productmanagement.entity.Category;
import com.shopease.productmanagement.entity.Product;
import com.shopease.productmanagement.entity.ProductImage;
import com.shopease.productmanagement.repository.CategoryRepository;
import com.shopease.productmanagement.repository.ProductRepository;
import com.shopease.reviewmanagement.entity.Review;
import com.shopease.reviewmanagement.entity.ReviewStatus;
import com.shopease.reviewmanagement.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CatalogService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;

    public CatalogService(CategoryRepository categoryRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            ReviewRepository reviewRepository,
            OrderItemRepository orderItemRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.reviewRepository = reviewRepository;
        this.orderItemRepository = orderItemRepository;
    }

    // Category methods
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> getRootCategories() {
        return categoryRepository.findByParentCategoryIsNull();
    }

    public Category getCategoryById(Integer categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));
    }

    @Transactional
    public Category createCategory(CategoryRequestDto dto) {
        Category parent = null;
        if (dto.getParentCategoryId() != null) {
            parent = getCategoryById(dto.getParentCategoryId());
        }
        Category category = new Category(dto.getName(), parent);
        return categoryRepository.save(category);
    }

    // Product methods
    public List<Product> searchProducts(String keyword, Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        return searchProducts(keyword, categoryId, minPrice, maxPrice, null);
    }

    public List<Product> searchProducts(String keyword, Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice,
            Boolean inStock) {
        List<Product> products = productRepository.searchProducts(keyword, categoryId, minPrice, maxPrice);
        for (Product p : products) {
            enrichProduct(p);
        }

        if (inStock != null) {
            if (inStock) {
                return products.stream()
                        .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() > 0)
                        .toList();
            } else {
                return products.stream()
                        .filter(p -> p.getStockQuantity() == null || p.getStockQuantity() <= 0)
                        .toList();
            }
        }
        return products;
    }

    public Product getProductById(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        enrichProduct(product);
        return product;
    }

    public void enrichProduct(Product product) {
        if (product == null)
            return;

        // 1. Ensure gallery images collection is initialized
        if (product.getGalleryImages() == null) {
            product.setGalleryImages(new ArrayList<>());
        }

        // 2. Live warehouse stock quantity
        inventoryRepository.findByProduct_ProductId(product.getProductId()).ifPresentOrElse(
                inv -> product.setStockQuantity(inv.getStockQuantity()),
                () -> product.setStockQuantity(0));

        // 3. Approved customer reviews rating and count
        List<Review> approvedReviews = reviewRepository.findByProduct_ProductIdAndStatusOrderByCreatedAtDesc(
                product.getProductId(), ReviewStatus.APPROVED);
        product.setReviewCount(approvedReviews.size());
        if (approvedReviews.isEmpty()) {
            product.setAverageRating(5.0);
        } else {
            double avg = approvedReviews.stream().mapToInt(Review::getRating).average().orElse(5.0);
            product.setAverageRating(Math.round(avg * 10.0) / 10.0);
        }
    }

    @Transactional
    public Product createProduct(ProductRequestDto dto) {
        Category category = getCategoryById(dto.getCategoryId());
        Product product = new Product(
                category,
                dto.getName(),
                dto.getDescription(),
                dto.getPrice(),
                dto.getDiscountPercentage(),
                dto.getMainImageUrl());

        if (dto.getAdditionalImageUrls() != null) {
            for (String imgUrl : dto.getAdditionalImageUrls()) {
                if (imgUrl != null && !imgUrl.isBlank()) {
                    product.getGalleryImages().add(new ProductImage(product, imgUrl.trim()));
                }
            }
        }
        product = productRepository.save(product);

        // Initialize warehouse inventory record
        int initStock = (dto.getInitialStock() != null && dto.getInitialStock() >= 0) ? dto.getInitialStock() : 20;
        Inventory inventory = new Inventory(product, initStock, 5);
        inventoryRepository.save(inventory);

        enrichProduct(product);
        return product;
    }

    @Transactional
    public Product updateProduct(Integer productId, ProductRequestDto dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        if (dto.getCategoryId() != null) {
            product.setCategory(getCategoryById(dto.getCategoryId()));
        }
        if (dto.getName() != null && !dto.getName().isBlank())
            product.setName(dto.getName());
        if (dto.getDescription() != null)
            product.setDescription(dto.getDescription());
        if (dto.getPrice() != null)
            product.setPrice(dto.getPrice());
        if (dto.getDiscountPercentage() != null)
            product.setDiscountPercentage(dto.getDiscountPercentage());
        if (dto.getMainImageUrl() != null && !dto.getMainImageUrl().isBlank())
            product.setMainImageUrl(dto.getMainImageUrl());

        // Update secondary gallery images if provided
        if (dto.getAdditionalImageUrls() != null) {
            product.getGalleryImages().clear();
            for (String imgUrl : dto.getAdditionalImageUrls()) {
                if (imgUrl != null && !imgUrl.isBlank()) {
                    product.getGalleryImages().add(new ProductImage(product, imgUrl.trim()));
                }
            }
        }

        product = productRepository.save(product);
        enrichProduct(product);
        return product;
    }

    @Transactional
    public void deleteProduct(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        // Check if referenced in historical customer orders
        List<OrderItem> orderItems = orderItemRepository.findByProduct_ProductId(productId);
        if (!orderItems.isEmpty()) {
            throw new IllegalStateException("Cannot delete product '" + product.getName() + "' (#" + productId +
                    ") because it is referenced in " + orderItems.size() + " historical customer order(s). " +
                    "Consider updating its status or adjusting stock to 0 instead.");
        }

        // Clean up associated inventory if present
        inventoryRepository.findByProduct_ProductId(productId).ifPresent(inventoryRepository::delete);

        // Delete product (cascades to product_images and reviews)
        productRepository.delete(product);
    }
}
