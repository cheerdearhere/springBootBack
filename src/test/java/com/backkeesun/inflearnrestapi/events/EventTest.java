package com.backkeesun.inflearnrestapi.events;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

//단순 단위 테스트는 속도를 위해 제외
//@Transactional
//@SpringBootTest
class EventTest {
    /**
     * builder 사용 체크
     */
    @Test
    void builder(){
        //given
        //when
        Event event = Event.builder()
                .name("Inflearn Spring Boot")
                .description("REST API development with Spring boot")
                .build();
        //then
        assertThat(event).isNotNull();
    }

    /**
     * java Bean 생성을 위한 생성자, getter, setter 체크
     */
    @Test
    void javaBean(){
        //given
        Integer id = 12345;
        String name = "Bean Event";
        String description = "Bean Event Description";
        //when
        Event event = new Event();
        event.setId(id);
        event.setName(name);
        event.setDescription(description);
        //then
        assertThat(event.getName()).isEqualTo(name);
        assertThat(event.getDescription()).isEqualTo(description);
        assertThat(event.getId()).isEqualTo(id);
    }
}