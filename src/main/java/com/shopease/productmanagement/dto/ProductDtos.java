package com.shopease.productmanagement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class ProductDtos {

    public static class ProductRequestDto {
        @NotNull(message = "Category ID is required")
        private Integer categoryId;

        @NotBlank(message = "Product name is required")
        private String name;

        private String description;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be positive")
        private BigDecimal price;

        private BigDecimal discountPercentage = BigDecimal.ZERO;

        private String mainImageUrl;

        private List<String> additionalImageUrls;

        private Integer initialStock = 20;

        public ProductRequestDto() {}

        public Integer getCategoryId() { return categoryId; }
        public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public BigDecimal getDiscountPercentage() { return discountPercentage; }
        public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }

        public String getMainImageUrl() { return mainImageUrl; }
        public void setMainImageUrl(String mainImageUrl) { this.mainImageUrl = mainImageUrl; }

        public List<String> getAdditionalImageUrls() { return additionalImageUrls; }
        public void setAdditionalImageUrls(List<String> additionalImageUrls) { this.additionalImageUrls = additionalImageUrls; }

        public Integer getInitialStock() { return initialStock; }
        public void setInitialStock(Integer initialStock) { this.initialStock = initialStock; }
    }

    public static class CategoryRequestDto {
        @NotBlank(message = "Category name is required")
        private String name;
        private Integer parentCategoryId;

        public CategoryRequestDto() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Integer getParentCategoryId() { return parentCategoryId; }
        public void setParentCategoryId(Integer parentCategoryId) { this.parentCategoryId = parentCategoryId; }
    }
}
