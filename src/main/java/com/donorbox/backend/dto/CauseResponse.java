package com.donorbox.backend.dto;

import com.donorbox.backend.entity.Cause;
import com.donorbox.backend.util.DateTimeUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CauseResponse {

    private Long id;
    private String title;
    private String description;
    private String shortDescription;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private String imageUrl;
    private String videoUrl;
    private Cause.MediaType mediaType;
    private Cause.CauseStatus status;
    private String category;
    private String location;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional computed fields
    private Double progressPercentage;
    private Boolean hasImage;
    private Boolean hasVideo;
    private Boolean isActive;
    private Boolean isCompleted;
    private Long daysRemaining;

    /**
     * Create CauseResponse from Cause entity with all details
     * @param cause The cause entity
     * @return Complete CauseResponse
     */
    public static CauseResponse fromEntity(Cause cause) {
        CauseResponse response = CauseResponse.builder()
                .id(cause.getId())
                .title(cause.getTitle())
                .description(cause.getDescription())
                .shortDescription(cause.getShortDescription())
                .targetAmount(cause.getTargetAmount())
                .currentAmount(cause.getCurrentAmount())
                .imageUrl(cause.getImageUrl())
                .videoUrl(cause.getVideoUrl())
                .mediaType(cause.getMediaType())
                .status(cause.getStatus())
                .category(cause.getCategory())
                .location(cause.getLocation())
                .endDate(cause.getEndDate())
                .createdAt(cause.getCreatedAt())
                .updatedAt(cause.getUpdatedAt())
                .build();

        // Calculate computed fields
        response.setProgressPercentage(calculateProgressPercentage(cause));
        response.setHasImage(cause.getImageUrl() != null && !cause.getImageUrl().trim().isEmpty());
        response.setHasVideo(cause.getVideoUrl() != null && !cause.getVideoUrl().trim().isEmpty());
        response.setIsActive(cause.getStatus() == Cause.CauseStatus.ACTIVE);
        response.setIsCompleted(cause.getStatus() == Cause.CauseStatus.COMPLETED);
        response.setDaysRemaining(calculateDaysRemaining(cause));

        return response;
    }

    /**
     * Create summary CauseResponse from Cause entity (for lists)
     * @param cause The cause entity
     * @return Summary CauseResponse
     */
    public static CauseResponse summaryFromEntity(Cause cause) {
        CauseResponse response = CauseResponse.builder()
                .id(cause.getId())
                .title(cause.getTitle())
                .shortDescription(cause.getShortDescription())
                .targetAmount(cause.getTargetAmount())
                .currentAmount(cause.getCurrentAmount())
                .imageUrl(cause.getImageUrl())
                .videoUrl(cause.getVideoUrl())
                .mediaType(cause.getMediaType())
                .status(cause.getStatus())
                .category(cause.getCategory())
                .location(cause.getLocation())
                .endDate(cause.getEndDate())
                .createdAt(cause.getCreatedAt())
                .build();

        // Calculate computed fields
        response.setProgressPercentage(calculateProgressPercentage(cause));
        response.setHasImage(cause.getImageUrl() != null && !cause.getImageUrl().trim().isEmpty());
        response.setHasVideo(cause.getVideoUrl() != null && !cause.getVideoUrl().trim().isEmpty());
        response.setIsActive(cause.getStatus() == Cause.CauseStatus.ACTIVE);
        response.setIsCompleted(cause.getStatus() == Cause.CauseStatus.COMPLETED);
        response.setDaysRemaining(calculateDaysRemaining(cause));

        return response;
    }

    private static Double calculateProgressPercentage(Cause cause) {
        if (cause.getTargetAmount() == null || cause.getTargetAmount().compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        
        BigDecimal currentAmount = cause.getCurrentAmount() != null ? cause.getCurrentAmount() : BigDecimal.ZERO;
        return currentAmount.divide(cause.getTargetAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    private static Long calculateDaysRemaining(Cause cause) {
        if (cause.getEndDate() == null) {
            return null;
        }
        
        LocalDateTime now = DateTimeUtil.getCurrentKolkataTime();
        if (cause.getEndDate().isBefore(now)) {
            return 0L;
        }
        
        return java.time.Duration.between(now, cause.getEndDate()).toDays();
    }
}
