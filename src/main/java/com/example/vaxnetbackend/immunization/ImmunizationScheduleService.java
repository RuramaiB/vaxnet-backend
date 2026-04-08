package com.example.vaxnetbackend.immunization;

import com.example.vaxnetbackend.children.Child;
import com.example.vaxnetbackend.children.ChildRepository;
import com.example.vaxnetbackend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementing the Zimbabwe National Immunization Schedule (CHW Job
 * Aid).
 *
 * Schedule source:
 * https://www.prescribingcompanion.com/zimbabwe/.../immunisation-schedule-for-children/
 *
 * Age is computed in weeks from the child's date of birth. Statuses are
 * computed dynamically:
 * - ADMINISTERED: dose has been given
 * - NOT_YET_DUE: child has not yet reached minimum age
 * - DUE_SOON: child is within 2 weeks of the scheduled age
 * - DUE: child is at or past the scheduled age but within the max age window
 * - OVERDUE: child is past the maximum age limit and dose was never
 * administered
 */
@Service
@RequiredArgsConstructor
public class ImmunizationScheduleService {

        private final ImmunizationRecordRepository immunizationRecordRepository;
        private final ChildRepository childRepository;

        // ─────────────────────────────────────────────────────────────────────────
        // Zimbabwe National Immunization Schedule Constants
        //
        // scheduledAgeWeeks: minimum age in WEEKS when the dose should be given
        // maxAgeWeeks : maximum age in WEEKS beyond which the dose is overdue
        // (0 = no strict upper limit beyond natural schedule)
        // ─────────────────────────────────────────────────────────────────────────

        /**
         * Returns the master list of all 22 vaccine doses in the Zimbabwe schedule.
         * A fresh copy (not yet dated) is created for each child.
         */
        private List<VaccineDose> getZimbabweScheduleTemplate() {
                return new ArrayList<>(Arrays.asList(
                                // ── At Birth ────────────────────────────────────────────
                                VaccineDose.builder()
                                                .vaccineKey("BCG")
                                                .vaccineName("BCG")
                                                .scheduledAgeWeeks(0)
                                                .maxAgeWeeks(48) // max 11 months ≈ 48 weeks
                                                .route("Intradermal")
                                                .site("Insertion of right deltoid muscle")
                                                .dosage("0.05 ml")
                                                .status(VaccineStatus.NOT_YET_DUE)
                                                .build(),

                                VaccineDose.builder()
                                                .vaccineKey("HEP_B_BIRTH")
                                                .vaccineName("Hepatitis B (Birth Dose)")
                                                .scheduledAgeWeeks(0)
                                                .maxAgeWeeks(0) // must be within 24 hours — tracked separately by
                                                                // admins
                                                .route("Intramuscular")
                                                .site("Right anterolateral aspect of mid thigh")
                                                .dosage("0.5 ml")
                                                .status(VaccineStatus.NOT_YET_DUE)
                                                .build(),

                                // ── 6 Weeks ─────────────────────────────────────────────
                                VaccineDose.builder()
                                                .vaccineKey("OPV1")
                                                .vaccineName("OPV 1 (Oral Polio Vaccine)")
                                                .scheduledAgeWeeks(6)
                                                .maxAgeWeeks(100)
                                                .route("Oral")
                                                .site("Oral")
                                                .dosage("2-3 drops")
                                                .status(VaccineStatus.NOT_YET_DUE)
                                                .build(),

                                VaccineDose.builder()
                                                .vaccineKey("DPT1")
                                                .vaccineName("DPT-HepB-Hib 1")
                                                .scheduledAgeWeeks(6)
                                                .maxAgeWeeks(100) // max 23 months ≈ 100 weeks
                                                .route("Intramuscular")
                                                .site("Right anterolateral aspect of mid-thigh")
                                                .dosage("0.5 ml")
                                                .status(VaccineStatus.NOT_YET_DUE)
                                                .build(),

                                VaccineDose.builder()
                                                .vaccineKey("PCV1")
                                                .vaccineName("Pneumococcal 1 (PCV)")
                                                .scheduledAgeWeeks(6)
                                                .maxAgeWeeks(100)
                                                .route("Intramuscular")
                                                .site("Left anterolateral aspect of mid-thigh")
                                                .dosage("0.5 ml")
                                                .status(VaccineStatus.NOT_YET_DUE)
                                                .build(),

                                VaccineDose.builder()
                                                .vaccineKey("ROTA1")
                                                .vaccineName("Rotavirus 1")
                                                .scheduledAgeWeeks(6)
                                                .maxAgeWeeks(104) // max 24 months ≈ 104 weeks
                                                .route("Oral")
                                                .site("Oral")
                                                .dosage("1.5 ml")
                                                .status(VaccineStatus.NOT_YET_DUE)
                                                .build(),

                                // ── 10 Weeks ────────────────────────────────────────────
                                VaccineDose.builder()
                                                .vaccineKey("OPV2")
                                                .vaccineName("OPV 2 (Oral Polio Vaccine)")
                                                .scheduledAgeWeeks(10)
                                                .maxAgeWeeks(100)
                                                .route("Oral")
                                                .site("Oral")
                                                .dosage("2-3 drops")
                                                .status(VaccineStatus.NOT_YET_DUE)
                                                .build(),

                                VaccineDose.builder()
                                                .vaccineKey("DPT2")
                                                .vaccineName("DPT-HepB-Hib 2")
                                                .scheduledAgeWeeks(10)
                                                .maxAgeWeeks(100)
                                                .route("Intramuscular")
                                                .site("Right anterolateral aspect of mid-thigh")
                                                .dosage("0.5 ml")
                                                .status(VaccineStatus.NOT_YET_DUE)
                                                .build(),

                                VaccineDose.builder()
                                                .vaccineKey("PCV2")
                                                .vaccineName("Pneumococcal 2 (PCV)")
                                                .scheduledAgeWeeks(10)
                                                .maxAgeWeeks(100)
                                                .route("Intramuscular")
                                                .site("Left anterolateral aspect of mid-thigh")
                                                .dosage("0.5 ml")
                                                .status(VaccineStatus.NOT_YET_DUE)
                                                .build(),

                                VaccineDose.builder()
                                                .vaccineKey("ROTA2")
                                                .vaccineName("Rotavirus 2")
                                                .scheduledAgeWeeks(10)
                                                .maxAgeWeeks(104)
                                                .route("Oral")
                                                .site("Oral")
                                                .dosage("1.5 ml")
                                                .status(VaccineStatus.NOT_YET_DUE)
                                                .build(),

                                // ── 14 Weeks ────────────────────────────────────────────
                                VaccineDose.builder()
                                                .vaccineKey("OPV3")
                                                .vaccineName("OPV 3 (Oral Polio Vaccine)")
                                                .scheduledAgeWeeks(14)
                                                .maxAgeWeeks(100)
                                                .route("Oral")
                                                .site("Oral")
                                                .dosage("2-3 drops")
                                                .status(VaccineStatus.NOT_YET_DUE)
                                                .build(),

                                VaccineDose.builder()
                                                .vaccineKey("IPV")
                                                .vaccineName("Inactivated Polio Vaccine (IPV)")
                                                .scheduledAgeWeeks(14)
                                                .maxAgeWeeks(100)
                                                .route("Intramuscular")
                                                .site("2.5 cm from PCV site")
                                                .dosage("0.5 ml")
                                                .status(VaccineStatus.NOT_YET_DUE)
                                                .build(),

                                VaccineDose.builder()
                                                .vaccineKey("DPT3")
                                                .vaccineName("DPT-HepB-Hib 3")
                                                .scheduledAgeWeeks(14)
                                                .maxAgeWeeks(100)
                                                .route("Intramuscular")
                                                .site("Right anterolateral aspect of mid-thigh")
                                                .dosage("0.5 ml")
                                                .status(VaccineStatus.NOT_YET_DUE)
                                                .build(),

                                VaccineDose.builder()
                                                .vaccineKey("PCV3")
                                                .vaccineName("Pneumococcal 3 (PCV)")
                                                .scheduledAgeWeeks(14)
                                                .maxAgeWeeks(100)
                                                .route("Intramuscular")
                                                .site("Left anterolateral aspect of mid-thigh")
                                                .dosage("0.5 ml")
                                                .status(VaccineStatus.NOT_YET_DUE)
                                                .build(),

                                // ── 9 Months ────────────────────────────────────────────
                                VaccineDose.builder()
                                                .vaccineKey("MR1")
                                                .vaccineName("Measles & Rubella 1 (MR)")
                                                .scheduledAgeWeeks(39) // 9 months ≈ 39 weeks
                                                .maxAgeWeeks(260) // max 5 years ≈ 260 weeks
                                                .route("Subcutaneous")
                                                .site("Left deltoid muscle")
                                                .dosage("0.5 ml")
                                                .status(VaccineStatus.NOT_YET_DUE)
                                                .build(),

                                VaccineDose.builder()
                                                .vaccineKey("TCV")
                                                .vaccineName("TCV (Typhoid Conjugate Vaccine)")
                                                .scheduledAgeWeeks(39)
                                                .maxAgeWeeks(0)
                                                .route("Intramuscular")
                                                .site("Left anterolateral aspect of mid thigh")
                                                .dosage("0.5 ml")
                                                .status(VaccineStatus.NOT_YET_DUE)
                                                .build(),

                                // ── 18 Months ───────────────────────────────────────────
                                VaccineDose.builder()
                                                .vaccineKey("MR2")
                                                .vaccineName("Measles & Rubella 2 (MR)")
                                                .scheduledAgeWeeks(78) // 18 months ≈ 78 weeks
                                                .maxAgeWeeks(0)
                                                .route("Subcutaneous")
                                                .site("Left deltoid muscle")
                                                .dosage("0.5 ml")
                                                .status(VaccineStatus.NOT_YET_DUE)
                                                .build(),

                                VaccineDose.builder()
                                                .vaccineKey("DPT_BOOSTER")
                                                .vaccineName("DPT Booster")
                                                .scheduledAgeWeeks(78)
                                                .maxAgeWeeks(0)
                                                .route("Intramuscular")
                                                .site("Right anterolateral aspect of mid-thigh")
                                                .dosage("0.5 ml")
                                                .status(VaccineStatus.NOT_YET_DUE)
                                                .build(),

                                VaccineDose.builder()
                                                .vaccineKey("OPV_BOOSTER")
                                                .vaccineName("OPV Booster")
                                                .scheduledAgeWeeks(78)
                                                .maxAgeWeeks(0)
                                                .route("Oral")
                                                .site("Oral")
                                                .dosage("2-3 drops")
                                                .status(VaccineStatus.NOT_YET_DUE)
                                                .build(),

                                // ── 5 Years ─────────────────────────────────────────────
                                VaccineDose.builder()
                                                .vaccineKey("TD_BOOSTER_5YR")
                                                .vaccineName("Td Booster (5 Years)")
                                                .scheduledAgeWeeks(260) // 5 years ≈ 260 weeks
                                                .maxAgeWeeks(0)
                                                .route("Intramuscular")
                                                .site("Left deltoid muscle")
                                                .dosage("0.5 ml")
                                                .status(VaccineStatus.NOT_YET_DUE)
                                                .build(),

                                // ── 10 Years ────────────────────────────────────────────
                                VaccineDose.builder()
                                                .vaccineKey("TD_BOOSTER_10YR")
                                                .vaccineName("Td Booster (10 Years)")
                                                .scheduledAgeWeeks(521) // 10 years ≈ 521 weeks
                                                .maxAgeWeeks(0)
                                                .route("Intramuscular")
                                                .site("Left deltoid muscle")
                                                .dosage("0.5 ml")
                                                .status(VaccineStatus.NOT_YET_DUE)
                                                .build(),

                                VaccineDose.builder()
                                                .vaccineKey("HPV")
                                                .vaccineName("Human Papilloma Virus (HPV)")
                                                .scheduledAgeWeeks(521)
                                                .maxAgeWeeks(574) // 11 years ≈ 574 weeks (second dose at 11yr)
                                                .route("Intramuscular")
                                                .site("Right deltoid muscle")
                                                .dosage("0.5 ml")
                                                .status(VaccineStatus.NOT_YET_DUE)
                                                .build()));
        }

        /**
         * Generates and saves an immunization schedule for the given child.
         * Called automatically when a child is registered.
         */
        public ImmunizationRecord generateSchedule(Child child) {
                // If a record already exists, return it (idempotent)
                return immunizationRecordRepository.findByChild(child)
                                .orElseGet(() -> {
                                        LocalDate dob = LocalDate.parse(child.getDateOfBirth());
                                        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

                                        List<VaccineDose> doses = getZimbabweScheduleTemplate().stream()
                                                        .map(dose -> {
                                                                LocalDate scheduledDate = dob
                                                                                .plusWeeks(dose.getScheduledAgeWeeks());
                                                                VaccineStatus status = computeStatus(dob, dose, null);
                                                                return VaccineDose.builder()
                                                                                .vaccineKey(dose.getVaccineKey())
                                                                                .vaccineName(dose.getVaccineName())
                                                                                .scheduledAgeWeeks(dose
                                                                                                .getScheduledAgeWeeks())
                                                                                .maxAgeWeeks(dose.getMaxAgeWeeks())
                                                                                .route(dose.getRoute())
                                                                                .site(dose.getSite())
                                                                                .dosage(dose.getDosage())
                                                                                .scheduledDate(scheduledDate
                                                                                                .format(fmt))
                                                                                .administeredDate(null)
                                                                                .batchNumber(null)
                                                                                .status(status)
                                                                                .build();
                                                        })
                                                        .collect(Collectors.toList());

                                        ImmunizationRecord record = ImmunizationRecord.builder()
                                                        .child(child)
                                                        .doses(doses)
                                                        .build();

                                        return immunizationRecordRepository.save(record);
                                });
        }

        /**
         * Returns the immunization schedule for a child, with statuses freshly
         * recomputed.
         */
        public ImmunizationRecord getScheduleWithStatus(String birthCertificateNumber) {
                Child child = childRepository.findById(birthCertificateNumber)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Child not found: " + birthCertificateNumber));

                ImmunizationRecord record = immunizationRecordRepository.findByChild(child)
                                .orElseGet(() -> generateSchedule(child)); // auto-generate if missing

                LocalDate dob = LocalDate.parse(child.getDateOfBirth());

                // Recompute statuses live (doses that were previously NOT_YET_DUE may now be
                // DUE)
                List<VaccineDose> refreshed = record.getDoses().stream()
                                .map(dose -> {
                                        if (dose.getStatus() == VaccineStatus.ADMINISTERED) {
                                                return dose; // already administered — unchanged
                                        }
                                        VaccineStatus fresh = computeStatus(dob, dose, null);
                                        dose.setStatus(fresh);
                                        return dose;
                                })
                                .collect(Collectors.toList());

                record.setDoses(refreshed);
                return immunizationRecordRepository.save(record);
        }

        /**
         * Marks a specific vaccine dose as administered for the given child.
         * 
         * @param administeredDateStr optional ISO date (yyyy-MM-dd); if null, defaults
         *                            to today.
         *                            Used by parents when recording a catch-up dose
         *                            given in the past.
         */
        public ImmunizationRecord administerVaccine(String birthCertificateNumber, String vaccineKey,
                        String batchNumber, String administeredDateStr) {
                Child child = childRepository.findById(birthCertificateNumber)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Child not found: " + birthCertificateNumber));

                ImmunizationRecord record = immunizationRecordRepository.findByChild(child)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Immunization record not found for child: " + birthCertificateNumber));

                String dateToRecord = (administeredDateStr != null && !administeredDateStr.isBlank())
                                ? administeredDateStr
                                : LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

                List<VaccineDose> updated = record.getDoses().stream()
                                .map(dose -> {
                                        if (dose.getVaccineKey().equals(vaccineKey)) {
                                                dose.setStatus(VaccineStatus.ADMINISTERED);
                                                dose.setAdministeredDate(dateToRecord);
                                                dose.setBatchNumber(batchNumber);
                                        }
                                        return dose;
                                })
                                .collect(Collectors.toList());

                record.setDoses(updated);
                return immunizationRecordRepository.save(record);
        }

        /**
         * Returns all immunization records that have at least one overdue dose.
         */
        public List<ImmunizationRecord> getAllWithOverdue() {
                return immunizationRecordRepository.findAll().stream()
                                .filter(record -> record.getDoses().stream()
                                                .anyMatch(dose -> {
                                                        if (dose.getStatus() == VaccineStatus.ADMINISTERED)
                                                                return false;
                                                        LocalDate dob = LocalDate
                                                                        .parse(record.getChild().getDateOfBirth());
                                                        VaccineStatus s = computeStatus(dob, dose, null);
                                                        dose.setStatus(s);
                                                        return s == VaccineStatus.OVERDUE;
                                                }))
                                .collect(Collectors.toList());
        }

        /**
         * Returns all immunization records that have at least one dose due today or
         * within 2 weeks.
         */
        public List<ImmunizationRecord> getAllDueSoon() {
                return immunizationRecordRepository.findAll().stream()
                                .filter(record -> record.getDoses().stream()
                                                .anyMatch(dose -> {
                                                        if (dose.getStatus() == VaccineStatus.ADMINISTERED)
                                                                return false;
                                                        LocalDate dob = LocalDate
                                                                        .parse(record.getChild().getDateOfBirth());
                                                        VaccineStatus s = computeStatus(dob, dose, null);
                                                        dose.setStatus(s);
                                                        return s == VaccineStatus.DUE || s == VaccineStatus.DUE_SOON;
                                                }))
                                .collect(Collectors.toList());
        }

        // ─────────────────────────────────────────────────────────────────────────
        // Private helpers
        // ─────────────────────────────────────────────────────────────────────────

        /**
         * Computes the current status of a dose given the child's DOB and the dose
         * template.
         */
        private VaccineStatus computeStatus(LocalDate dob, VaccineDose dose, String existingAdminDate) {
                if (existingAdminDate != null) {
                        return VaccineStatus.ADMINISTERED;
                }

                LocalDate today = LocalDate.now();
                LocalDate scheduledDate = dob.plusWeeks(dose.getScheduledAgeWeeks());

                long daysUntilDue = ChronoUnit.DAYS.between(today, scheduledDate);

                // Check if past maximum age
                if (dose.getMaxAgeWeeks() > 0) {
                        LocalDate maxDate = dob.plusWeeks(dose.getMaxAgeWeeks());
                        if (today.isAfter(maxDate)) {
                                return VaccineStatus.OVERDUE;
                        }
                }

                if (today.isBefore(scheduledDate)) {
                        // Not yet reached scheduled date
                        if (daysUntilDue <= 14) {
                                return VaccineStatus.DUE_SOON;
                        }
                        return VaccineStatus.NOT_YET_DUE;
                }

                // Today is on or after the scheduled date
                return VaccineStatus.DUE;
        }
}
