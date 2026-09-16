package com.unibuzz.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {
    private String fullName;
    private String email;
    private String role;
    private String department;
    private java.util.List<String> interests;
    private String officeLocation;
    private String phoneNumber;
}
