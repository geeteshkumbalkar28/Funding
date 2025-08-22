package com.donorbox.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.donorbox.backend.util.DateTimeUtil;

@Entity
@Table(name = "donations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Donor name is required")
    @Column(name = "donor_name", nullable = false)
    private String donorName;

    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    @Column(name = "donor_email", nullable = false)
    private String donorEmail;

    @Column(name = "donor_phone")
    private String donorPhone;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "cause_id")
    private Cause cause;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "currency")
    private String currency;

    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "order_id")
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private DonationStatus status = DonationStatus.PENDING;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "followup_email_count", nullable = true)
    @Builder.Default
    private Integer followupEmailCount = 0;

    @PrePersist
    protected void onCreate() {
        createdAt = DateTimeUtil.getCurrentTimeForDatabase();
        updatedAt = DateTimeUtil.getCurrentTimeForDatabase();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = DateTimeUtil.getCurrentTimeForDatabase();
    }

    public enum DonationStatus {
        PENDING, COMPLETED, FAILED, REFUNDED
    }
}
