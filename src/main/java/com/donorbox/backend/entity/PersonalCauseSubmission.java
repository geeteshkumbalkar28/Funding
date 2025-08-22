package com.donorbox.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import com.donorbox.backend.util.DateTimeUtil;

@Entity
@Table(name = "personal_cause_submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString
public class PersonalCauseSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Column(name = "title", nullable = false)
    private String title;

    @NotBlank(message = "Description is required")
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "short_description")
    private String shortDescription;

    @NotNull(message = "Target amount is required")
    @PositiveOrZero(message = "Target amount must be positive or zero")
    @Column(name = "target_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "video_url")
    private String videoUrl;

    @ElementCollection
    @CollectionTable(name = "personal_cause_submission_images", joinColumns = @JoinColumn(name = "submission_id"))
    @Column(name = "image_url")
    private List<String> imageUrls;

    @ElementCollection
    @CollectionTable(name = "personal_cause_submission_videos", joinColumns = @JoinColumn(name = "submission_id"))
    @Column(name = "video_url")
    private List<String> videoUrls;

    @Column(name = "proof_document_url")
    private String proofDocumentUrl;

    @ElementCollection
    @CollectionTable(name = "personal_cause_submission_documents", joinColumns = @JoinColumn(name = "submission_id"))
    @Column(name = "document_url")
    private List<String> proofDocumentUrls;

    @Column(name = "proof_document_name")
    private String proofDocumentName;

    @Column(name = "proof_document_type")
    private String proofDocumentType;

    @Column(name = "category")
    private String category;

    @Column(name = "location")
    private String location;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    // Submitter information
    @NotBlank(message = "Submitter name is required")
    @Column(name = "submitter_name", nullable = false)
    private String submitterName;

    @NotBlank(message = "Submitter email is required")
    @Email(message = "Valid email is required")
    @Column(name = "submitter_email", nullable = false)
    private String submitterEmail;

    @Column(name = "submitter_phone")
    private String submitterPhone;

    @Column(name = "submitter_message", columnDefinition = "TEXT")
    private String submitterMessage;

    // Approval workflow
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.PENDING;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    // Reference to created cause (if approved)
    @Column(name = "cause_id")
    private Long causeId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = DateTimeUtil.getCurrentTimeForDatabase();
        updatedAt = DateTimeUtil.getCurrentTimeForDatabase();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = DateTimeUtil.getCurrentTimeForDatabase();
    }

    public enum SubmissionStatus {
        PENDING,      // Awaiting admin review
        APPROVED,     // Approved and converted to cause
        REJECTED,     // Rejected by admin
        UNDER_REVIEW  // Currently being reviewed
    }
}
