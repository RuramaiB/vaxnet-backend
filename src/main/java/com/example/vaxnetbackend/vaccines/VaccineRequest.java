package com.example.vaxnetbackend.vaccines;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Data
@ToString
public class VaccineRequest {
    private String vaccineName;
    private String manufacturer;
    private String countryOfOrigin;
    private String requiredDoses;
    private String storageTemperature;
    private String ageGroup;
    private List<String> distributionCenters;
    private LocalDate startDate;
    private  LocalDate endDate;
}
