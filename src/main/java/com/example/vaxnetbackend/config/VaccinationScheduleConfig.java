package com.example.vaxnetbackend.config;

import com.example.vaxnetbackend.immunization.ImmunizationScheduleService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VaccinationScheduleConfig {

    @Bean
    public CommandLineRunner schedulingRunner(ImmunizationScheduleService immunizationScheduleService) {
        return args -> {
            System.out.println("Starting automatic vaccination schedule check...");
            immunizationScheduleService.generateMissingSchedules();
            System.out.println("Automatic vaccination schedule check completed.");
        };
    }
}
