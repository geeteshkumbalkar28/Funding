package com.donorbox.backend.dto;

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
@Schema(description = "Homepage statistics response")
public class HomepageStatsResponse {
    
    @Schema(description = "Total amount donated", example = "50000.00")
    private BigDecimal totalDonations;
    
    @Schema(description = "Total number of causes", example = "15")
    private Long totalCauses;
    
    @Schema(description = "Total number of active causes", example = "8")
    private Long activeCauses;
    
    @Schema(description = "Total number of events", example = "12")
    private Long totalEvents;
    
    @Schema(description = "Total number of upcoming events", example = "5")
    private Long upcomingEvents;
    
    @Schema(description = "Total number of registered volunteers", example = "150")
    private Long totalVolunteers;
    
    @Schema(description = "Total number of approved volunteers", example = "120")
    private Long approvedVolunteers;
    
    @Schema(description = "Total number of donations made", example = "500")
    private Long donationCount;
}
