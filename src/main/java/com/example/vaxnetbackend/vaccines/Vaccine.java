package com.example.vaxnetbackend.vaccines;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
@TypeAlias("Vaccine")
public class Vaccine {
    @Id
    private String vaccineID;
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
