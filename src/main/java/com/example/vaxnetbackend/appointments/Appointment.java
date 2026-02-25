package com.example.vaxnetbackend.appointments;

import com.example.vaxnetbackend.children.Child;
import com.example.vaxnetbackend.facility.Facility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
@TypeAlias("Appointments")
public class Appointment {
    @Id
    private String appointmentID;
    private String dateOfAppointment;
    private String timeOfAppointment;
    @DBRef
    private Child child;
    private String reasonForAppointment;
    @DBRef
    private Facility facility;
    private String appointmentStatus;
}
