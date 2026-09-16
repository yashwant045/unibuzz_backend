package com.unibuzz.crm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Role is required")
    private String role;

    // student fields
    private String enrollmentNumber;
    private String phoneNumber;
    private String department;
    private String section;
    private String year;

    // faculty fields
    private String designation;
    private String expertise;
    private String officeLocation;

    private List<String> interests;
}