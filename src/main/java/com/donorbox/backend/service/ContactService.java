package com.donorbox.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.donorbox.backend.repository.*;
import com.donorbox.backend.entity.*;
import com.donorbox.backend.dto.*;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContactService {
    private final MessageRepository messageRepository;
    private final EmailService emailService;

    @Value("${admin.email:testing@alphaseam.com}")
    private String adminEmail;

    @Transactional
    public Message sendMessage(ContactRequest request) {
        Message message = Message.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .subject(request.getSubject())
                .content(request.getContent())
                .build();

        Message savedMessage = messageRepository.save(message);
        emailService.sendContactNotificationEmails(
            message.getName(),
            message.getEmail(),
            message.getPhone(),
            message.getSubject(),
            message.getContent(),
            adminEmail);
        return savedMessage;
    }
}
