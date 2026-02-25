package com.example.vaxnetbackend.facility;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
@TypeAlias("Facility")
public class Facility  {

    @Id
    private String facilityID;
    private String facilityName;
    private String facilityAddress;
    private String facilityPhone;
    private String facilityDistrict;
    private String facilityType;
    private String status;
    private String facilityCoordinates;
}
