package com.example.vaxnetbackend.auth;

import com.example.vaxnetbackend.user.Gender;
import com.example.vaxnetbackend.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

  private String firstname;
  private String lastname;
  private String email;
  private String dateOfBirth;
  private Gender gender;
  private String physicalAddress;
  private String phoneNumber;
  private String town;
  private String city;
  private String password;
  private Role role;
  private boolean enabled;
}
