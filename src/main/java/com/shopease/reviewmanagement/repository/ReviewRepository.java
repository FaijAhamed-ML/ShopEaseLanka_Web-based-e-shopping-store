package com.shopease.reviewmanagement.repository;

import com.shopease.reviewmanagement.entity.Review;
import com.shopease.reviewmanagement.entity.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByProduct_ProductIdAndStatusOrderByCreatedAtDesc(Integer productId, ReviewStatus status);

    List<Review> findByStatusOrderByCreatedAtDesc(ReviewStatus status);

    List<Review> findAllByOrderByCreatedAtDesc();

    Optional<Review> findByProduct_ProductIdAndCustomer_CustomerId(Integer productId, Integer customerId);

    List<Review> findByCustomer_CustomerIdOrderByCreatedAtDesc(Integer customerId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.productId = :productId AND r.status = 'APPROVED'")
    Double calculateAverageRating(@Param("productId") Integer productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.productId = :productId AND r.status = 'APPROVED'")
    Long countApprovedReviews(@Param("productId") Integer productId);

    // Verified Buyer Check: Customer must have an order with status 'DELIVERED' containing the product
    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE o.customer.customerId = :customerId " +
           "AND oi.product.productId = :productId " +
           "AND o.orderStatus = 'DELIVERED'")
    boolean isVerifiedBuyer(@Param("customerId") Integer customerId, @Param("productId") Integer productId);
}
