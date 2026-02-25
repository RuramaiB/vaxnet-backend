package com.example.vaxnetbackend.twilio;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SMSService {

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;

    @Value("${sms.simulation.enabled:false}")
    private boolean simulationEnabled;

    @Value("${sms.simulation.receiver:+263771234567}")
    private String simulationReceiver;

    public String sendSms(String toPhoneNumber, String messageBody) {
        String recipient = simulationEnabled ? simulationReceiver : toPhoneNumber;

        System.out.println("Sending SMS to: " + recipient + (simulationEnabled ? " (SIMULATION MODE)" : ""));

        Message message = Message.creator(
                new PhoneNumber(recipient), // Receiver’s number
                new PhoneNumber(fromPhoneNumber), // Sender’s Twilio number
                messageBody // Message content
        ).create();

        return message.getSid(); // returns Twilio Message SID
    }

    public void sendBroadcast(List<String> toPhoneNumbers, String messageBody) {
        for (String phone : toPhoneNumbers) {
            try {
                sendSms(phone, messageBody);
            } catch (Exception e) {
                System.err.println("Failed to send SMS to " + phone + ": " + e.getMessage());
            }
        }
    }
}
