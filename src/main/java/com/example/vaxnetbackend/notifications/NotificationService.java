package com.example.vaxnetbackend.notifications;

import com.example.vaxnetbackend.twilio.SMSService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SMSService smsService;
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void register(String email, SseEmitter emitter) {
        emitters.put(email, emitter);
        emitter.onCompletion(() -> emitters.remove(email));
        emitter.onTimeout(() -> emitters.remove(email));
        
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Connected to notification stream for " + email));
        } catch (IOException e) {
            emitters.remove(email);
        }
    }

    public void sendToast(String email, String message, String type) {
        SseEmitter emitter = emitters.get(email);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("toast")
                        .data("{\"message\":\"" + message + "\", \"type\":\"" + type + "\"}"));
            } catch (IOException e) {
                emitters.remove(email);
            }
        }
    }

    public void broadcastSms(List<String> phones, String message) {
        smsService.sendBroadcast(phones, message);
        // Also notify interested admins via toast if needed
        // For now, we just log and send SMS
    }
}
