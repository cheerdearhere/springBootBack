//package com.backkeesun.inflearnrestapi.events;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.hamcrest.Matchers;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.hateoas.MediaTypes;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.time.LocalDateTime;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
///**
// * Slicing test로는 제대로 영속성 처리를 체크할 수 없으므로 test를 더 진행할 수 없다
// * Mocking 테스트는 여기까지만 진행하고 Stop
// */
//@WebMvcTest // MockMvc를 주입받아 사용
//class EventControllerSlicingTests {
//    @Autowired
//    MockMvc mockMvc; //웹과 같은 환경으로 테스트(계층별 테스트: slicing test)
//    @Autowired
//    ObjectMapper objectMapper;// json으로 변환
//    @MockBean // 이 bean을 MockBean으로 만들어 줌
//    EventRepository eventRepository;//repository는 webBean이 아니어서 주입되지 않음
//
//    @Test
//    void createEvent() throws Exception {
//        //given
//        Event event = Event.builder()
//                .id(10) //test의 경우 id값을 생성하지 않으므로 nullPointException 발생. 이를 대비한 테스트 자료
//                .name("spring")
//                .description("RestAPI Development with Spring")
//                .beginEnrollmentDateTime(LocalDateTime.of(2018,11,12,13,21))
//                .closeEnrollmentDateTime(LocalDateTime.of(2018,12,30,11,12))
//                .beginEventDateTime(LocalDateTime.of(2018, 11, 14,10,5))
//                .endEventDateTime(LocalDateTime.of(2018,12,1,23,00))
//                .basePrice(100)
//                .maxPrice(200)
//                .limitOfEnrollment(100)
//                .location("강남역 D2 스타터 팩토리")
//                .free(true)//입력되서는 안될 값 체크용
//                .offline(false)
//                .build();
//        //when & then
//            //test의 경우 id값을 생성하지 않으므로 nullPointException 발생. 이를 대비한 처리
//        Mockito.when(
//                eventRepository
//                    .save(event))
//                    .thenReturn(event);
//        mockMvc.perform(
//                    post("/api/events")//HTTPRequestServlet Method
//                        .contentType(MediaType.APPLICATION_JSON) // request 구체적 구현
//                        .accept(MediaTypes.HAL_JSON) // HAL JSON response 요구
//                        .content(objectMapper.writeValueAsString(event))// object를 JsonString으로 변환
//                )
//                .andDo(print())//결과 프린팅 내용을 아래에서 체크할 수 있다.
//                .andExpect(status().isCreated()) //isCreated: 201
//                .andExpect(jsonPath("id").exists())//연속적으로 데이터 확인 가능
//                .andExpect(header().exists(HttpHeaders.LOCATION))
//                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
//                //입력된 값에 대한 제한 체크(웹 단계의 슬라이싱 테스트에서는 제대로 generate되는지 확인 불가)
////                .andExpect(jsonPath("id").value(Matchers.not(10)))//id 값은 generate 처리된 값
////                .andExpect(jsonPath("free").value(Matchers.not(true)))
////                .andExpect(jsonPath("offline").value(Matchers.not(false)))
//        ;
//        /* TDD는 보통 데이터 3개 정도를 넣고 진행 */
//    }
//}
