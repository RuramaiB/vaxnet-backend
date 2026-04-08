package com.example.vaxnetbackend.children;

import com.example.vaxnetbackend.immunization.ImmunizationScheduleService;
import com.example.vaxnetbackend.user.User;
import com.example.vaxnetbackend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ChildServices {
    private final ChildRepository childRepository;
    private final UserRepository userRepository;
    private final ImmunizationScheduleService immunizationScheduleService;

    public List<Child> getAllChildren() {
        return childRepository.findAll();
    }

    public ResponseEntity<ChildResponse> addNewChild(ChildRequest childRequest) {
        Child child = new Child();
        User parent = userRepository.findByEmail(childRequest.getParentEmail())
                .orElseThrow(() -> new IllegalStateException(
                        "Parent with email " + childRequest.getParentEmail() + " does not exist"));
        child.setParent(parent);
        child.setBirthCertificateNumber(childRequest.getBirthCertificateNumber());
        child.setFirstName(childRequest.getFirstName());
        child.setLastName(childRequest.getLastName());
        child.setDateOfBirth(childRequest.getDateOfBirth());
        child.setPlaceOfBirth(childRequest.getPlaceOfBirth());
        child.setPhysicalAddress(childRequest.getPhysicalAddress());
        child.setGender(childRequest.getGender());
        child.setBirthWeight(childRequest.getBirthWeight());
        child.setBirthHeight(childRequest.getBirthHeight());
        child.setMedicalConditions(childRequest.getMedicalConditions());
        child.setRelationshipToParent(childRequest.getRelationshipToParent());
        childRepository.save(child);

        // ── Auto-generate the Zimbabwe immunization schedule for this child ──
        try {
            immunizationScheduleService.generateSchedule(child);
        } catch (Exception e) {
            // Log but don't fail registration if schedule generation fails
            System.err.println("Warning: Failed to auto-generate immunization schedule for child "
                    + child.getBirthCertificateNumber() + ": " + e.getMessage());
        }

        return ResponseEntity.ok(ChildResponse
                .builder()
                .child(child)
                .msg("Child added successfully")
                .build());
    }

    public List<Child> getChildrenByParentEmail(String parentEmail) {
        User parent = userRepository.findByEmail(parentEmail)
                .orElseThrow(() -> new IllegalStateException("Parent with email " + parentEmail + " does not exist"));
        return childRepository.findByParent(parent);
    }
}
