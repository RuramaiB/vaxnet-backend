package com.example.vaxnetbackend.children;

import com.example.vaxnetbackend.user.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChildRepository extends MongoRepository<Child, String> {
    List<Child> findByParent(User parent);
}
