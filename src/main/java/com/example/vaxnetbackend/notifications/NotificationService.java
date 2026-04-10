package com.example.vaxnetbackend.notifications;

import com.example.vaxnetbackend.twilio.SMSService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SMSService smsService;

    public void broadcastSms(List<String> phones, String message) {
        smsService.sendBroadcast(phones, message);
    }
}
