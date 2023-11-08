package com.backkeesun.inflearnrestapi.events;


import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.util.Arrays;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class EventResource extends EntityModel<Event> {
    public EventResource(Event event, Link... links){
        super(event, Arrays.asList(links));
        WebMvcLinkBuilder resourceBuilder = linkTo(EventController.class).slash(event.getId());
        //new Link("url")도 가능:         add(Link.of("http://localhost:8181/api/events/"+event.getId()));
        //데이터가 변경되면 하나하나 직접 변경해야흐므로 아래를 권장
        add(resourceBuilder.withSelfRel());
        add(resourceBuilder.withRel("query-events"));
//        add(linkTo(methodOn(EventController.class).queryEvents()).withRel("query-events")); 해당 링크의 프로필은 x
        add(resourceBuilder.withRel("update-event"));
    }
}
/**
 * 1번 방법: RepresentationModel 사용
 */
//import com.fasterxml.jackson.annotation.JsonUnwrapped;
//import org.springframework.hateoas.RepresentationModel;
//public class EventResource extends RepresentationModel {
//    @JsonUnwrapped
//    private Event event;
//    public EventResource (Event event){
//        this.event = event;
//    }
//    public Event getEvent() {
//        return event;
//    }
//}
