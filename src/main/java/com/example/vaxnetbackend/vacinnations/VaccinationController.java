package com.example.vaxnetbackend.vacinnations;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vaccinations")
public class VaccinationController {
    private final VaccinationService vaccinationService;

    @GetMapping("/get-all-vaccination-records")
    public List<Vaccinations> getAllVaccinationRecords() {
        return vaccinationService.getVaccinations();
    }

    @PostMapping("/add-new-vaccination-record")
    public ResponseEntity<VaccinationResponse> addNewVaccinationRecord(@RequestBody VaccinationRequest vaccinationRequest) {
        return vaccinationService.addNewVaccinationRecord(vaccinationRequest);
    }
}
