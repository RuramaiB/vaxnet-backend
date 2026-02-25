package com.example.vaxnetbackend.facility;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FacilityResponse {
    private Facility facility;
    private String msg;
}
