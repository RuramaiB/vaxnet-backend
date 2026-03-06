package com.example.vaxnetbackend.insights;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightController {

    private final InsightEngine insightEngine;

    /** Global overview — national-level aggregate stats */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview() {
        return ResponseEntity.ok(insightEngine.getOverview());
    }

    /** Coverage heatmap data grouped by district */
    @GetMapping("/by-district")
    public ResponseEntity<List<Map<String, Object>>> getByDistrict() {
        return ResponseEntity.ok(insightEngine.getByDistrict());
    }

    /** Per-vaccine uptake coverage rates across all children */
    @GetMapping("/vaccine-coverage")
    public ResponseEntity<List<Map<String, Object>>> getVaccineCoverage() {
        return ResponseEntity.ok(insightEngine.getVaccineCoverage());
    }

    /** Month-by-month doses administered trend */
    @GetMapping("/monthly-trends")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyTrends() {
        return ResponseEntity.ok(insightEngine.getMonthlyTrends());
    }

    /** Insight for a specific child by birth certificate number */
    @GetMapping("/by-child/{birthCertificateNumber}")
    public ResponseEntity<Map<String, Object>> getChildInsight(@PathVariable String birthCertificateNumber) {
        return ResponseEntity.ok(insightEngine.getChildInsight(birthCertificateNumber));
    }

    /** Insight for a specific parent by email */
    @GetMapping("/by-parent/{email}")
    public ResponseEntity<Map<String, Object>> getParentInsight(@PathVariable String email) {
        return ResponseEntity.ok(insightEngine.getParentInsight(email));
    }

    /** AI-style narrative insight paragraph (full national report) */
    @GetMapping("/narrative")
    public ResponseEntity<Map<String, Object>> getNarrative() {
        String narrative = insightEngine.generateNarrative();
        return ResponseEntity.ok(Map.of(
                "narrative", narrative,
                "generatedAt", java.time.LocalDate.now().toString()));
    }
}
