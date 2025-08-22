package com.donorbox.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalCauseSubmissionRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String shortDescription;

    @NotNull(message = "Target amount is required")
    @PositiveOrZero(message = "Target amount must be positive or zero")
    private BigDecimal targetAmount;

    private String category;
    private String location;
    private LocalDateTime endDate;

    // Submitter information
    @NotBlank(message = "Submitter name is required")
    private String submitterName;

    @NotBlank(message = "Submitter email is required")
    @Email(message = "Valid email is required")
    private String submitterEmail;

    private String submitterPhone;
    private String submitterMessage;
}
