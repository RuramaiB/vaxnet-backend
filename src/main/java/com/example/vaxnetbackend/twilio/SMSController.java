package com.example.vaxnetbackend.twilio;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sms")
public class SMSController {

    @Autowired
    private SMSService smsService;

    @PostMapping("/send")
    public String sendSms(
            @RequestParam String to,
            @RequestParam String message
    ) {
        String sid = smsService.sendSms(to, message);
        return "📩 SMS sent successfully! SID: " + sid;
    }
}

