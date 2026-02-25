package com.example.vaxnetbackend.vaccines;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vaccines")
public class VaccineController {
    private final VaccineServices vaccineServices;

    @GetMapping("/get-all-vaccines")
    public List<Vaccine> getAllVaccines() {
        return vaccineServices.getAllVaccines();
    }

    @PostMapping("/add-new-vaccine")
    public ResponseEntity<VaccineResponse> addNewVaccine(@RequestBody VaccineRequest vaccineRequest) {
        return vaccineServices.addNewVaccine(vaccineRequest);
    }
}
