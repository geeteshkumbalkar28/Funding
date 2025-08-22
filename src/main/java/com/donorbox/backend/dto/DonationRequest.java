package com.donorbox.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Donation request payload")
public class DonationRequest {
    
    @NotBlank(message = "Donor name is required")
    @Schema(description = "Name of the donor", example = "John Doe")
    private String donorName;
    
    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    @Schema(description = "Email of the donor", example = "john.doe@example.com")
    private String donorEmail;
    
    @Schema(description = "Phone number of the donor", example = "+1234567890")
    private String donorPhone;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Schema(description = "Donation amount", example = "100.00")
    private BigDecimal amount;
    
    @Schema(description = "ID of the cause to donate to", example = "1")
    private Long causeId;
    
    @Schema(description = "Message from the donor", example = "Happy to contribute to this cause!")
    private String message;
    
    @Schema(description = "Currency code", example = "INR")
    @Builder.Default
    private String currency = "INR";
    
    @Schema(description = "Payment method", example = "card")
    private String paymentMethod;
}
