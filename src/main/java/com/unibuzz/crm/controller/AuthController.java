package com.unibuzz.crm.controller;

import com.unibuzz.crm.dto.LoginRequest;
import com.unibuzz.crm.dto.RegisterRequest;
import com.unibuzz.crm.dto.ResetPasswordRequest;
import com.unibuzz.crm.dto.VerifyOtpRequest;
import com.unibuzz.crm.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthService authService;

    private String getBindingErrorMessage(BindingResult result) {
        if (result.hasErrors()) {
            return result.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .findFirst()
                    .orElse("Invalid request payload");
        }
        return null;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, BindingResult result) {
        String validationError = getBindingErrorMessage(result);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }
        try {
            String message = authService.register(request);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request, BindingResult result) {
        String validationError = getBindingErrorMessage(result);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }
        try {
            String message = authService.verifyOtp(request.getEmail(), request.getOtp());
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body("Email is required");
            }
            String message = authService.resendOtp(email);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body("Email is required");
            }
            String message = authService.forgotPassword(email);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request, BindingResult result) {
        String validationError = getBindingErrorMessage(result);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }
        try {
            String message = authService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, BindingResult result) {
        String validationError = getBindingErrorMessage(result);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }
        try {
            String token = authService.login(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(token);
        } catch (RuntimeException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}