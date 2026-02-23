package com.example.marketplace.repository;

import com.example.marketplace.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByUserIdAndItemId(Long userId, Long itemId);
    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);
    boolean existsByUserIdAndItemId(Long userId, Long itemId);
    void deleteByUserIdAndItemId(Long userId, Long itemId);
    void deleteByItemId(Long itemId);
}

