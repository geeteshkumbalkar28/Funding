package com.donorbox.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.donorbox.backend.repository.*;
import com.donorbox.backend.entity.*;
import com.donorbox.backend.dto.*;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VolunteerService {
    private final VolunteerRepository volunteerRepository;
    private final EmailService emailService;

    @Value("${admin.email:testing@alphaseam.com}")
    private String adminEmail;

    @Transactional
    public Volunteer registerVolunteer(VolunteerRequest request) {
        Volunteer volunteer = Volunteer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .skills(request.getSkills())
                .availability(request.getAvailability())
                .experience(request.getExperience())
                .motivation(request.getMotivation())
                .build();

        Volunteer savedVolunteer = volunteerRepository.save(volunteer);
        emailService.sendVolunteerNotificationEmails(
            volunteer.getFirstName(),
            volunteer.getLastName(),
            volunteer.getEmail(),
            volunteer.getPhone(),
            volunteer.getSkills(),
            volunteer.getAvailability(),
            volunteer.getExperience(),
            volunteer.getMotivation(),
            adminEmail);
        return savedVolunteer;
    }

    @Transactional(readOnly = true)
    public List<Volunteer> getAllVolunteers() {
        return volunteerRepository.findAll();
    }
}
