package com.example.vaxnetbackend.auth;


import com.example.vaxnetbackend.config.JwtService;
import com.example.vaxnetbackend.exception.ResourceNotFoundException;
import com.example.vaxnetbackend.token.Token;
import com.example.vaxnetbackend.token.TokenRepository;
import com.example.vaxnetbackend.token.TokenState;
import com.example.vaxnetbackend.token.TokenType;
import com.example.vaxnetbackend.user.Role;
import com.example.vaxnetbackend.user.User;
import com.example.vaxnetbackend.user.UserRepository;
import com.example.vaxnetbackend.verification.Verification;
import com.example.vaxnetbackend.verification.VerificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
  private final UserRepository repository;
  private final TokenRepository tokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final VerificationRepository verificationRepository;
    private final UserRepository userRepository;

    public AuthenticationResponse register(RegisterRequest request) {
      if (existsByEmail(request.getEmail())) {
        System.out.println("User already exists.");
        return AuthenticationResponse.builder().message("User already exists").build();
      } else {
        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .physicalAddress(request.getPhysicalAddress())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(true)
                .build();
        var savedUser = repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, jwtToken);
        return AuthenticationResponse.builder()
                .role(user.getRole())
                .email(user.getEmail())
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .message("Account created successfully")
                .build();
      }
  }
  public String generateVerificationCode(String studentEmail) {
    User user = repository.findByEmail(studentEmail)
            .orElseThrow(()-> new ResourceNotFoundException("User not found"));
    int [] digits = new int[6];
    String code = "";
      for (int i = 0; i < 6; i++) {
        digits[i] = (int)(Math.random()* 10);
    }
    for (int count: digits) code += count;
//    Verification ver= verificationRepository.findByEmail(studentEmail)
//                    .orElse(new Verification());
//    verificationRepository.delete(verificationRepository.findByEmail(studentEmail).orElseThrow(()-> new ResourceNotFoundException("Verification not found.")));
    Verification verification = new Verification();
    verification.setEmail(user.getEmail());
    verification.setVerificationCode(code);
    verificationRepository.insert(verification);
    return code;
  }
  public ResponseEntity<String> verifyCode(String email, String code){
    Verification verification= verificationRepository.findByEmailAndVerificationCode(email, code)
            .orElseThrow(() -> new ResourceNotFoundException("Verification details not found"));
    if (Objects.equals(verification.getVerificationCode(), code)){
      User user = repository.findByEmail(email)
              .orElseThrow(()-> new ResourceNotFoundException("User not found"));
      Token tkn = tokenRepository.findByUserAndTokenState(user, TokenState.fresh)
              .orElseThrow(() -> new ResourceNotFoundException("Token not found"));
      tkn.setRevoked(false);
     tokenRepository.save(tkn);
//     user.setIsVerified(true);
//     user.setIsFirstTime(false);
     repository.save(user);
     return ResponseEntity.ok("Verification successful.");
    }
    else{
      return ResponseEntity.ok("Verification failed.");
    }
  }
  public AuthenticationResponse authenticate(AuthenticationRequest request) {
      authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                      request.getEmail(),
                      request.getPassword()
              )
      );
      var user = repository.findByEmail(request.getEmail())
              .orElseThrow();
      var jwtToken = jwtService.generateToken(user);
      var userRole = user.getRole();
      var email = user.getEmail();
      var refreshToken = jwtService.generateRefreshToken(user);
      saveUserToken(user, jwtToken);
      Token tkn = tokenRepository.findByUserAndTokenState(user, TokenState.fresh)
              .orElseThrow(() -> new ResourceNotFoundException("Token not found"));
      tkn.setRevoked(false);
      tokenRepository.save(tkn);

      return AuthenticationResponse.builder()
              .role(userRole)
              .email(email)
              .accessToken(jwtToken)
              .message("Login successful.")
//              .isFirstTime(user.getIsFirstTime())
//              .isVerified(user.getIsVerified())
//              .hasTwoFactor(user.getHasTwoFactor())
              .refreshToken(refreshToken)
              .build();

  }
  public AuthenticationResponse authenticateWithVerification(AuthenticationRequest request) {
      authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                      request.getEmail(),
                      request.getPassword()
              )
      );
      var user = repository.findByEmail(request.getEmail())
              .orElseThrow();
      var jwtToken = jwtService.generateToken(user);
      var userRole = user.getRole();
      var email = user.getEmail();
      var refreshToken = jwtService.generateRefreshToken(user);
//        String code = generateVerificationCode(user.getEmail());
//        System.out.println("Verification code: " + code);
      saveUserToken(user, jwtToken);
      return AuthenticationResponse.builder()
              .role(userRole)
              .email(email)
              .accessToken(jwtToken)
              .message("Account verification required (SMS support pending)")
              .refreshToken(refreshToken)
              .build();
  }
  private void saveUserToken(User user, String jwtToken) {
    List<Token> tokens = tokenRepository.findByUser(user);
    for (Token t: tokens) {
      t.setTokenState(TokenState.old);
      tokenRepository.save(t);
    }
    var token = Token.builder()
            .user(user)
            .token(jwtToken)
            .tokenType(TokenType.BEARER)
            .expired(false)
            .revoked(true)
            .tokenState(TokenState.fresh)
            .build();
    tokenRepository.save(token);
  }
  private void revokeAllUserTokens(User user) {
    var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
    if (validUserTokens.isEmpty())
      return;
    validUserTokens.forEach(token -> {
      token.setExpired(true);
      token.setRevoked(true);
    });
    tokenRepository.saveAll(validUserTokens);
  }

  public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    final String refreshToken;
    final String userEmail;
    if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
      return;
    }
    refreshToken = authHeader.substring(7);
    userEmail = jwtService.extractUsername(refreshToken);
    if (userEmail != null) {
      var user = this.repository.findByEmail(userEmail)
              .orElseThrow();
      if (jwtService.isTokenValid(refreshToken, user)) {
        var accessToken = jwtService.generateToken(user);
//        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);
        var authResponse = AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
      }
    }
  }

  public boolean existsByEmail(String email) {
    return repository.existsByEmail(email);
  }


  public User getUserByEmail(String email){
      return repository.findByEmail(email)
            .orElseThrow(()-> new ResourceNotFoundException("User not found"));
  }
  public List<User> getAllUsersByRole(Role role){
      return userRepository.findAllByRole(role);
  }


    public List<User> getAllUsers(){
        return repository.findAll();
    }
    public ResponseEntity<AuthenticationResponse> updateUserRole(String email, Role role){
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setRole(role);
        repository.save(user);
        return ResponseEntity.ok(AuthenticationResponse
                .builder()
                        .message("User role updated successfully")
                .build());
    }
}

