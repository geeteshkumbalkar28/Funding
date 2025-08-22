package com.donorbox.backend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String shortDescription;

    @NotNull(message = "Event date is required")
    @Future(message = "Event date must be in the future")
    private LocalDateTime eventDate;

    private String location;

    private String imageUrl;

    @NotNull(message = "Status is required")
    private String status; // Expecting values like "UPCOMING", "ONGOING", etc.

    @Min(value = 1, message = "Max participants must be at least 1")
    @Max(value = 10000, message = "Max participants must be less than 10,000")
    private Integer maxParticipants;
}
