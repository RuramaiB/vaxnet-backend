package com.example.vaxnetbackend.notifications;

import com.example.vaxnetbackend.appointments.Appointment;
import com.example.vaxnetbackend.appointments.AppointmentRepository;
import com.example.vaxnetbackend.children.Child;
import com.example.vaxnetbackend.immunization.ImmunizationRecord;
import com.example.vaxnetbackend.immunization.ImmunizationRecordRepository;
import com.example.vaxnetbackend.immunization.VaccineDose;
import com.example.vaxnetbackend.immunization.VaccineStatus;
import com.example.vaxnetbackend.twilio.SMSService;
import com.example.vaxnetbackend.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Runs daily at 08:00 and:
 * 1. Finds every child with a vaccine due in exactly 7 days → auto-creates an Appointment.
 * 2. Sends an SMS to the parent (via Twilio) if they have a phone number.
 * 3. Sends same-day reminders for doses due today via SMS.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VaccinationReminderService {

    private final ImmunizationRecordRepository immunizationRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final SMSService smsService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @Scheduled(cron = "0 0 8 * * *")
    public void runDailyReminders() {
        log.info("VaccinationReminderService: Running daily reminder job...");
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysFromNow = today.plusDays(7);

        List<ImmunizationRecord> records = immunizationRecordRepository.findAll();

        for (ImmunizationRecord record : records) {
            Child child = record.getChild();
            if (child == null) continue;

            User parent = child.getParent();
            if (parent == null) continue;

            for (VaccineDose dose : record.getDoses()) {
                if (dose.getStatus() == VaccineStatus.ADMINISTERED) continue;
                if (dose.getScheduledDate() == null) continue;

                try {
                    LocalDate scheduledDate = LocalDate.parse(dose.getScheduledDate());

                    // ── 7-day advance: auto-book appointment + notify ──
                    if (scheduledDate.equals(sevenDaysFromNow)) {
                        autoBookAppointment(child, dose, parent);
                        sendReminder(parent, child, dose, scheduledDate, "7-day advance");
                    }

                    // ── Same-day reminder ──
                    else if (scheduledDate.equals(today)) {
                        sendReminder(parent, child, dose, scheduledDate, "same-day");
                    }

                } catch (Exception e) {
                    log.warn("Failed to process reminder for child {} dose {}: {}", 
                        child.getBirthCertificateNumber(), dose.getVaccineKey(), e.getMessage());
                }
            }
        }
    }

    public void autoBookAppointment(Child child, VaccineDose dose, User parent) {
        String reason = "Scheduled Immunization – " + dose.getVaccineName();

        boolean alreadyExists = appointmentRepository.findAllByChild(child).stream()
                .anyMatch(a -> a.getDateOfAppointment().equals(dose.getScheduledDate())
                        && a.getReasonForAppointment().equals(reason));

        if (alreadyExists) return;

        Appointment appointment = new Appointment();
        appointment.setAppointmentID("AUTO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        appointment.setDateOfAppointment(dose.getScheduledDate());
        appointment.setTimeOfAppointment("08:00");
        appointment.setChild(child);
        appointment.setReasonForAppointment(reason);
        appointment.setAppointmentStatus("pending");
        appointmentRepository.save(appointment);

        log.info("Auto-booked appointment for {} – {}", child.getBirthCertificateNumber(), dose.getVaccineName());
    }

    private void sendReminder(User parent, Child child, VaccineDose dose, LocalDate scheduledDate, String reminderType) {
        String childName = child.getFirstName() + " " + child.getLastName();
        String formattedDate = scheduledDate.format(DATE_FMT);
        String vaccineName = dose.getVaccineName();

        String smsBody = String.format(
                "VaxNet Reminder: %s's vaccination (%s) is due on %s. " +
                "Please visit your nearest health facility. – Zimbabwe VaxNet",
                childName, vaccineName, formattedDate);

        // Send SMS
        if (parent.getPhoneNumber() != null && !parent.getPhoneNumber().isBlank()) {
            try {
                smsService.sendSms(parent.getPhoneNumber(), smsBody);
                log.info("SMS sent to {} for {} reminder ({})", parent.getPhoneNumber(), vaccineName, reminderType);
            } catch (Exception e) {
                log.warn("SMS failed for {}: {}", parent.getPhoneNumber(), e.getMessage());
            }
        }
    }

    public String triggerManually() {
        runDailyReminders();
        return "Reminder job executed successfully at " + LocalDate.now();
    }
}
