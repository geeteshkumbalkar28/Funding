package com.donorbox.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import com.donorbox.backend.util.DateTimeUtil;

@Entity
@Table(name = "causes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = "donations")
public class Cause {

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

    @Column(name = "current_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal currentAmount = BigDecimal.ZERO;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "video_url")
    private String videoUrl;

    @ElementCollection
    @CollectionTable(name = "cause_images", joinColumns = @JoinColumn(name = "cause_id"))
    @Column(name = "image_url")
    private List<String> imageUrls;

    @ElementCollection
    @CollectionTable(name = "cause_videos", joinColumns = @JoinColumn(name = "cause_id"))
    @Column(name = "video_url")
    private List<String> videoUrls;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type")
    @Builder.Default
    private MediaType mediaType = MediaType.IMAGE;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private CauseStatus status = CauseStatus.ACTIVE;

    @Column(name = "category")
    private String category;

    @Column(name = "location")
    private String location;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "cause", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Donation> donations;

    @PrePersist
    protected void onCreate() {
        createdAt = DateTimeUtil.getCurrentTimeForDatabase();
        updatedAt = DateTimeUtil.getCurrentTimeForDatabase();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = DateTimeUtil.getCurrentTimeForDatabase();
    }

    public enum CauseStatus {
        ACTIVE, INACTIVE, COMPLETED, SUSPENDED
    }

    public enum MediaType {
        IMAGE, VIDEO, BOTH, NONE
    }
}
