package com.example.vaxnetbackend.children;

import com.example.vaxnetbackend.user.User;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.List;

@Data
@ToString
public class ChildRequest {
    private String birthCertificateNumber;
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String placeOfBirth;
    private String physicalAddress;
    private String gender;
    private String birthWeight;
    private String birthHeight;
    private List<String> medicalConditions;
    private String parentEmail;
    private String relationshipToParent;
}
