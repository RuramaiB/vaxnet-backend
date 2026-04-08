package com.example.vaxnetbackend.immunization;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/immunization")
@RequiredArgsConstructor
public class ImmunizationRecordController {

    private final ImmunizationScheduleService immunizationScheduleService;

    /**
     * Get the full immunization schedule for a specific child.
     * Statuses are recomputed dynamically based on today's date and the child's
     * DOB.
     */
    @GetMapping("/schedule/{birthCertificateNumber}")
    public ResponseEntity<ImmunizationRecord> getSchedule(
            @PathVariable String birthCertificateNumber) {
        return ResponseEntity.ok(
                immunizationScheduleService.getScheduleWithStatus(birthCertificateNumber));
    }

    /**
     * Mark a specific vaccine dose as administered for a child.
     * Body: { "batchNumber": "BATCH001" }
     */
    @PostMapping("/administer/{birthCertificateNumber}/{vaccineKey}")
    public ResponseEntity<ImmunizationRecord> administerVaccine(
            @PathVariable String birthCertificateNumber,
            @PathVariable String vaccineKey,
            @RequestBody(required = false) Map<String, String> body) {
        String batchNumber = (body != null) ? body.getOrDefault("batchNumber", "N/A") : "N/A";
        String administeredDate = (body != null) ? body.get("administeredDate") : null;
        return ResponseEntity.ok(
                immunizationScheduleService.administerVaccine(birthCertificateNumber, vaccineKey, batchNumber,
                        administeredDate));
    }

    /**
     * Get all children who have at least one overdue vaccine dose.
     * Useful for admin alerts and CHW follow-up.
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<ImmunizationRecord>> getOverdue() {
        return ResponseEntity.ok(immunizationScheduleService.getAllWithOverdue());
    }

    /**
     * Get all children who have at least one dose due within the next 2 weeks.
     * Useful for scheduling reminders.
     */
    @GetMapping("/due-soon")
    public ResponseEntity<List<ImmunizationRecord>> getDueSoon() {
        return ResponseEntity.ok(immunizationScheduleService.getAllDueSoon());
    }
}
