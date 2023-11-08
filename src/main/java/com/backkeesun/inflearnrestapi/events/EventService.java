package com.backkeesun.inflearnrestapi.events;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;

    public Event createEvent(Event event) {
        event.update();
        return eventRepository.save(event);
    }
    public Page<Event> queryEvents(Pageable pageable){
        return eventRepository.findAll(pageable);
    }

    public Optional<Event> getEvent(Integer id) {
        return eventRepository.findById(id);
    }

    public Event saveEvent(Event event, Integer id) {
        event.update();
        event.setId(id);
        return eventRepository.save(event);
    }
}
