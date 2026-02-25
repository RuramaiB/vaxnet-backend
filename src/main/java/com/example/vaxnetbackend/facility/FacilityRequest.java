package com.example.vaxnetbackend.facility;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class FacilityRequest {
    private String facilityName;
    private String facilityAddress;
    private String facilityPhone;
    private String facilityDistrict;
    private String facilityType;
    private String status;
    private String facilityCoordinates;

}
