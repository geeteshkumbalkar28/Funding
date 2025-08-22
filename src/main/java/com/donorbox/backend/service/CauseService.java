package com.donorbox.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.donorbox.backend.dto.CauseRequest;
import com.donorbox.backend.dto.CauseResponse;
import com.donorbox.backend.repository.*;
import com.donorbox.backend.entity.*;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CauseService {
    private final CauseRepository causeRepository;
    private final ImageUploadService imageUploadService;
    private final MediaUploadService mediaUploadService;
    @Transactional(readOnly = true)
    public List<CauseResponse> getAllCauses() {
        List<Cause> causes = causeRepository.findAll();
        System.out.println("Causes retrieved: " + causes.size());
        return causes.stream()
                .map(CauseResponse::summaryFromEntity)
                .toList();
    }

    // Backward compatibility method
    @Transactional(readOnly = true)
    public List<Cause> getAllCausesEntities() {
        return causeRepository.findAll();
    }

    // Backward compatibility method
    @Transactional(readOnly = true)
    public Cause getCauseEntityById(Long id) {
        return causeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cause not found"));
    }

    @Transactional(readOnly = true)
    public CauseResponse getCauseById(Long id) {
        return CauseResponse.fromEntity(
            causeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cause not found"))
        );
    }

    @Transactional
    public CauseResponse createCause(CauseRequest request) {
        Cause cause = request.toEntity();
        Cause savedCause = causeRepository.save(cause);
        return CauseResponse.fromEntity(savedCause);
    }

    @Transactional
    public CauseResponse updateCause(Long id, CauseRequest request) { 
        Cause cause = causeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Cause not found"));
        request.updateEntity(cause);
        Cause updatedCause = causeRepository.save(cause);
        return CauseResponse.fromEntity(updatedCause);
    }

    @Transactional
    public void deleteCause(Long id) {
        // Get the cause to check if it has media
        Cause cause = causeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Cause not found"));
        
        // Delete associated media if exists
        if (cause.getImageUrl() != null && !cause.getImageUrl().trim().isEmpty()) {
            mediaUploadService.deleteMedia(cause.getImageUrl());
        }
        if (cause.getVideoUrl() != null && !cause.getVideoUrl().trim().isEmpty()) {
            mediaUploadService.deleteMedia(cause.getVideoUrl());
        }

        // Delete the cause from database
        causeRepository.deleteById(id);
    }

    // Backward compatibility methods for direct entity operations
    @Transactional
    public Cause createCause(Cause cause) {
        return causeRepository.save(cause);
    }

    @Transactional
    public Cause updateCause(Long id, Cause cause) {
        if (!causeRepository.existsById(id)) {
            throw new IllegalArgumentException("Cause not found");
        }
        cause.setId(id);
        return causeRepository.save(cause);
    }
}

