package com.unibuzz.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationResponseDTO {
    private Long id;
    private Long eventId;
    private String studentEmail;
    private boolean attended;
}
