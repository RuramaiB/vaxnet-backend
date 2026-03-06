package com.example.vaxnetbackend.immunization;

import com.example.vaxnetbackend.children.Child;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document
@TypeAlias("ImmunizationRecord")
public class ImmunizationRecord {

    @Id
    private String id;

    @DBRef
    private Child child;

    /**
     * The full list of vaccine doses for this child based on the Zimbabwe national
     * schedule.
     * Populated automatically when the child is registered.
     */
    private List<VaccineDose> doses;
}
