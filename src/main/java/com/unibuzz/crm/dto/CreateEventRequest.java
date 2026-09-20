package com.unibuzz.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateEventRequest {
    private String title;
    private String description;
    private LocalDate eventDate;
    private String eventTime;
    private String location;
    private Integer seats;
    private String category;
}
