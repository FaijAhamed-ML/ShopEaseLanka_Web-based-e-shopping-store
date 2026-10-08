package com.shopease.reviewmanagement.service;

import com.shopease.usermanagement.entity.Customer;
import com.shopease.usermanagement.repository.CustomerRepository;
import com.shopease.productmanagement.entity.Product;
import com.shopease.productmanagement.repository.ProductRepository;
import com.shopease.reviewmanagement.dto.ReviewDtos.*;
import com.shopease.reviewmanagement.entity.MessageType;
import com.shopease.reviewmanagement.entity.Review;
import com.shopease.reviewmanagement.entity.ReviewStatus;
import com.shopease.reviewmanagement.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;

    public ReviewService(ReviewRepository reviewRepository,
                         ProductRepository productRepository,
                         CustomerRepository customerRepository,
                         NotificationService notificationService) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.notificationService = notificationService;
    }

    public boolean isVerifiedBuyer(Integer customerId, Integer productId) {
        if (customerId == null || productId == null) return false;
        return reviewRepository.isVerifiedBuyer(customerId, productId);
    }

    public Optional<Review> getCustomerReviewForProduct(Integer productId, Integer customerId) {
        if (productId == null || customerId == null) return Optional.empty();
        return reviewRepository.findByProduct_ProductIdAndCustomer_CustomerId(productId, customerId);
    }

    @Transactional
    public Review submitReview(SubmitReviewRequest request) {
        // Business Rule: Verified Buyer Rule
        // Customers can only review a product if they purchased it AND the order status is DELIVERED
        boolean verified = isVerifiedBuyer(request.getCustomerId(), request.getProductId());
        if (!verified) {
            throw new IllegalStateException("Only verified buyers who have received delivery of this product can submit a review.");
        }

        // Check if customer already has a review for this product; if so, update it
        Optional<Review> existing = reviewRepository.findByProduct_ProductIdAndCustomer_CustomerId(
                request.getProductId(), request.getCustomerId());
        if (existing.isPresent()) {
            Review r = existing.get();
            r.setRating(request.getRating());
            r.setComment(request.getComment());
            r.setImageUrl(request.getImageUrl());
            r.setStatus(ReviewStatus.PENDING);
            r.setCreatedAt(LocalDateTime.now());
            return reviewRepository.save(r);
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + request.getProductId()));
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found: " + request.getCustomerId()));

        Review review = new Review(
                product,
                customer,
                request.getRating(),
                request.getComment(),
                request.getImageUrl()
        );
        // All submitted reviews default to PENDING
        review.setStatus(ReviewStatus.PENDING);
        return reviewRepository.save(review);
    }

    @Transactional
    public Review updateReview(Integer reviewId, UpdateReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found: " + reviewId));

        if (!review.getCustomer().getCustomerId().equals(request.getCustomerId())) {
            throw new IllegalArgumentException("You are not authorized to edit this review. You can only edit your own reviews.");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setImageUrl(request.getImageUrl());
        // Resets to PENDING so edits undergo moderation
        review.setStatus(ReviewStatus.PENDING);
        review.setCreatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(Integer reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new RuntimeException("Review not found: " + reviewId);
        }
        reviewRepository.deleteById(reviewId);
    }

    public ProductReviewSummary getProductReviews(Integer productId) {
        Double avgRating = reviewRepository.calculateAverageRating(productId);
        Long count = reviewRepository.countApprovedReviews(productId);
        List<Review> approvedReviews = reviewRepository.findByProduct_ProductIdAndStatusOrderByCreatedAtDesc(productId, ReviewStatus.APPROVED);

        double roundedAvg = avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0;
        int totalCount = count != null ? count.intValue() : 0;

        return new ProductReviewSummary(roundedAvg, totalCount, approvedReviews);
    }

    public List<Review> getPendingReviews() {
        return reviewRepository.findByStatusOrderByCreatedAtDesc(ReviewStatus.PENDING);
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Review moderateReview(Integer reviewId, ReviewStatus status) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found: " + reviewId));
        review.setStatus(status);
        review = reviewRepository.save(review);

        // Automated notification to the customer
        try {
            String productName = (review.getProduct() != null) ? review.getProduct().getName() : "your product";
            String msg;
            if (status == ReviewStatus.APPROVED) {
                msg = "Your review for '" + productName + "' has been approved and published!";
            } else if (status == ReviewStatus.REJECTED) {
                msg = "Your review for '" + productName + "' was not approved according to moderation guidelines.";
            } else {
                msg = "Your review for '" + productName + "' is currently in moderation status: " + status;
            }
            notificationService.sendNotification(review.getCustomer().getCustomerId(), MessageType.ORDER, msg);
        } catch (Exception ex) {
            System.err.println("Notice: Could not dispatch review notification: " + ex.getMessage());
        }

        return review;
    }
}
