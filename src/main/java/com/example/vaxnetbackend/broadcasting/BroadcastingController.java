package com.example.vaxnetbackend.broadcasting;

import com.example.vaxnetbackend.twilio.SMSService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/broadcasting")
@RequiredArgsConstructor
public class BroadcastingController {

    private final SMSService smsService;

    @PostMapping("/send")
    public String sendBroadcast(@RequestBody BroadcastRequest request) {
        smsService.sendBroadcast(request.getPhones(), request.getMessage());
        return "Broadcast initiated for " + request.getPhones().size() + " recipients";
    }

    @Data
    public static class BroadcastRequest {
        private List<String> phones;
        private String message;
    }
}
