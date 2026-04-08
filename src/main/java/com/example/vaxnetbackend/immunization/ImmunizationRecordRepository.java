package com.example.vaxnetbackend.immunization;

import com.example.vaxnetbackend.children.Child;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImmunizationRecordRepository extends MongoRepository<ImmunizationRecord, String> {
    Optional<ImmunizationRecord> findByChild(Child child);
}
