package com.example.vaxnetbackend.appointments;

import com.example.vaxnetbackend.facility.Facility;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Data
@ToString
public class AppointmentRequest {
    private String dateOfAppointment;
    private String timeOfAppointment;
    private String birthCertificateNumber;
    private String reasonForAppointment;
    private String facilityID;
    private String appointmentStatus;
}
