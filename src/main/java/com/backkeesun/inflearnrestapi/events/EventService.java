package com.backkeesun.inflearnrestapi.events;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;

    public Event createEvent(Event event) {
        event.update();
        return eventRepository.save(event);

    }
}
