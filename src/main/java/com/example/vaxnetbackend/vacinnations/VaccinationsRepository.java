package com.example.vaxnetbackend.vacinnations;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface VaccinationsRepository extends MongoRepository<Vaccinations, String> {
}
