package com.backkeesun.inflearnrestapi.events;

import com.backkeesun.inflearnrestapi.account.Account;
import com.backkeesun.inflearnrestapi.account.AccountAdepter;
import com.backkeesun.inflearnrestapi.account.CustomAccount;
import com.backkeesun.inflearnrestapi.common.ErrorResource;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Controller
@RequiredArgsConstructor
@RequestMapping(value="/api/events",produces= MediaTypes.HAL_JSON_VALUE)
public class EventController {

    private final EventService eventService;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;

    @PostMapping
    public ResponseEntity createEvent(
            @RequestBody @Valid EventDto eventDto,
            Errors errors,
            @CustomAccount Account author
    ){
        if(errors.hasErrors()){
            return badRequest(errors);
        }
        eventValidator.validate(eventDto,errors);
        if(errors.hasErrors()){
            return badRequest(errors);
        }
        Event event = modelMapper.map(eventDto,Event.class);
        event.setAuthor(author);
        Event newEvent = eventService.createEvent(event);
//        ControllerLinkBuilder
        WebMvcLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(newEvent.getId());
        URI createdUri = selfLinkBuilder.toUri();
        EventResource eventResource = new EventResource(event);
        //new Link("url")도 가능:  add(new Link("http://localhost:8181/api/events/"+event.getId()));
//        eventResource.add(linkTo(EventController.class).withRel("query-events"));
//        eventResource.add(selfLinkBuilder.withSelfRel());
//        eventResource.add(selfLinkBuilder.withRel("update-event"));//HttpMethod 차이일뿐 링크는 같을 수 있음
        eventResource.add(Link.of("/docs/index.html#resources-events-create").withRel("profile"));//프로필은 method마다 다름
        return ResponseEntity.created(createdUri).body(eventResource);//.build();
    }
    @GetMapping
    //Pageable로 paging에 필요한 parameter를 받음(Spring data JPA가 제공)
    public ResponseEntity queryEvents(
            Pageable pageable,
            PagedResourcesAssembler<Event> pagedResourcesAssembler,
            @CustomAccount Account currentUser
    ){
//      어노테이션으로 대체  @AuthenticationPrincipal User user
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        User userDetails =(User) authentication.getPrincipal();

        Page<Event> page = this.eventService.queryEvents(pageable);
        PagedModel<EntityModel<Event>> pageEntityModel = pagedResourcesAssembler.toModel(page, EventResource::new);
        pageEntityModel.add(Link.of("/docs/index.html#resources-query-events").withRel("profile"));
        if(currentUser != null){
            pageEntityModel.add(linkTo(EventController.class).withRel("create-event"));
        }
        return ResponseEntity.ok(pageEntityModel);
    }

    @GetMapping("/{id}")
    public ResponseEntity getEvent(
            @PathVariable Integer id,
            @CustomAccount Account currentAccount
    ){
        Optional<Event> optionalEvent = this.eventService.getEvent(id);
        if(optionalEvent.isEmpty()){
            return ResponseEntity.notFound()/*.header("header", String.valueOf(HttpServletResponse.SC_NOT_FOUND))*/.build();
        }
        Event event = optionalEvent.get();
        EventResource eventResource = new EventResource(event);
        eventResource.add(Link.of("/docs/index.html#resources-events-get").withRel("profile"));
        if(event.getAuthor().equals(currentAccount)){
            eventResource.add(linkTo(EventController.class).slash(event.getId()).withRel("update-event"));
        }
        return ResponseEntity.ok().body(eventResource);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateEvent(@PathVariable Integer id,
                                      @RequestBody @Valid EventDto eventDto,
                                      Errors errors,
                                      @CustomAccount Account currentAccount
    ){
    //Original Event data
        Optional<Event> optionalEvent = this.eventService.getEvent(id);
    //Validation
        if(optionalEvent.isEmpty()){
            return notFound(errors);
        }
        if(errors.hasErrors()){
            return badRequest(errors);
        }
        Event originalEvent = optionalEvent.get();
        if(!originalEvent.getAuthor().equals(currentAccount)){
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        eventValidator.validate(eventDto,errors);
        if(errors.hasErrors()){
            return badRequest(errors);
        }
    //Update
        //update내용 생성
        Event event = modelMapper.map(eventDto,Event.class);
        //해당 데이터에 저장
        Event updateEvent = eventService.saveEvent(event, id);
    //Return resources
        EventResource eventResource = new EventResource(updateEvent);
        String locationURI = linkTo(EventController.class).slash(updateEvent.getId()).toUri().toString();
        eventResource.add(Link.of("/docs/index.html#resources-events-update").withRel("profile"));
        return ResponseEntity.ok().header(HttpHeaders.LOCATION,locationURI).body(eventResource);
    }
    private static ResponseEntity<ErrorResource> badRequest(Errors errors) {
        return ResponseEntity.badRequest().body(new ErrorResource(errors));
    }
    private ResponseEntity<ErrorResource> notFound(Errors errors) {
        ErrorResource errorResource = new ErrorResource(errors);
        return new ResponseEntity(errorResource, HttpStatus.NOT_FOUND);
    }
}
