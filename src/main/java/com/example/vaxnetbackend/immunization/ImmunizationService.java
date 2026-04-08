package com.example.vaxnetbackend.immunization;

import com.example.vaxnetbackend.children.Child;
import com.example.vaxnetbackend.children.ChildRepository;
import com.example.vaxnetbackend.notifications.NotificationService;
import com.example.vaxnetbackend.user.User;
import com.example.vaxnetbackend.user.UserRepository;
import com.example.vaxnetbackend.vaccines.Vaccine;
import com.example.vaxnetbackend.vaccines.VaccineRepository;
import com.example.vaxnetbackend.vacinnations.Vaccinations;
import com.example.vaxnetbackend.vacinnations.VaccinationsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImmunizationService {

    private final ChildRepository childRepository;
    private final VaccinationsRepository vaccinationsRepository;
    private final VaccineRepository vaccineRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public List<ScheduleResponse> getSchedulesForParent(String parentEmail) {
        User parent = userRepository.findByEmail(parentEmail)
                .orElseThrow(() -> new RuntimeException("Parent not found"));

        List<Child> children = childRepository.findByParent(parent);
        List<ScheduleResponse> schedules = new ArrayList<>();

        for (Child child : children) {
            schedules.add(getScheduleForChild(child.getBirthCertificateNumber()));
        }

        return schedules;
    }

    public ScheduleResponse getScheduleForChild(String birthCertificateNumber) {
        Child child = childRepository.findById(birthCertificateNumber)
                .orElseThrow(() -> new RuntimeException("Child not found"));

        List<Vaccinations> actualVaccinations = vaccinationsRepository.findAll().stream()
                .filter(v -> v.getChild() != null && v.getChild().getBirthCertificateNumber().equals(birthCertificateNumber))
                .toList();

        List<VaccineDoseDTO> doses = new ArrayList<>();
        LocalDate dob = LocalDate.parse(child.getDateOfBirth());

        // Standard Zimbabwe Immunization Schedule
        addDose(doses, "BCG", "BCG", 0, "Intradermal", "Right Forearm", "0.05ml", dob, actualVaccinations);
        addDose(doses, "OPV_0", "OPV 0", 0, "Oral", "Mouth", "2 drops", dob, actualVaccinations);
        
        addDose(doses, "PENTA_1", "Pentavalent 1", 6, "Intramuscular", "Left Thigh", "0.5ml", dob, actualVaccinations);
        addDose(doses, "OPV_1", "OPV 1", 6, "Oral", "Mouth", "2 drops", dob, actualVaccinations);
        addDose(doses, "PCV_1", "PCV 1", 6, "Intramuscular", "Right Thigh", "0.5ml", dob, actualVaccinations);
        addDose(doses, "ROTA_1", "Rota 1", 6, "Oral", "Mouth", "1.5ml", dob, actualVaccinations);
        
        addDose(doses, "PENTA_2", "Pentavalent 2", 10, "Intramuscular", "Left Thigh", "0.5ml", dob, actualVaccinations);
        addDose(doses, "OPV_2", "OPV 2", 10, "Oral", "Mouth", "2 drops", dob, actualVaccinations);
        addDose(doses, "PCV_2", "PCV 2", 10, "Intramuscular", "Right Thigh", "0.5ml", dob, actualVaccinations);
        addDose(doses, "ROTA_2", "Rota 2", 10, "Oral", "Mouth", "1.5ml", dob, actualVaccinations);
        
        addDose(doses, "PENTA_3", "Pentavalent 3", 14, "Intramuscular", "Left Thigh", "0.5ml", dob, actualVaccinations);
        addDose(doses, "OPV_3", "OPV 3", 14, "Oral", "Mouth", "2 drops", dob, actualVaccinations);
        addDose(doses, "PCV_3", "PCV 3", 14, "Intramuscular", "Right Thigh", "0.5ml", dob, actualVaccinations);
        addDose(doses, "IPV", "IPV", 14, "Intramuscular", "Left Thigh", "0.5ml", dob, actualVaccinations);
        
        addDose(doses, "MR_1", "Measles-Rubella 1", 39, "Subcutaneous", "Right Upper Arm", "0.5ml", dob, actualVaccinations);
        addDose(doses, "MR_2", "Measles-Rubella 2", 78, "Subcutaneous", "Right Upper Arm", "0.5ml", dob, actualVaccinations);

        return ScheduleResponse.builder()
                .child(child)
                .doses(doses)
                .build();
    }

    private void addDose(List<VaccineDoseDTO> doses, String key, String name, int weeks, String route, String site, String dosage, LocalDate dob, List<Vaccinations> actual) {
        LocalDate scheduledDate = dob.plusWeeks(weeks);
        
        // Check if there is a matching vaccination record
        Optional<Vaccinations> record = actual.stream()
                .filter(v -> v.getVaccine() != null && v.getVaccine().getVaccineName().equalsIgnoreCase(name))
                .findFirst();

        VaccineDoseDTO.VaccineDoseDTOBuilder builder = VaccineDoseDTO.builder()
                .vaccineKey(key)
                .vaccineName(name)
                .scheduledAgeWeeks(weeks)
                .route(route)
                .site(site)
                .dosage(dosage)
                .scheduledDate(scheduledDate);

        if (record.isPresent()) {
            builder.administeredDate(record.get().getVaccinationDate())
                   .batchNumber(record.get().getNarration() != null ? record.get().getNarration() : "N/A")
                   .status("ADMINISTERED");
        } else {
            LocalDate now = LocalDate.now();
            if (now.isAfter(scheduledDate.plusDays(7))) {
                builder.status("OVERDUE");
            } else if (now.isAfter(scheduledDate.minusDays(7))) {
                builder.status("DUE");
            } else if (now.isAfter(scheduledDate.minusDays(14))) {
                builder.status("DUE_SOON");
            } else {
                builder.status("NOT_YET_DUE");
            }
        }

        doses.add(builder.build());
    }

    public ScheduleResponse administerVaccine(String birthCertificateNumber, String vaccineKey, String batchNumber) {
        Child child = childRepository.findById(birthCertificateNumber)
                .orElseThrow(() -> new RuntimeException("Child not found"));

        // Map vaccineKey back to a vaccine name
        String vaccineName = getVaccineNameFromKey(vaccineKey);
        
        // Find the vaccine entity
        Vaccine vaccine = vaccineRepository.findAll().stream()
                .filter(v -> v.getVaccineName().equalsIgnoreCase(wineNameFix(vaccineName)))
                .findFirst()
                .orElse(null);

        // Save new vaccination record
        Vaccinations record = new Vaccinations();
        record.setChild(child);
        record.setVaccinationDate(LocalDate.now());
        record.setNarration(batchNumber);
        record.setStatus("ADMINISTERED");
        record.setVaccine(vaccine);
        vaccinationsRepository.save(record);

        // Send SMS notification via Twilio
        if (child.getParent() != null && child.getParent().getPhoneNumber() != null) {
            String message = String.format("VaxNet Update: Your child %s has received their %s dose today. Batch: %s. Next milestone: %s.",
                    child.getFirstName(), vaccineName, batchNumber, getNextMilestoneDescription(vaccineKey));
            notificationService.broadcastSms(Collections.singletonList(child.getParent().getPhoneNumber()), message);
        }

        return getScheduleForChild(birthCertificateNumber);
    }

    private String wineNameFix(String name) {
        // Handle name mapping if needed
        return name;
    }

    private String getVaccineNameFromKey(String key) {
        // Simplified mapping back from key
        if (key.startsWith("PENTA")) return "Pentavalent " + key.split("_")[1];
        if (key.startsWith("OPV")) return "OPV " + key.split("_")[1];
        if (key.startsWith("PCV")) return "PCV " + key.split("_")[1];
        if (key.startsWith("ROTA")) return "Rota " + key.split("_")[1];
        if (key.equals("BCG")) return "BCG";
        if (key.equals("IPV")) return "IPV";
        if (key.equals("MR_1")) return "Measles-Rubella 1";
        if (key.equals("MR_2")) return "Measles-Rubella 2";
        return key;
    }

    private String getNextMilestoneDescription(String currentKey) {
        if (currentKey.contains("_0") || currentKey.equals("BCG")) return "6 Weeks";
        if (currentKey.contains("_1")) return "10 Weeks";
        if (currentKey.contains("_2")) return "14 Weeks";
        if (currentKey.contains("_3") || currentKey.equals("IPV")) return "9 Months";
        if (currentKey.equals("MR_1")) return "18 Months";
        return "Growth Monitoring";
    }
}
