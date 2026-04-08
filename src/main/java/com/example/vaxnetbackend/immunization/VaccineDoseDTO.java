package com.example.vaxnetbackend.immunization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VaccineDoseDTO {
    private String vaccineKey;
    private String vaccineName;
    private int scheduledAgeWeeks;
    private String route;
    private String site;
    private String dosage;
    private LocalDate scheduledDate;
    private LocalDate administeredDate;
    private String batchNumber;
    private String status; // ADMINISTERED, OVERDUE, DUE, DUE_SOON, NOT_YET_DUE
}
