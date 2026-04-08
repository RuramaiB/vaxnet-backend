package com.example.vaxnetbackend.immunization;

import com.example.vaxnetbackend.children.Child;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleResponse {
    private String id;
    private Child child;
    private List<VaccineDoseDTO> doses;
}
