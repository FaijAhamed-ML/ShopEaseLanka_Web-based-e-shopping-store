package com.shopease.reviewmanagement.dto;

import com.shopease.reviewmanagement.entity.Review;
import com.shopease.reviewmanagement.entity.ReviewStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ReviewDtos {

    public static class SubmitReviewRequest {
        @NotNull(message = "Product ID is required")
        private Integer productId;

        @NotNull(message = "Customer ID is required")
        private Integer customerId;

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating cannot exceed 5")
        private Integer rating;

        private String comment;
        private String imageUrl;

        public SubmitReviewRequest() {}

        public Integer getProductId() { return productId; }
        public void setProductId(Integer productId) { this.productId = productId; }

        public Integer getCustomerId() { return customerId; }
        public void setCustomerId(Integer customerId) { this.customerId = customerId; }

        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }

        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }

    public static class ReviewModerationRequest {
        @NotNull(message = "Moderation status is required")
        private ReviewStatus status;

        public ReviewModerationRequest() {}

        public ReviewStatus getStatus() { return status; }
        public void setStatus(ReviewStatus status) { this.status = status; }
    }

    public static class ProductReviewSummary {
        private Double averageRating;
        private Integer totalReviews;
        private List<Review> reviews;

        public ProductReviewSummary() {}

        public ProductReviewSummary(Double averageRating, Integer totalReviews, List<Review> reviews) {
            this.averageRating = averageRating;
            this.totalReviews = totalReviews;
            this.reviews = reviews;
        }

        public Double getAverageRating() { return averageRating; }
        public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

        public Integer getTotalReviews() { return totalReviews; }
        public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }

        public List<Review> getReviews() { return reviews; }
        public void setReviews(List<Review> reviews) { this.reviews = reviews; }
    }

    public static class UpdateReviewRequest {
        @NotNull(message = "Customer ID is required")
        private Integer customerId;

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating cannot exceed 5")
        private Integer rating;

        private String comment;
        private String imageUrl;

        public UpdateReviewRequest() {}

        public UpdateReviewRequest(Integer customerId, Integer rating, String comment, String imageUrl) {
            this.customerId = customerId;
            this.rating = rating;
            this.comment = comment;
            this.imageUrl = imageUrl;
        }

        public Integer getCustomerId() { return customerId; }
        public void setCustomerId(Integer customerId) { this.customerId = customerId; }

        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }

        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }
}
