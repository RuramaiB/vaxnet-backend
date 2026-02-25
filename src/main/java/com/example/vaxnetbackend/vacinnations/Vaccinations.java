package com.example.vaxnetbackend.vacinnations;

import com.example.vaxnetbackend.children.Child;
import com.example.vaxnetbackend.facility.Facility;
import com.example.vaxnetbackend.vaccines.Vaccine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
@TypeAlias("Vaccinations")
public class Vaccinations {
    @Id
    private String vaccinationID;
    private LocalDate vaccinationDate;
    private String narration;
    @DBRef
    private Vaccine vaccine;
    @DBRef
    private Child child;
    @DBRef
    private Facility facility;

    private String status;
}
