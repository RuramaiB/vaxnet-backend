package com.example.vaxnetbackend.insights;

import com.example.vaxnetbackend.children.Child;
import com.example.vaxnetbackend.children.ChildRepository;
import com.example.vaxnetbackend.immunization.ImmunizationRecord;
import com.example.vaxnetbackend.immunization.ImmunizationRecordRepository;
import com.example.vaxnetbackend.immunization.VaccineDose;
import com.example.vaxnetbackend.immunization.VaccineStatus;
import com.example.vaxnetbackend.user.User;
import com.example.vaxnetbackend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * AI Data Analysis and Insight Engine for VaxNet.
 *
 * Uses rule-based statistical analysis of live MongoDB data to generate:
 * - National & regional vaccination coverage rates
 * - Vaccine-specific uptake rates
 * - Parent behaviour scores (compliance index)
 * - Age-milestone compliance
 * - Monthly trend data
 * - AI-style narrative insight paragraphs
 */
@Service
@RequiredArgsConstructor
public class InsightEngine {

    private final ImmunizationRecordRepository immunizationRecordRepository;
    private final ChildRepository childRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MMM yyyy");

    // ─────────────────────────────────────────────────────────────────────────
    // Overview (National Summary)
    // ─────────────────────────────────────────────────────────────────────────

    public Map<String, Object> getOverview() {
        List<ImmunizationRecord> records = immunizationRecordRepository.findAll();
        int totalChildren = records.size();

        if (totalChildren == 0) {
            return Map.of(
                    "totalChildren", 0,
                    "totalDoses", 0,
                    "administeredDoses", 0,
                    "coveragePercent", 0.0,
                    "overdueChildren", 0,
                    "overduePercent", 0.0,
                    "fullyVaccinatedChildren", 0,
                    "fullyVaccinatedPercent", 0.0);
        }

        int totalDoses = 0, administeredDoses = 0, overdueChildren = 0, fullyVaccinated = 0;

        for (ImmunizationRecord r : records) {
            boolean hasOverdue = false;
            boolean allDone = true;
            for (VaccineDose d : r.getDoses()) {
                totalDoses++;
                if (d.getStatus() == VaccineStatus.ADMINISTERED) {
                    administeredDoses++;
                } else {
                    allDone = false;
                    if (d.getStatus() == VaccineStatus.OVERDUE)
                        hasOverdue = true;
                }
            }
            if (hasOverdue)
                overdueChildren++;
            if (allDone)
                fullyVaccinated++;
        }

        double coveragePct = totalDoses > 0 ? (administeredDoses * 100.0 / totalDoses) : 0;
        double overduePct = totalChildren > 0 ? (overdueChildren * 100.0 / totalChildren) : 0;
        double fullPct = totalChildren > 0 ? (fullyVaccinated * 100.0 / totalChildren) : 0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalChildren", totalChildren);
        result.put("totalDoses", totalDoses);
        result.put("administeredDoses", administeredDoses);
        result.put("coveragePercent", Math.round(coveragePct * 10.0) / 10.0);
        result.put("overdueChildren", overdueChildren);
        result.put("overduePercent", Math.round(overduePct * 10.0) / 10.0);
        result.put("fullyVaccinatedChildren", fullyVaccinated);
        result.put("fullyVaccinatedPercent", Math.round(fullPct * 10.0) / 10.0);
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // By District
    // ─────────────────────────────────────────────────────────────────────────

    public List<Map<String, Object>> getByDistrict() {
        List<ImmunizationRecord> records = immunizationRecordRepository.findAll();

        // Group records by the child's district (extracted from physicalAddress or
        // placeOfBirth)
        Map<String, List<ImmunizationRecord>> byDistrict = new HashMap<>();
        for (ImmunizationRecord r : records) {
            if (r.getChild() == null)
                continue;
            String district = extractDistrict(r.getChild());
            byDistrict.computeIfAbsent(district, k -> new ArrayList<>()).add(r);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<ImmunizationRecord>> entry : byDistrict.entrySet()) {
            int total = 0, administered = 0, overdue = 0;
            for (ImmunizationRecord r : entry.getValue()) {
                for (VaccineDose d : r.getDoses()) {
                    total++;
                    if (d.getStatus() == VaccineStatus.ADMINISTERED)
                        administered++;
                    else if (d.getStatus() == VaccineStatus.OVERDUE)
                        overdue++;
                }
            }
            double pct = total > 0 ? Math.round(administered * 100.0 / total * 10) / 10.0 : 0;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("district", entry.getKey());
            row.put("children", entry.getValue().size());
            row.put("totalDoses", total);
            row.put("administeredDoses", administered);
            row.put("coveragePercent", pct);
            row.put("overdueDoses", overdue);
            result.add(row);
        }

        result.sort((a, b) -> Double.compare((Double) b.get("coveragePercent"), (Double) a.get("coveragePercent")));
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Per-Vaccine Coverage
    // ─────────────────────────────────────────────────────────────────────────

    public List<Map<String, Object>> getVaccineCoverage() {
        List<ImmunizationRecord> records = immunizationRecordRepository.findAll();

        // vaccineKey → [total, administered]
        Map<String, int[]> vaccineStats = new LinkedHashMap<>();

        for (ImmunizationRecord r : records) {
            for (VaccineDose d : r.getDoses()) {
                vaccineStats.computeIfAbsent(d.getVaccineKey(), k -> new int[] { 0, 0, 0 });
                vaccineStats.get(d.getVaccineKey())[0]++; // total
                if (d.getStatus() == VaccineStatus.ADMINISTERED)
                    vaccineStats.get(d.getVaccineKey())[1]++;
                if (d.getStatus() == VaccineStatus.OVERDUE)
                    vaccineStats.get(d.getVaccineKey())[2]++;
            }
        }

        // Also track vaccine display names
        Map<String, String> vaccineNames = new HashMap<>();
        for (ImmunizationRecord r : records) {
            for (VaccineDose d : r.getDoses()) {
                vaccineNames.put(d.getVaccineKey(), d.getVaccineName());
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : vaccineStats.entrySet()) {
            int total = entry.getValue()[0];
            int administered = entry.getValue()[1];
            int overdue = entry.getValue()[2];
            double pct = total > 0 ? Math.round(administered * 100.0 / total * 10) / 10.0 : 0;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("vaccineKey", entry.getKey());
            row.put("vaccineName", vaccineNames.getOrDefault(entry.getKey(), entry.getKey()));
            row.put("total", total);
            row.put("administered", administered);
            row.put("overdue", overdue);
            row.put("coveragePercent", pct);
            result.add(row);
        }

        result.sort((a, b) -> Double.compare((Double) b.get("coveragePercent"), (Double) a.get("coveragePercent")));
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Monthly Trends
    // ─────────────────────────────────────────────────────────────────────────

    public List<Map<String, Object>> getMonthlyTrends() {
        List<ImmunizationRecord> records = immunizationRecordRepository.findAll();
        Map<YearMonth, Integer> monthly = new TreeMap<>();

        for (ImmunizationRecord r : records) {
            for (VaccineDose d : r.getDoses()) {
                if (d.getAdministeredDate() != null && !d.getAdministeredDate().isEmpty()) {
                    try {
                        YearMonth ym = YearMonth.from(LocalDate.parse(d.getAdministeredDate()));
                        monthly.merge(ym, 1, Integer::sum);
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<YearMonth, Integer> entry : monthly.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("month", entry.getKey().format(MONTH_FMT));
            row.put("dosesAdministered", entry.getValue());
            result.add(row);
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Per-Child Insight
    // ─────────────────────────────────────────────────────────────────────────

    public Map<String, Object> getChildInsight(String birthCertificateNumber) {
        Child child = childRepository.findById(birthCertificateNumber)
                .orElse(null);
        if (child == null)
            return Map.of("error", "Child not found");

        ImmunizationRecord record = immunizationRecordRepository.findByChild(child).orElse(null);
        if (record == null)
            return Map.of("error", "No immunization record for this child");

        int total = record.getDoses().size();
        long administered = record.getDoses().stream().filter(d -> d.getStatus() == VaccineStatus.ADMINISTERED).count();
        long overdue = record.getDoses().stream().filter(d -> d.getStatus() == VaccineStatus.OVERDUE).count();
        long due = record.getDoses().stream()
                .filter(d -> d.getStatus() == VaccineStatus.DUE || d.getStatus() == VaccineStatus.DUE_SOON).count();
        double pct = total > 0 ? Math.round(administered * 100.0 / total * 10) / 10.0 : 0;

        String complianceLevel;
        if (pct >= 80)
            complianceLevel = "EXCELLENT";
        else if (pct >= 60)
            complianceLevel = "GOOD";
        else if (pct >= 40)
            complianceLevel = "FAIR";
        else
            complianceLevel = "NEEDS_ATTENTION";

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("childName", child.getFirstName() + " " + child.getLastName());
        result.put("birthCertificateNumber", birthCertificateNumber);
        result.put("dateOfBirth", child.getDateOfBirth());
        result.put("totalDoses", total);
        result.put("administeredDoses", (int) administered);
        result.put("overdueDoses", (int) overdue);
        result.put("dueDoses", (int) due);
        result.put("coveragePercent", pct);
        result.put("complianceLevel", complianceLevel);
        result.put("insight", generateChildNarrative(child.getFirstName(), pct, (int) overdue, (int) due));
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Per-Parent Insight
    // ─────────────────────────────────────────────────────────────────────────

    public Map<String, Object> getParentInsight(String email) {
        User parent = userRepository.findByEmail(email).orElse(null);
        if (parent == null)
            return Map.of("error", "Parent not found");

        List<Child> children = childRepository.findByParent(parent);
        if (children.isEmpty())
            return Map.of("error", "No children registered for this parent");

        int totalDoses = 0, administered = 0, overdue = 0;
        List<String> childNames = new ArrayList<>();

        for (Child child : children) {
            childNames.add(child.getFirstName() + " " + child.getLastName());
            immunizationRecordRepository.findByChild(child).ifPresent(r -> {
                r.getDoses().forEach(d -> {
                    // Note: these are effectively final inside lambda
                });
            });
            Optional<ImmunizationRecord> rec = immunizationRecordRepository.findByChild(child);
            if (rec.isPresent()) {
                for (VaccineDose d : rec.get().getDoses()) {
                    totalDoses++;
                    if (d.getStatus() == VaccineStatus.ADMINISTERED)
                        administered++;
                    if (d.getStatus() == VaccineStatus.OVERDUE)
                        overdue++;
                }
            }
        }

        double pct = totalDoses > 0 ? Math.round(administered * 100.0 / totalDoses * 10) / 10.0 : 0;
        int score = computeParentBehaviourScore(pct, overdue, children.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("parentName", parent.getFirstname() + " " + parent.getLastname());
        result.put("email", email);
        result.put("children", childNames);
        result.put("numberOfChildren", children.size());
        result.put("totalDoses", totalDoses);
        result.put("administeredDoses", administered);
        result.put("overdueDoses", overdue);
        result.put("coveragePercent", pct);
        result.put("parentBehaviourScore", score);
        result.put("behaviourRating",
                score >= 80 ? "Exemplary" : score >= 60 ? "Good" : score >= 40 ? "Needs Improvement" : "At Risk");
        result.put("insight", generateParentNarrative(parent.getFirstname(), children.size(), pct, overdue));
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Narrative (AI-style paragraph)
    // ─────────────────────────────────────────────────────────────────────────

    public String generateNarrative() {
        Map<String, Object> overview = getOverview();
        List<Map<String, Object>> districts = getByDistrict();
        List<Map<String, Object>> vaccines = getVaccineCoverage();

        int totalChildren = (int) overview.get("totalChildren");
        double coverage = (Double) overview.get("coveragePercent");
        double overduePct = (Double) overview.get("overduePercent");
        double fullPct = (Double) overview.get("fullyVaccinatedPercent");

        if (totalChildren == 0) {
            return "No immunization data is available yet. Register children and begin recording vaccinations to generate insights.";
        }

        String topDistrict = districts.isEmpty() ? "Unknown" : (String) districts.get(0).get("district");
        double topCoverage = districts.isEmpty() ? 0.0 : (Double) districts.get(0).get("coveragePercent");
        String bottomDistrict = districts.size() > 1
                ? (String) districts.get(districts.size() - 1).get("district")
                : "Unknown";
        double bottomCoverage = districts.size() > 1
                ? (Double) districts.get(districts.size() - 1).get("coveragePercent")
                : 0.0;

        String topVaccine = vaccines.isEmpty() ? "BCG" : (String) vaccines.get(0).get("vaccineName");
        String lowestVaccine = vaccines.size() > 1
                ? (String) vaccines.get(vaccines.size() - 1).get("vaccineName")
                : "HPV";
        double lowestVaccinePct = vaccines.size() > 1
                ? (Double) vaccines.get(vaccines.size() - 1).get("coveragePercent")
                : 0.0;

        String coverageAssessment = coverage >= 80 ? "strong"
                : coverage >= 60 ? "moderate" : "low";

        String urgencyStatement = overduePct > 20
                ? String.format(" Critical attention is required as %.1f%% of children have overdue doses.", overduePct)
                : overduePct > 10
                        ? String.format(" %.1f%% of children have overdue immunizations and should be prioritised.",
                                overduePct)
                        : " The overdue rate is within acceptable thresholds.";

        return String.format(
                "VaxNet National Immunization Insight Report – %s\n\n" +
                        "The Zimbabwe national immunization programme currently has %d children enrolled in VaxNet " +
                        "with an overall vaccination coverage rate of %.1f%%, which is considered %s. " +
                        "%.1f%% of enrolled children have completed all scheduled immunizations.%s\n\n" +
                        "Regional Analysis: %s leads with %.1f%% coverage, while %s shows the lowest coverage at %.1f%%. "
                        +
                        "Regional health teams should deploy CHWs to underperforming districts to close the gap.\n\n" +
                        "Vaccine Uptake: %s has the highest uptake rate, while %s remains the least administered vaccine "
                        +
                        "at only %.1f%% coverage. Community sensitisation campaigns should specifically target low-uptake vaccines.\n\n"
                        +
                        "Recommendation: Focus resources on districts with below 60%% coverage, ensure CHW home visits "
                        +
                        "for overdue children, and engage parents through multi-channel reminders (SMS, email, in-app).",
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                totalChildren, coverage, coverageAssessment, fullPct,
                urgencyStatement,
                topDistrict, topCoverage, bottomDistrict, bottomCoverage,
                topVaccine, lowestVaccine, lowestVaccinePct);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private String extractDistrict(Child child) {
        if (child.getPlaceOfBirth() != null && !child.getPlaceOfBirth().isBlank()) {
            return child.getPlaceOfBirth().trim();
        }
        if (child.getPhysicalAddress() != null && !child.getPhysicalAddress().isBlank()) {
            String addr = child.getPhysicalAddress().trim();
            // Try to extract last meaningful word as district proxy
            String[] parts = addr.split(",");
            return parts[parts.length - 1].trim();
        }
        return "Unknown";
    }

    private int computeParentBehaviourScore(double coveragePct, int overdueDoses, int childCount) {
        int score = (int) Math.round(coveragePct);
        // Penalty: 5 points per overdue dose per child
        int penalty = childCount > 0 ? (overdueDoses / childCount) * 5 : 0;
        return Math.max(0, Math.min(100, score - penalty));
    }

    private String generateChildNarrative(String firstName, double pct, int overdue, int due) {
        if (overdue > 0) {
            return String.format("%s has %d overdue vaccine dose(s) and needs immediate attention. " +
                    "Coverage is currently at %.1f%%. Please visit a health facility as soon as possible.", firstName,
                    overdue, pct);
        }
        if (due > 0) {
            return String.format("%s has %d dose(s) currently due or due soon. " +
                    "Coverage is %.1f%%. Please ensure a health facility visit is scheduled promptly.", firstName, due,
                    pct);
        }
        if (pct >= 100) {
            return String.format("Excellent! %s is fully up-to-date on all scheduled immunizations. " +
                    "No action required at this time.", firstName);
        }
        return String.format("%s is on track with %.1f%% of scheduled doses administered. " +
                "Continue to attend scheduled immunization visits.", firstName, pct);
    }

    private String generateParentNarrative(String name, int childCount, double pct, int overdue) {
        String base = String.format("%s has %d registered child(ren) with an overall " +
                "immunization coverage of %.1f%%.", name, childCount, pct);
        if (overdue > 0) {
            return base + String.format(" There are %d overdue dose(s) across all children. " +
                    "Immediate health facility visits are recommended.", overdue);
        }
        if (pct >= 90) {
            return base
                    + " This parent demonstrates excellent immunization compliance and is an exemplary community member.";
        }
        return base + " Continued engagement with the immunization programme is encouraged.";
    }
}
