package com.example.vaxnetbackend.vacinnations;

import com.example.vaxnetbackend.children.ChildRepository;
import com.example.vaxnetbackend.facility.FacilityRepository;
import com.example.vaxnetbackend.vaccines.Vaccine;
import com.example.vaxnetbackend.vaccines.VaccineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class VaccinationService {
    private final VaccinationsRepository vaccinationsRepository;
    private final ChildRepository childRepository;
    private final VaccineRepository vaccineRepository;
    private final FacilityRepository facilityRepository;

    public List<Vaccinations> getVaccinations() {
        return vaccinationsRepository.findAll();
    }

    public ResponseEntity<VaccinationResponse> addNewVaccinationRecord(VaccinationRequest vaccinationRequest) {
        Vaccinations vaccination = new Vaccinations();
        Vaccine vaccine = vaccineRepository.findById(vaccinationRequest.getVaccineID())
                .orElseThrow(() -> new IllegalStateException("Vaccine with ID " + vaccinationRequest.getVaccineID() + " does not exist"));
        vaccination.setVaccine(vaccine);
        vaccination.setNarration(vaccinationRequest.getNarration());
        vaccination.setVaccinationDate(vaccinationRequest.getVaccinationDate());
        vaccination.setStatus(vaccinationRequest.getStatus());
        childRepository.findById(vaccinationRequest.getBirthCertificateNumber())
                .ifPresent(vaccination::setChild);
        facilityRepository.findById(vaccinationRequest.getFacilityID())
                        .ifPresent(vaccination::setFacility);
        vaccinationsRepository.save(vaccination);
        return ResponseEntity.ok(VaccinationResponse
                .builder()
                        .vaccination(vaccination)
                        .msg("Vaccination record added successfully")
                .build());
    }
}
