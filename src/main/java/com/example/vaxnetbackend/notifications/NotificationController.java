package com.example.vaxnetbackend.notifications;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/sse/{email}")
    public SseEmitter subscribe(@PathVariable("email") String email) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        notificationService.register(email, emitter);
        return emitter;
    }
}
