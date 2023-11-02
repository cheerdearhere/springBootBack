package com.backkeesun.inflearnrestapi.events;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Data // Entity가 아닌 DTO는 Data 사용도 나쁘지않음
@NoArgsConstructor @AllArgsConstructor 
public class EventDto {

    // from Event
    private String name;
    private String description;
    private LocalDateTime beginEnrollmentDateTime; //등록 시간
    private LocalDateTime closeEnrollmentDateTime; //만료 시간
    private LocalDateTime beginEventDateTime; // 시작 시간
    private LocalDateTime endEventDateTime; // 종료 시간
    private String location;
    private int basePrice; // optional
    private int maxPrice; // optional
    private int limitOfEnrollment; //최대 event
}
