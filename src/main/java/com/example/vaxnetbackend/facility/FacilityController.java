package com.example.vaxnetbackend.facility;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facilities")
@RequiredArgsConstructor
public class FacilityController {
    private final FacilityServices facilityServices;

    @GetMapping("/get-all-facilities")
    public List<Facility> getAllFacilities() {
        return facilityServices.getAllFacilities();
    }

    @PostMapping("/add-new-facility")
    public ResponseEntity<FacilityResponse> addNewFacility(@RequestBody FacilityRequest facilityRequest) {
        return facilityServices.addNewFacility(facilityRequest);
    }

    @GetMapping("/by-district/{district}")
    public List<Facility> getFacilitiesByDistrict(@PathVariable String district) {
        return facilityServices.getFacilitiesByDistrict(district);
    }

    @PutMapping("/edit-facility/{facilityID}")
    public ResponseEntity<FacilityResponse> editFacility(@RequestBody FacilityRequest facilityRequest,
            @PathVariable String facilityID) {
        return facilityServices.editFacility(facilityRequest, facilityID);
    }
}
