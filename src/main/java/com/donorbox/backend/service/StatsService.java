package com.donorbox.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.donorbox.backend.repository.*;
import com.donorbox.backend.entity.*;
import com.donorbox.backend.dto.*;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final DonationRepository donationRepository;
    private final CauseRepository causeRepository;
    private final EventRepository eventRepository;
    private final VolunteerRepository volunteerRepository;

    @Transactional(readOnly = true)
    public HomepageStatsResponse getHomepageStats() {
        // Calculate total donations
        BigDecimal totalDonations = donationRepository.findAll()
                .stream()
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Count various entities
        long totalCauses = causeRepository.count();
        long activeCauses = causeRepository.findAll()
                .stream()
                .filter(cause -> cause.getStatus() == Cause.CauseStatus.ACTIVE)
                .count();

        long totalEvents = eventRepository.count();
        long upcomingEvents = eventRepository.findAll()
                .stream()
                .filter(event -> event.getStatus() == Event.EventStatus.UPCOMING)
                .count();

        long totalVolunteers = volunteerRepository.count();
        long approvedVolunteers = volunteerRepository.findAll()
                .stream()
                .filter(volunteer -> volunteer.getStatus() == Volunteer.VolunteerStatus.APPROVED)
                .count();

        long donationCount = donationRepository.count();

        return HomepageStatsResponse.builder()
                .totalDonations(totalDonations)
                .totalCauses(totalCauses)
                .activeCauses(activeCauses)
                .totalEvents(totalEvents)
                .upcomingEvents(upcomingEvents)
                .totalVolunteers(totalVolunteers)
                .approvedVolunteers(approvedVolunteers)
                .donationCount(donationCount)
                .build();
    }
}
