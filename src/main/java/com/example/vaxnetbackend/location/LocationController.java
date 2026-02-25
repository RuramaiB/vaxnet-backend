package com.example.vaxnetbackend.location;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;

    @GetMapping("/provinces")
    public List<Province> getAllProvinces() {
        return provinceRepository.findAll();
    }

    @GetMapping("/districts")
    public List<District> getDistricts(@RequestParam(required = false) String provinceId) {
        if (provinceId != null) {
            return districtRepository.findByProvinceId(provinceId);
        }
        return districtRepository.findAll();
    }
}
