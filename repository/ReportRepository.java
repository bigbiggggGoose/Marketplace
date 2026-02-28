package com.example.marketplace.repository;

import com.example.marketplace.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    List<Report> findByItemId(Long itemId);
    void deleteByItemId(Long itemId);
}

