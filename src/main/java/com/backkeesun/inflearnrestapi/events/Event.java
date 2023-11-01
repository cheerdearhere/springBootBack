package com.backkeesun.inflearnrestapi.events;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of= "id") //id값을 기준으로만 해당 객체 비교
public class Event {
//    base data
    private Integer id; // Identifier: PK
    private String name;
    private String description;
//    time checkers
    private LocalDateTime beginEnrollmentDateTime; //등록 시간
    private LocalDateTime closeEnrollmentDateTime; //만료 시간
    private LocalDateTime beginEventDateTime; // 시작 시간
    private LocalDateTime endEventDateTime; // 종료 시간
//    optional
    private String location; // optional: null ? online : offline
    /*
        price 로직
        base    max     logic
        0       100     선착순
        0       0       무료
        100     0       무제한 경매
        100     100     선착순 경매(순위꿘)
     */
    private int basePrice; // optional
    private int maxPrice; // optional
//  other data
    private int limitOfEnrollment; //최대 event
    private boolean offline;
    private boolean free;
    private EventStatus eventStatus;
}