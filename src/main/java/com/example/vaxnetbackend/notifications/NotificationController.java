package com.example.vaxnetbackend.notifications;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final VaccinationReminderService vaccinationReminderService;

    // In-memory SSE emitter registry: email → emitter
    // In production this would use a pub/sub or message broker
    private static final Map<String, SseEmitter> EMITTERS = new ConcurrentHashMap<>();

    /**
     * Admin endpoint: manually trigger the reminder job to test SMS/email delivery
     * without waiting for the scheduled cron.
     */
    @PostMapping("/reminders/trigger")
    public ResponseEntity<String> triggerReminders() {
        String result = vaccinationReminderService.triggerManually();
        return ResponseEntity.ok(result);
    }

    /**
     * Server-Sent Events stream — frontend subscribes to receive in-app toast
     * notifications.
     * Usage: new EventSource('/api/notifications/sse/{email}')
     */
    @GetMapping(value = "/sse/{email}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable String email) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        EMITTERS.put(email, emitter);

        emitter.onCompletion(() -> EMITTERS.remove(email));
        emitter.onTimeout(() -> EMITTERS.remove(email));
        emitter.onError(e -> EMITTERS.remove(email));

        // Send a welcome event so the client knows the connection is live
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("{\"message\":\"VaxNet notification stream connected\"}"));
        } catch (IOException e) {
            EMITTERS.remove(email);
        }

        return emitter;
    }

    /**
     * Push a toast notification to a specific user by email (called internally by
     * services).
     */
    public static void pushToast(String email, String type, String message) {
        SseEmitter emitter = EMITTERS.get(email);
        if (emitter == null)
            return;
        try {
            emitter.send(SseEmitter.event()
                    .name("toast")
                    .data("{\"type\":\"" + type + "\",\"message\":\"" + escapeJson(message) + "\"}"));
        } catch (IOException e) {
            EMITTERS.remove(email);
        }
    }

    private static String escapeJson(String s) {
        return s.replace("\"", "\\\"").replace("\n", "\\n");
    }
}
