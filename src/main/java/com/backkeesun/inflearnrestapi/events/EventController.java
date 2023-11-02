package com.backkeesun.inflearnrestapi.events;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpClientErrorException;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Controller
@RequiredArgsConstructor
@RequestMapping(value="/api/events",produces= MediaTypes.HAL_JSON_VALUE)
public class EventController {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EventValidation eventValidation;

    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto, Errors errors){
        if(errors.hasErrors()){
            return ResponseEntity.badRequest().body(errors);
        }
        eventValidation.validate(eventDto,errors);
        if(errors.hasErrors()){
            return ResponseEntity.badRequest().body(errors);
        }
        Event event = modelMapper.map(eventDto,Event.class);
        Event newEvent = this.eventRepository.save(event);
        URI createdUri = linkTo(EventController.class).slash(newEvent.getId()).toUri();
        return ResponseEntity.created(createdUri).body(event);//.build();
    }
}
