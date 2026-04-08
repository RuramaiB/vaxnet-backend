package com.example.vaxnetbackend.config;

import com.example.vaxnetbackend.appointments.AppointmentServices;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VaccinationScheduleConfig {

    @Bean
    public CommandLineRunner schedulingRunner(AppointmentServices appointmentServices) {
        return args -> {
            System.out.println("Starting automatic vaccination schedule check...");
            appointmentServices.scheduleForAllUnscheduledChildren();
            System.out.println("Automatic vaccination schedule check completed.");
        };
    }
}
