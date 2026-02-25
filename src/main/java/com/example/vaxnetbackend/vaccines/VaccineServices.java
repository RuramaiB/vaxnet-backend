package com.example.vaxnetbackend.vaccines;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class VaccineServices {
    private final VaccineRepository vaccineRepository;

    public List<Vaccine> getAllVaccines(){
        return vaccineRepository.findAll();
    }

    public ResponseEntity<VaccineResponse> addNewVaccine(VaccineRequest vaccineRequest){
        Vaccine vaccine = new Vaccine();
        vaccine.setVaccineName(vaccineRequest.getVaccineName());
        vaccine.setManufacturer(vaccineRequest.getManufacturer());
        vaccine.setRequiredDoses(vaccineRequest.getRequiredDoses());
        vaccine.setStorageTemperature(vaccineRequest.getStorageTemperature());
        vaccine.setAgeGroup(vaccineRequest.getAgeGroup());
        vaccine.setCountryOfOrigin(vaccineRequest.getCountryOfOrigin());
        vaccine.setStartDate(vaccineRequest.getStartDate());
        vaccine.setEndDate(vaccineRequest.getEndDate());
        vaccine.setDistributionCenters(vaccineRequest.getDistributionCenters());
        vaccineRepository.save(vaccine);
        VaccineResponse vaccineResponse = VaccineResponse.builder()
                .vaccine(vaccine)
                .msg("New vaccine added successfully")
                .build();
        return ResponseEntity.ok(vaccineResponse);
    }
}
