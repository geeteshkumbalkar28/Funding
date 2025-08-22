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
@Schema(description = "Payment verification request payload")
public class PaymentVerificationRequest {
    
    @NotBlank(message = "Order ID is required")
    @Schema(description = "Razorpay Order ID", example = "order_MNqBxQrAzjvICs")
    private String orderId;
    
    @NotBlank(message = "Payment ID is required")
    @Schema(description = "Razorpay Payment ID", example = "pay_MNqCLXvKzjvICs")
    private String paymentId;
    
    @NotBlank(message = "Payment signature is required")
    @Schema(description = "Razorpay Payment Signature", example = "489e7a0b13ba5906eb8bdd75bb78a9f2bb9d6e75b5f9c5b5b5b5b5b5b5b5b5b5")
    private String signature;
    
    @NotBlank(message = "Donor name is required")
    @Schema(description = "Name of the donor", example = "John Doe")
    private String donorName;
    
    @Email(message = "Valid donor email is required")
    @NotBlank(message = "Donor email is required")
    @Schema(description = "Email of the donor", example = "john.doe@example.com")
    private String donorEmail;
    
    @NotNull(message = "Donation amount is required")
    @Positive(message = "Amount must be positive")
    @Schema(description = "Donation amount", example = "100.00")
    private BigDecimal amount;
    
    @Schema(description = "Currency code", example = "INR")
    @Builder.Default
    private String currency = "INR ";
    
    @Schema(description = "Name of the cause donated to", example = "Education for All")
    private String causeName;
    
    @Schema(description = "ID of the cause donated to", example = "1")
    private Long causeId;
    
    @Schema(description = "Donor's phone number", example = "+1234567890")
    private String donorPhone;
    
    @Schema(description = "Message from the donor", example = "Happy to contribute to this cause!")
    private String message;
}
