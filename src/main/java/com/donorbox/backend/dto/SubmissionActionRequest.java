package com.donorbox.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionActionRequest {
    
    private String adminNotes;
    private String approvedBy;
    
    // For approval - optional cause modifications
    private String modifiedTitle;
    private String modifiedDescription;
    private String modifiedShortDescription;
    private String modifiedCategory;
    private String modifiedLocation;
}
