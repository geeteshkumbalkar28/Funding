package com.donorbox.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Volunteer registration request payload")
public class VolunteerRequest {

    @NotBlank(message = "First name is required")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "First name must contain only letters and spaces")
    @Schema(description = "First name of the volunteer", example = "John")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Last name must contain only letters and spaces")
    @Schema(description = "Last name of the volunteer", example = "Doe")
    private String lastName;

    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    @Schema(description = "Email of the volunteer", example = "john.doe@example.com")
    private String email;

    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
    @NotBlank(message = "Phone number is required")
    @Schema(description = "Phone number of the volunteer", example = "9876543210")
    private String phone;

    @Schema(description = "Address of the volunteer", example = "123 Main St, City, State")
    private String address;

    @Schema(description = "Skills of the volunteer", example = "Teaching, Event Management")
    private String skills;

    @Schema(description = "Availability of the volunteer", example = "Weekends")
    private String availability;

    @Schema(description = "Previous volunteer experience", example = "2 years at local shelter")
    private String experience;

    @Schema(description = "Motivation for volunteering", example = "Want to help the community")
    private String motivation;
}
