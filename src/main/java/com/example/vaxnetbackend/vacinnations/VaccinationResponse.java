package com.example.vaxnetbackend.vacinnations;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VaccinationResponse {
    private Vaccinations vaccination;
    private String msg;
}
