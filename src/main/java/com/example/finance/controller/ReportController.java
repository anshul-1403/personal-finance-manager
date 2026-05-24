package com.example.finance.controller;

import com.example.finance.dto.ReportMonthlyResponse;
import com.example.finance.dto.ReportYearlyResponse;
import com.example.finance.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<ReportMonthlyResponse> getMonthlyReport(@PathVariable("year") int year, @PathVariable("month") int month) {
        return ResponseEntity.ok(reportService.getMonthlyReport(year, month));
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<ReportYearlyResponse> getYearlyReport(@PathVariable("year") int year) {
        return ResponseEntity.ok(reportService.getYearlyReport(year));
    }
}
