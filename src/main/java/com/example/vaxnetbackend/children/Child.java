package com.example.vaxnetbackend.children;

import com.example.vaxnetbackend.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
@TypeAlias("Patient")
public class Child {
    @Id
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
    private String relationshipToParent;
    @DBRef
    private User parent;

}
