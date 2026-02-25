package com.example.vaxnetbackend.email;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class EmailRequest {
    private String email;
    private String body;
    private String subject;
}