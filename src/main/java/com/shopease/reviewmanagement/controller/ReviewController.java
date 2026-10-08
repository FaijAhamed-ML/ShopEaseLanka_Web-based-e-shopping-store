package com.shopease.reviewmanagement.controller;

import com.shopease.common.ApiResponse;
import com.shopease.reviewmanagement.dto.ReviewDtos.*;
import com.shopease.reviewmanagement.entity.Review;
import com.shopease.reviewmanagement.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<ProductReviewSummary>> getProductReviews(@PathVariable Integer productId) {
        ProductReviewSummary summary = reviewService.getProductReviews(productId);
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @GetMapping("/eligibility")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkEligibility(
            @RequestParam Integer customerId,
            @RequestParam Integer productId) {
        boolean eligible = reviewService.isVerifiedBuyer(customerId, productId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "eligible", eligible,
                "reason", eligible ? "Verified Buyer" : "You can only review products you purchased and received (Delivered status)."
        )));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Review>> submitReview(@Valid @RequestBody SubmitReviewRequest request) {
        try {
            Review review = reviewService.submitReview(request);
            return ResponseEntity.ok(ApiResponse.ok("Review submitted successfully and is pending moderation by Customer Experience.", review));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to submit review: " + e.getMessage()));
        }
    }

    @GetMapping("/moderation/pending")
    public ResponseEntity<ApiResponse<List<Review>>> getPendingReviews() {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getPendingReviews()));
    }

    @GetMapping("/moderation/all")
    public ResponseEntity<ApiResponse<List<Review>>> getAllReviews() {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getAllReviews()));
    }

    @PutMapping("/moderation/{id}")
    public ResponseEntity<ApiResponse<Review>> moderateReview(@PathVariable Integer id, @Valid @RequestBody ReviewModerationRequest request) {
        try {
            Review moderated = reviewService.moderateReview(id, request.getStatus());
            return ResponseEntity.ok(ApiResponse.ok("Review moderation updated to: " + request.getStatus(), moderated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/product/{productId}/my-review")
    public ResponseEntity<ApiResponse<Review>> getMyReview(
            @PathVariable Integer productId,
            @RequestParam Integer customerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                reviewService.getCustomerReviewForProduct(productId, customerId).orElse(null)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Review>> updateReview(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateReviewRequest request) {
        try {
            Review updated = reviewService.updateReview(id, request);
            return ResponseEntity.ok(ApiResponse.ok("Review updated successfully and resubmitted for moderation.", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to update review: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Integer id) {
        try {
            reviewService.deleteReview(id);
            return ResponseEntity.ok(ApiResponse.ok("Review deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete review: " + e.getMessage()));
        }
    }
}
