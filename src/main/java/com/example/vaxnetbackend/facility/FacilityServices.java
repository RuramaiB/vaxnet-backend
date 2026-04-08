package com.example.vaxnetbackend.facility;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacilityServices {
    private final FacilityRepository facilityRepository;

    public List<Facility> getAllFacilities() {
        return facilityRepository.findAll();
    }

    public ResponseEntity<FacilityResponse> addNewFacility(FacilityRequest facilityRequest) {
        Facility facility = new Facility();
        facility.setFacilityName(facilityRequest.getFacilityName());
        facility.setFacilityAddress(facilityRequest.getFacilityAddress());
        facility.setFacilityPhone(facilityRequest.getFacilityPhone());
        facility.setFacilityDistrict(facilityRequest.getFacilityDistrict());
        facility.setFacilityType(facilityRequest.getFacilityType());
        facility.setStatus(facilityRequest.getStatus());
        facility.setFacilityCoordinates(facilityRequest.getFacilityCoordinates());

        Facility savedFacility = facilityRepository.save(facility);

        FacilityResponse facilityResponse = FacilityResponse.builder()
                .facility(savedFacility)
                .msg("New facility added successfully")
                .build();

        return ResponseEntity.ok(facilityResponse);
    }

    public ResponseEntity<FacilityResponse> editFacility(FacilityRequest facilityRequest, String facilityID) {
        Facility facility = facilityRepository.findById(facilityID)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found"));

        facility.setFacilityName(facilityRequest.getFacilityName());
        facility.setFacilityAddress(facilityRequest.getFacilityAddress());
        facility.setFacilityPhone(facilityRequest.getFacilityPhone());
        facility.setFacilityDistrict(facilityRequest.getFacilityDistrict());
        facility.setFacilityType(facilityRequest.getFacilityType());
        facility.setStatus(facilityRequest.getStatus());
        facility.setFacilityCoordinates(facilityRequest.getFacilityCoordinates());

        Facility updatedFacility = facilityRepository.save(facility);

        FacilityResponse facilityResponse = FacilityResponse.builder()
                .facility(updatedFacility)
                .msg("Facility updated successfully")
                .build();

        return ResponseEntity.ok(facilityResponse);
    }

    public List<Facility> getFacilitiesByDistrict(String district) {
        return facilityRepository.findByFacilityDistrictContainingIgnoreCase(district);
    }
}
