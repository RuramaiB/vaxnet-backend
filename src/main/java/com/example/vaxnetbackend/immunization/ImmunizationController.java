package com.example.vaxnetbackend.immunization;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/immunization")
@RequiredArgsConstructor
public class ImmunizationController {

    private final ImmunizationScheduleService immunizationService;

    @GetMapping("/schedule/{birthCertificateNumber}")
    public ResponseEntity<ImmunizationRecord> getSchedule(@PathVariable("birthCertificateNumber") String birthCertificateNumber) {
        return ResponseEntity.ok(immunizationService.getScheduleWithStatus(birthCertificateNumber));
    }

    @GetMapping("/bootstrap")
    public ResponseEntity<String> bootstrap() {
        immunizationService.generateMissingSchedules();
        return ResponseEntity.ok("Immunization schedules and appointments bootstrapped for all children.");
    }

    @GetMapping("/bootstrap/status")
    public ResponseEntity<String> getBootstrapStatus() {
        return ResponseEntity.ok(immunizationService.getBootstrapStats());
    }

    @GetMapping("/parent/schedules/{email}")
    public ResponseEntity<List<ImmunizationRecord>> getParentSchedules(@PathVariable("email") String email) {
        return ResponseEntity.ok(immunizationService.getSchedulesForParent(email));
    }

    @PostMapping("/administer/{birthCertificateNumber}/{vaccineKey}")
    public ResponseEntity<ImmunizationRecord> administer(
            @PathVariable("birthCertificateNumber") String birthCertificateNumber,
            @PathVariable("vaccineKey") String vaccineKey,
            @RequestBody AdministerRequest request) {
        return ResponseEntity.ok(immunizationService.administerVaccine(
                birthCertificateNumber, 
                vaccineKey, 
                request.getBatchNumber() != null ? request.getBatchNumber() : "N/A", 
                request.getAdministeredDate()
        ));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<ImmunizationRecord>> getOverdue() {
        return ResponseEntity.ok(immunizationService.getAllWithOverdue());
    }

    @GetMapping("/due-soon")
    public ResponseEntity<List<ImmunizationRecord>> getDueSoon() {
        return ResponseEntity.ok(immunizationService.getAllDueSoon());
    }

    @Data
    public static class AdministerRequest {
        private String batchNumber;
        private String administeredDate;
    }
}
