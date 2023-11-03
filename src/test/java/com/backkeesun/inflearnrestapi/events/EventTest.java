package com.backkeesun.inflearnrestapi.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

//단순 단위 테스트는 속도를 위해 제외
//@Transactional
//@SpringBootTest
class EventTest {
    /**
     * builder 사용 체크
     */
    @Test
    @DisplayName(value = "Entity 조형")
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
    @DisplayName(value="bean 생성 확인")
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

    @Test
    @DisplayName(value = "무료 확인")
    void testFree(){
        //given
        Event event = Event.builder()
                .basePrice(0)
                .maxPrice(0)
                .build();
        //when
        event.update();
        //then
        assertThat(event.isFree()).isTrue();
    }
    @DisplayName(value = "basePrice가 있는 경우")
    @Test
    void testFree_isBase(){
        //given
        Event event = Event.builder()
                .basePrice(20)
                .maxPrice(0)
                .build();
        //when
        event.update();
        //then
        assertThat(event.isFree()).isFalse();
    }
    @Test
    @DisplayName(value="maxPrice가 있는 경우")
    void testFree_isMax(){
        //given
        Event event = Event.builder()
                .basePrice(0)
                .maxPrice(10)
                .build();
        //when
        event.update();
        //then
        assertThat(event.isFree()).isFalse();
    }

    @Test
    @DisplayName(value = "location이 없는 경우")
    void testOffline_empty(){
        //given
        Event event = Event.builder().build();
        //when
        event.update();
        //then
        assertThat(event.isOffline()).isFalse();
    }

    @Test
    @DisplayName(value = "location이 입력된 경우")
    void testOffline(){
        //given
        Event event = Event.builder()
                .location("summit address")
                .build();
        //when
        event.update();
        //then
        assertThat(event.isOffline()).isTrue();
    }


    @ParameterizedTest
    @MethodSource("testFree_useParams")
    @DisplayName(value = "free: parameters 테스트")
    void paramsForFree(int basePrice, int maxPrice, boolean isFree){
        // given
        Event event = Event.builder()
                .basePrice(basePrice)
                .maxPrice(maxPrice)
                .build();

        // when
        event.update();

        // then
        assertThat(event.isFree()).isEqualTo(isFree);
    }
    @ParameterizedTest
    @MethodSource("testOffline_useParams")
    @DisplayName(value = "offline: parameters 테스트")
    void paramsForOffline(String location, boolean isOffline){
        //given
        Event event = Event.builder()
                .location(location)
                .build();
        //when
        event.update();
        //then
        assertThat(event.isOffline()).isEqualTo(isOffline);
    }

    private static Stream<Arguments> testFree_useParams(){
        int free = 0;
        int pay = 1000;
        boolean isFree = true;
        return Stream.of(
                Arguments.of(free,free,isFree),
                Arguments.of(pay,free,!isFree),
                Arguments.of(free,pay,!isFree),
                Arguments.of(pay,pay,!isFree)
        );
    }
    private static Stream<Arguments> testOffline_useParams(){
        String useLocation = "비어 있지 않음";
        String emptyLocation = "      ";
        boolean isOffline = true;
        return Stream.of(
                Arguments.of(null,!isOffline),
                Arguments.of(emptyLocation,!isOffline),
                Arguments.of(useLocation,isOffline)
        );
    }
}