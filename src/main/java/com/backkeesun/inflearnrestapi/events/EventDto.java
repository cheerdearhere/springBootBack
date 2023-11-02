package com.backkeesun.inflearnrestapi.events;

import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Builder
@Data // Entity가 아닌 DTO는 Data 사용도 나쁘지않음
@NoArgsConstructor @AllArgsConstructor 
public class EventDto {

    // from Event
    @NotEmpty
    private String name;
    @NotEmpty
    private String description;
    @NotNull
    private LocalDateTime beginEnrollmentDateTime; //등록 시간
    @NotNull
    private LocalDateTime closeEnrollmentDateTime; //만료 시간
    @NotNull
    private LocalDateTime beginEventDateTime; // 시작 시간
    @NotNull
    private LocalDateTime endEventDateTime; // 종료 시간
    private String location; //optional if null ? offline
    @Min(0)
    private int basePrice; // optional
    @Min(0)
    private int maxPrice; // optional
    @Min(0)
    private int limitOfEnrollment; //최대 event
}
