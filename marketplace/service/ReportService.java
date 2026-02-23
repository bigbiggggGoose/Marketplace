package com.example.marketplace.service;

import com.example.marketplace.dto.ReportRequest;
import com.example.marketplace.dto.PageResult;
import com.example.marketplace.model.Report;
import com.example.marketplace.repository.ReportRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ReportService {
    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public ResponseEntity<?> createReport(Long itemId, Long reporterId, ReportRequest request) {
        Report report = new Report();
        report.setItemId(itemId);
        report.setReporterId(reporterId);
        report.setReason(request.getReason());
        report.setDescription(request.getDesc());
        report.setStatus("PENDING");

        report = reportRepository.save(report);
        return ResponseEntity.ok(Map.of("id", report.getId(), "message", "举报已提交"));
    }

    public ResponseEntity<PageResult<Report>> getReports(String status, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Report> reports;
        if (status == null || "ALL".equalsIgnoreCase(status)) {
            reports = reportRepository.findAll(pageable);
        } else {
            reports = reportRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        }
        PageResult<Report> result = new PageResult<>(
            reports.getContent(),
            reports.getTotalElements(),
            page + 1,
            pageSize
        );
        return ResponseEntity.ok(result);
    }

    public ResponseEntity<?> processReport(Long reportId, String action) {
        Report report = reportRepository.findById(reportId).orElse(null);
        if (report == null) {
            return ResponseEntity.status(404).body(Map.of("error", "举报不存在"));
        }

        if ("approve".equals(action)) {
            report.setStatus("PROCESSED");
        } else if ("ignore".equals(action)) {
            report.setStatus("IGNORED");
        }

        report.setProcessedAt(java.time.LocalDateTime.now());
        reportRepository.save(report);
        return ResponseEntity.ok(Map.of("message", "处理成功"));
    }
}

