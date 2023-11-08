package com.backkeesun.inflearnrestapi.events;

import com.backkeesun.inflearnrestapi.account.Account;
import lombok.*;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of= "id") //id값을 기준으로만 해당 객체 비교
@Entity // for JPA
public class Event {
//    base data
    @Id @GeneratedValue
    @Column(name="event_id")
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

    @Enumerated(EnumType.STRING)
    private EventStatus eventStatus = EventStatus.DRAFT;//초기값 설정ㄷ

    //author를 확인하기위한 단방향 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    private Account author;

    /**
     * 데이터 변경에 따라 연산 데이터를 변경
     */
    public void update(){
        // price update
        this.free = this.basePrice == 0 && this.maxPrice == 0;
        // location update
        this.offline = !(this.location == null || this.location.isBlank());
    }
}
