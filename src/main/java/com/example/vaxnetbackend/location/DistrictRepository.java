package com.example.vaxnetbackend.location;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface DistrictRepository extends MongoRepository<District, String> {
    List<District> findByProvinceId(String provinceId);
}
