package com.example.vaxnetbackend.email;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final JavaMailSender mailSender;

    public void sendEmail(String toEmail, String subject, String body){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("fadzainherera9@gmail.com");
        message.setTo(toEmail);
        message.setText(body);
        message.setSubject(subject);

        mailSender.send(message);
        System.out.println("Mail sent sucessfully.");
    }
    public void sendEmailToAll(String subject, String body){
        List<String> emails = Arrays.asList("h210218q@hit.ac.zw", "saghnashproductions@gmail.com");
        SimpleMailMessage message = new SimpleMailMessage();
        for(String mail: emails){
            message.setTo(mail);
            message.setFrom("fadzainherera9@gmail.com");
            message.setText(body);
            message.setSubject(subject);
            mailSender.send(message);
        }
        mailSender.send(message);
    }
    public void broadcastToEmails(String subject, String body, List<String> emails){
        SimpleMailMessage message = new SimpleMailMessage();
        for(String mail: emails){
            message.setTo(mail);
            message.setFrom("fadzainherera9@gmail.com");
            message.setText(body);
            message.setSubject(subject);
            mailSender.send(message);
        }
        mailSender.send(message);
    }
}
