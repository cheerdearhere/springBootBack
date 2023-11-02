package com.backkeesun.inflearnrestapi.events;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.time.LocalDateTime;

@Component
public class EventValidator {
    public void validate(EventDto eventDto, Errors errors){
        //check price
        if(eventDto.getBasePrice() > eventDto.getMaxPrice() && eventDto.getMaxPrice() > 0){
            errors.rejectValue("basePrice","3011","basePrice can't be larger than maxPrice");
            errors.rejectValue("maxPrice","3011","maxPrice can't be smaller than basePrice and 0");
        }
        //check endEventDateTime
        LocalDateTime endEventDateTime = eventDto.getEndEventDateTime();
        if(endEventDateTime.isBefore(eventDto.getBeginEventDateTime()) ||
                endEventDateTime.isBefore(eventDto.getBeginEnrollmentDateTime()) ||
                endEventDateTime.isBefore(eventDto.getCloseEnrollmentDateTime())
        ){
            errors.rejectValue("endEventDateTime","4011","wrong input value");
//            errors.reject("globalError","just globalError test");

        }
        //check beginEventDateTime
        LocalDateTime beginEventDateTime = eventDto.getBeginEventDateTime();
        if(beginEventDateTime.isAfter(eventDto.getEndEventDateTime()) ||
                beginEventDateTime.isAfter(eventDto.getCloseEnrollmentDateTime()) ||
                beginEventDateTime.isBefore(eventDto.getBeginEnrollmentDateTime())
        ){
            errors.rejectValue("beginEventDateTime","4012","wrong input value");
        }
        //check closeEventEnrollmentDateTime
        LocalDateTime closeEventEnrollmentDateTime = eventDto.getCloseEnrollmentDateTime();
        if(closeEventEnrollmentDateTime.isBefore(eventDto.getBeginEventDateTime())||
                closeEventEnrollmentDateTime.isBefore(eventDto.getBeginEnrollmentDateTime())
        ){
            errors.rejectValue("closeEventEnrollmentDateTime","4013","wrong input value");
        }
    }
}
