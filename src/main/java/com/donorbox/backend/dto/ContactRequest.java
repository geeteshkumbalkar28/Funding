package com.donorbox.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Contact form request payload")
public class ContactRequest {
    
    @NotBlank(message = "Name is required")
    @Schema(description = "Name of the sender", example = "John Doe")
    private String name;
    
    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    @Schema(description = "Email of the sender", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "Phone number of the sender", example = "+1234567890")
    private String phone;
    
    @NotBlank(message = "Subject is required")
    @Schema(description = "Subject of the message", example = "Inquiry about donations")
    private String subject;
    
    @NotBlank(message = "Message is required")
    @Schema(description = "Content of the message", example = "I would like to know more about your causes")
    private String content;
}
