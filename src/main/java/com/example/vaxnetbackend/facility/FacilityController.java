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
        return  facilityServices.addNewFacility(facilityRequest);
    }

    @PutMapping("/edit-facility/{facilityID}")
    public ResponseEntity<FacilityResponse> editFacility(@RequestBody FacilityRequest facilityRequest, @PathVariable("facilityID") String facilityID) {
        return facilityServices.editFacility(facilityRequest, facilityID);
    }
}
