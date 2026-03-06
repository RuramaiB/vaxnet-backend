package com.example.vaxnetbackend.facility;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FacilityRepository extends MongoRepository<Facility, String> {
    List<Facility> findByFacilityDistrictContainingIgnoreCase(String district);

    List<Facility> findByStatus(String status);
}
