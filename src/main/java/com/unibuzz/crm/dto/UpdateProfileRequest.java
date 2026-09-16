package com.unibuzz.crm.dto;

import lombok.Data;
import java.util.List;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String department;
    private List<String> interests;
    private String officeLocation;
    private String phoneNumber;
}
