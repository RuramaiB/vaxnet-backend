package com.example.vaxnetbackend.vaccines;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VaccineResponse {
    private Vaccine vaccine;
    private String msg;
}
