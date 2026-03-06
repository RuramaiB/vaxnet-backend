package com.example.vaxnetbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VaxnetBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(VaxnetBackendApplication.class, args);
    }

}
