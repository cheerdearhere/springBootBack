package com.backkeesun.inflearnrestapi.events;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.time.LocalDateTime;

@Component
public class EventValidation {
    public void validate(EventDto eventDto, Errors errors){
        if(eventDto.getBasePrice() > eventDto.getMaxPrice() && eventDto.getMaxPrice() > 0){
            errors.rejectValue("basePrice","wrong input value","basePrice can't be larger than maxPrice");
            errors.rejectValue("maxPrice","wrong input value","maxPrice can't be smaller than basePrice and 0");
        }
        LocalDateTime endEventDateTime = eventDto.getEndEventDateTime();
        if(endEventDateTime.isBefore(eventDto.getBeginEventDateTime()) ||
        endEventDateTime.isBefore(eventDto.getBeginEnrollmentDateTime()) ||
        endEventDateTime.isBefore(eventDto.getCloseEnrollmentDateTime())
        ){
            errors.rejectValue("endEventDateTime","wrong input value","endEventDateTime must be before other dateTimes");
            errors.reject("globalError");
        }
        //TODO beginEventDateTime
        //TODO closeEventEnrollmentDateTime
    }
}
