package com.backkeesun.inflearnrestapi.index;

import com.backkeesun.inflearnrestapi.events.Event;
import com.backkeesun.inflearnrestapi.events.EventController;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/api")
public class IndexController {
    @GetMapping
    public RepresentationModel index(){
        var index= new RepresentationModel();
        index.add(linkTo(EventController.class).withRel("events"));
        return index;
    }
}
