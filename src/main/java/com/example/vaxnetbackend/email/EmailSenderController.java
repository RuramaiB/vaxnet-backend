package com.example.vaxnetbackend.email;



import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/emailing")
@RequiredArgsConstructor
public class EmailSenderController {


    private final EmailSenderService emailSenderService;


//    @EventListener(ApplicationReadyEvent.class)
    @PostMapping("sendEmail")
    public void sendMail(@RequestBody EmailRequest emailRequest){
        emailSenderService.sendEmail(
                emailRequest.getEmail(),
                emailRequest.getSubject(),
                emailRequest.getBody()
        );
    }

    @PostMapping("sendEmailForAll")
    public void sendMailForAll (@RequestBody EmailRequest emailRequest){

            emailSenderService.sendEmailToAll(
                    emailRequest.getSubject(),
                    emailRequest.getBody()
            );
    }

}
