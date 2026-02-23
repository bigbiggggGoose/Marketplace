package com.example.marketplace.repository;

import com.example.marketplace.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Page<Item> findByStatus(String status, Pageable pageable);
    
    @Query("SELECT i FROM Item i WHERE i.status IN ('ON_SALE', 'OUT_OF_STOCK') " +
           "AND i.reviewStatus = 'APPROVED' " +
           "AND (:keyword IS NULL OR i.title LIKE CONCAT('%', :keyword, '%') OR i.description LIKE CONCAT('%', :keyword, '%')) " +
           "AND (:category IS NULL OR i.category = :category)")
    Page<Item> searchItems(@Param("keyword") String keyword, 
                           @Param("category") String category, 
                           Pageable pageable);
 
    List<Item> findByReviewStatusOrderByCreatedAtDesc(String reviewStatus);

    @Query("SELECT i FROM Item i WHERE (:keyword IS NULL OR i.title LIKE CONCAT('%', :keyword, '%') OR i.description LIKE CONCAT('%', :keyword, '%')) " +
           "AND (:status IS NULL OR i.status = :status) " +
           "AND (:reviewStatus IS NULL OR i.reviewStatus = :reviewStatus)")
    Page<Item> adminSearchItems(@Param("keyword") String keyword,
                                @Param("status") String status,
                                @Param("reviewStatus") String reviewStatus,
                                Pageable pageable);

    List<Item> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
    
    @Query("SELECT i FROM Item i WHERE i.status IN ('ON_SALE', 'OUT_OF_STOCK') " +
           "AND i.reviewStatus = 'APPROVED' ORDER BY i.views DESC")
    List<Item> findTopByViews(Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.status IN ('ON_SALE', 'OUT_OF_STOCK') " +
           "AND i.reviewStatus = 'APPROVED' " +
           "ORDER BY i.createdAt DESC")
    List<Item> findApprovedItemsForRecommendation(Pageable pageable);
}

