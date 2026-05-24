package com.finance.manager.controller;

import com.finance.manager.security.CustomUserDetails;
import com.finance.manager.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<?> getMonthlyReport(
            @PathVariable int year,
            @PathVariable int month,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(reportService.getMonthlyReport(userDetails.getUser(), year, month));
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<?> getYearlyReport(
            @PathVariable int year,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(reportService.getYearlyReport(userDetails.getUser(), year));
    }
}
