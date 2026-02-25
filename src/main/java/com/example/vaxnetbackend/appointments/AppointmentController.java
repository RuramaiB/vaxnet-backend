package com.example.vaxnetbackend.appointments;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentServices appointmentServices;

    @GetMapping("/get-all-appointments")
    public List<Appointment> getAllAppointments() {
        return appointmentServices.getAllAppointments();
    }

    @PostMapping("/add-new-appointment")
    public ResponseEntity<AppointmentResponse> addAppointment(@RequestBody AppointmentRequest appointmentRequest) {
        return appointmentServices.addAppointment(appointmentRequest);
    }

    @GetMapping("/get-appointments-by-child/{birthCertificateNumber}")
    public List<Appointment> getAppointmentsByChild(@PathVariable String birthCertificateNumber) {
        return appointmentServices.getAppointmentsByChild(birthCertificateNumber);
    }
}
