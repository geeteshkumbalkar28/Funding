package com.donorbox.backend.dto;

import com.donorbox.backend.entity.Cause;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
public class CauseRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String shortDescription;

    @NotNull(message = "Target amount is required")
    @PositiveOrZero(message = "Target amount must be positive or zero")
    private BigDecimal targetAmount;

    private String imageUrl;

    private String videoUrl;

    @Builder.Default
    private Cause.MediaType mediaType = Cause.MediaType.IMAGE;

    @Builder.Default
    private Cause.CauseStatus status = Cause.CauseStatus.ACTIVE;

    private String category;

    private String location;

    private LocalDateTime endDate;

    /**
     * Convert CauseRequest to Cause entity
     * @return Cause entity
     */
    public Cause toEntity() {
        return Cause.builder()
                .title(this.title)
                .description(this.description)
                .shortDescription(this.shortDescription)
                .targetAmount(this.targetAmount)
                .imageUrl(this.imageUrl)
                .videoUrl(this.videoUrl)
                .mediaType(this.mediaType != null ? this.mediaType : Cause.MediaType.IMAGE)
                .status(this.status != null ? this.status : Cause.CauseStatus.ACTIVE)
                .category(this.category)
                .location(this.location)
                .endDate(this.endDate)
                .build();
    }

    /**
     * Update an existing Cause entity with values from this request
     * @param cause The existing cause to update
     */
    public void updateEntity(Cause cause) {
        cause.setTitle(this.title);
        cause.setDescription(this.description);
        cause.setShortDescription(this.shortDescription);
        cause.setTargetAmount(this.targetAmount);
        cause.setImageUrl(this.imageUrl);
        cause.setVideoUrl(this.videoUrl);
        cause.setMediaType(this.mediaType != null ? this.mediaType : Cause.MediaType.IMAGE);
        cause.setStatus(this.status != null ? this.status : Cause.CauseStatus.ACTIVE);
        cause.setCategory(this.category);
        cause.setLocation(this.location);
        cause.setEndDate(this.endDate);
    }
}
