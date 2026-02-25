package com.example.vaxnetbackend.vaccines;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface VaccineRepository extends MongoRepository<Vaccine,String> {
}
