package com.backkeesun.inflearnrestapi.events;

import com.backkeesun.inflearnrestapi.common.TestDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slicing test로는 제대로 영속성 처리를 체크할 수 없으므로 test를 더 진행할 수 없다
 * WebMvc 계층 테스트를 spring test로 변경
 */
@SpringBootTest
@AutoConfigureMockMvc // spring내에서 mock사용
class EventControllerTests {
    @Autowired
    MockMvc mockMvc; //AutoConfigureMockMvc로 사용 가능
    @Autowired
    ObjectMapper objectMapper;// json으로 변환
    //repository도 포함되므로 Mockito는 필요 x
    @Test
    @DisplayName(value = "정상 처리된 경우 확인")
    void createEvent() throws Exception {
        //given
        EventDto event = EventDto.builder()
//                .id(10) //test의 경우 id값을 생성하지 않으므로 nullPointException 발생. 이를 대비한 테스트 자료
                .name("spring")
                .description("RestAPI Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2018,11,12,13,21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018,12,30,11,12))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 14,10,5))
                .endEventDateTime(LocalDateTime.of(2019,12,1,23,1))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타터 팩토리")
//                .free(true)//입력되서는 안될 값 체크용
//                .offline(false)
//                .eventStatus(EventStatus.PUBLISHED)
                .build();
        //when & then
        mockMvc.perform(post("/api/events")//HTTPRequestServlet Method
                            .contentType(MediaType.APPLICATION_JSON) // request 구체적 구현
                            .accept(MediaTypes.HAL_JSON) // HAL JSON response 요구
                            .content(objectMapper.writeValueAsString(event))// object를 JsonString으로 변환
                )
                .andDo(print())//결과 프린팅 내용을 아래에서 체크할 수 있다.
                .andExpect(status().isCreated()) //isCreated: 201
                .andExpect(jsonPath("id").exists())//연속적으로 데이터 확인 가능
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                //입력된 값에 대한 제한 체크
//                .andExpect(jsonPath("id").value(Matchers.not(10)))//dto를 사용하면서 id입력 안받음. 값 유무는 위에서 확인
                .andExpect(jsonPath("free").value(Matchers.not(true)))//default값 확인
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()));
//                .andExpect(jsonPath("offline").value(Matchers.not(false))); 아직 내부 연산 처리 안됨
        /* TDD는 보통 데이터 3개 정도를 넣고 진행 */
    }
    @Test
    @DisplayName(value = "불필요한 값이 입력 됐을때 response code 체크")
    void createEvent_BadRequest() throws Exception {
        //given
        Event event = Event.builder()
                .id(10) //불필요한 값이 있는 경우 테스트
                .name("spring")
                .description("RestAPI Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2018,11,12,13,21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018,12,30,11,12))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 14,10,5))
                .endEventDateTime(LocalDateTime.of(2018,12,1,23,1))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타터 팩토리")
                .free(true)//불필요한 값이 있는 경우 테스트
                .offline(false)//불필요한 값이 있는 경우 테스트
                .eventStatus(EventStatus.PUBLISHED)//불필요한 값이 있는 경우 테스트
                .build();
        //when & then
        mockMvc.perform(post("/api/events")//HTTPRequestServlet Method
                            .contentType(MediaType.APPLICATION_JSON) // request 구체적 구현
                            .accept(MediaTypes.HAL_JSON) // HAL JSON response 요구
                            .content(objectMapper.writeValueAsString(event))// object를 JsonString으로 변환
                )
                .andDo(print())//결과 프린팅 내용을 아래에서 체크할 수 있다.
                .andExpect(status().isBadRequest()); //isBadRequest : 400
    }
    @Test
    @DisplayName(value = "빈 값이 입력 됐을때 response code 체크")
    void createEvent_BadRequest_EmptyInput() throws Exception {
        //given
        EventDto eventDto = EventDto.builder().build();
        //when then
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(eventDto))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
//    @TestDescription(value = "잘못된 값이 입력 됐을때 response code 체크")
    @DisplayName(value = "잘못된 값이 입력 됐을때 response code 체크")
    void createEvent_BadRequest_WrongData() throws Exception{
        //given
        EventDto eventDto = EventDto.builder()
                .name("spring")
                .description("RestAPI Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2018,11,12,13,21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018,12,30,11,12))
                //끝나는 날짜가 시작 날짜 보다 빠를때 테스트
                .beginEventDateTime(LocalDateTime.of(2018, 11, 14,10,5))
                .endEventDateTime(LocalDateTime.of(2018,11,1,23,1))
                //base > max 테스트
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타터 팩토리")
                .build();
        //when then
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(eventDto))
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$[0].field").exists())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].code").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].rejectedValue").exists())
        ;
    }

    @Test
    @DisplayName(value = "price가 입력되면 free가 false로")
    void createEvent_putPrice() throws Exception{
        //given
        EventDto eventDto = EventDto.builder()
                .name("event")
                .description("no free")
                .beginEnrollmentDateTime(LocalDateTime.of(2018,11,12,13,21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018,12,30,11,12))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 14,10,5))
                .endEventDateTime(LocalDateTime.of(2019,12,1,23,1))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("서울 강서구")
                .build();
        //when & then
        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(eventDto))
        )
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE,MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("free").value(false))
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
                .andDo(print());
    }
}
