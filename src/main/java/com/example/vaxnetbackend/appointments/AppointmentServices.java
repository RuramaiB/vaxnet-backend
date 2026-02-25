package com.example.vaxnetbackend.appointments;

import com.example.vaxnetbackend.children.Child;
import com.example.vaxnetbackend.children.ChildRepository;
import com.example.vaxnetbackend.facility.FacilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;


@RequiredArgsConstructor
@Service
public class AppointmentServices {
    private final AppointmentRepository appointmentRepository;
    private final FacilityRepository facilityRepository;
    private final ChildRepository childRepository;

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public ResponseEntity<AppointmentResponse> addAppointment(AppointmentRequest appointmentRequest) {
        Appointment appointment = new Appointment();
        appointment.setDateOfAppointment(appointmentRequest.getDateOfAppointment());
        appointment.setTimeOfAppointment(appointmentRequest.getTimeOfAppointment());
        appointment.setReasonForAppointment(appointmentRequest.getReasonForAppointment());
        appointment.setAppointmentStatus(appointmentRequest.getAppointmentStatus());
        childRepository.findById(appointmentRequest.getBirthCertificateNumber())
                .ifPresent(appointment::setChild);
        facilityRepository.findById(appointmentRequest.getFacilityID())
                .ifPresent(appointment::setFacility);
        appointmentRepository.save(appointment);
        return ResponseEntity.ok(AppointmentResponse
                .builder()
                .appointment(appointment)
                .msg("Appointment added successfully")
                .build());
    }

    public List<Appointment> getAppointmentsByChild(String birthCertificateNumber) {
       Child child = childRepository.findById(birthCertificateNumber)
               .orElseThrow(()-> new RuntimeException("Child not found"));
       return appointmentRepository.findAllByChild(child);
    }
}
