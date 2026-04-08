package com.example.vaxnetbackend.immunization;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/immunization")
@RequiredArgsConstructor
public class ImmunizationController {

    private final ImmunizationService immunizationService;

    @GetMapping("/schedule/{birthCertificateNumber}")
    public ScheduleResponse getSchedule(@PathVariable("birthCertificateNumber") String birthCertificateNumber) {
        return immunizationService.getScheduleForChild(birthCertificateNumber);
    }

    @GetMapping("/parent/schedules/{email}")
    public List<ScheduleResponse> getParentSchedules(@PathVariable("email") String email) {
        return immunizationService.getSchedulesForParent(email);
    }

    @PostMapping("/administer/{birthCertificateNumber}/{vaccineKey}")
    public ScheduleResponse administer(
            @PathVariable("birthCertificateNumber") String birthCertificateNumber,
            @PathVariable("vaccineKey") String vaccineKey,
            @RequestBody AdministerRequest request) {
        return immunizationService.administerVaccine(birthCertificateNumber, vaccineKey, request.getBatchNumber());
    }

    @Data
    public static class AdministerRequest {
        private String batchNumber;
    }
}
