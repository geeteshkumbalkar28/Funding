package com.donorbox.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.donorbox.backend.repository.EventRepository;
import com.donorbox.backend.entity.Event;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final ImageUploadService imageUploadService;

    @Transactional(readOnly = true)
public List<Event> getAllEvents() {
    List<Event> events = eventRepository.findAll();
    System.out.println("Events retrieved: " + events.size());
        return eventRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
    }

    @Transactional
    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    @Transactional
    public Event updateEvent(Long id, Event event) {
        Event existingEvent = getEventById(id);
        existingEvent.setTitle(event.getTitle());
        existingEvent.setDescription(event.getDescription());
        existingEvent.setShortDescription(event.getShortDescription());
        existingEvent.setEventDate(event.getEventDate());
        existingEvent.setLocation(event.getLocation());
        existingEvent.setImageUrl(event.getImageUrl());
        existingEvent.setMaxParticipants(event.getMaxParticipants());
        existingEvent.setStatus(event.getStatus());
        return eventRepository.save(existingEvent);
    }

    @Transactional
    public void deleteEvent(Long id) {
        // Get the event to check if it has an image
        Event event = getEventById(id);
        
        // Delete associated image if exists
        if (event.getImageUrl() != null && !event.getImageUrl().trim().isEmpty()) {
            imageUploadService.deleteImage(event.getImageUrl());
        }
        
        // Delete the event from database
        eventRepository.deleteById(id);
    }
}
