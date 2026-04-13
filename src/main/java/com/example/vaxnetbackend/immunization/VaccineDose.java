package com.example.vaxnetbackend.immunization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VaccineDose {

    private String vaccineKey; // e.g. "BCG", "OPV1", "DPT1"
    private String vaccineName; // Display name e.g. "BCG"
    private int scheduledAgeWeeks; // Minimum age in weeks when dose is due
    private int maxAgeWeeks; // Maximum age in weeks (0 = no strict max tracked beyond schedule)
    private String route; // e.g. "Intradermal", "Oral", "Intramuscular", "Subcutaneous"
    private String site; // e.g. "Right deltoid muscle"
    private String dosage; // e.g. "0.05 ml"
    private String scheduledDate; // ISO date computed from DOB + scheduledAgeWeeks
    private String administeredDate; // ISO date set when dose is given; null until then
    private String batchNumber; // Recorded when administered
    private VaccineStatus status; // Computed status

    public java.time.LocalDate calculateScheduledDate(java.time.LocalDate dob) {
        return dob.plusWeeks(this.scheduledAgeWeeks);
    }

    public String calculateScheduledDateStr(java.time.LocalDate dob) {
        return calculateScheduledDate(dob).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
