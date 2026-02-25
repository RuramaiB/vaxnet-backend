package com.example.vaxnetbackend.vacinnations;

import com.example.vaxnetbackend.children.Child;
import com.example.vaxnetbackend.vaccines.Vaccine;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDate;

@Data
@ToString
public class VaccinationRequest {
    private String narration;
    private String vaccineID;
    private String birthCertificateNumber;
    private LocalDate vaccinationDate;
    private String facilityID;
    private String status;
}
