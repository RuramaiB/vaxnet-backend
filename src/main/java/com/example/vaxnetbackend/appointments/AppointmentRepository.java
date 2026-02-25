package com.example.vaxnetbackend.appointments;

import com.example.vaxnetbackend.children.Child;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AppointmentRepository extends MongoRepository<Appointment,String> {
    List<Appointment> findAllByChild(Child child);
}
