package com.backkeesun.inflearnrestapi.events;

import com.backkeesun.inflearnrestapi.account.Account;
import com.backkeesun.inflearnrestapi.account.AccountRepository;
import com.backkeesun.inflearnrestapi.account.AccountRole;
import com.backkeesun.inflearnrestapi.account.AccountService;
import com.backkeesun.inflearnrestapi.common.AppProperties;
import com.backkeesun.inflearnrestapi.common.RestDocsConfiguration;
import com.backkeesun.inflearnrestapi.common.WebMockControllerTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slicing test로는 제대로 영속성 처리를 체크할 수 없으므로 test를 더 진행할 수 없다
 * WebMvc 계층 테스트를 spring test로 변경
 */
//@SpringBootTest
//@AutoConfigureMockMvc // spring내에서 mock사용
//@AutoConfigureRestDocs // REST Docs용
//@Import(RestDocsConfiguration.class)
//@ActiveProfiles("test")//application-test.yml override
class EventControllerTests extends WebMockControllerTest {
    @Autowired
    EventRepository eventRepository;
//    @Autowired
//    MockMvc mockMvc; //AutoConfigureMockMvc로 사용 가능
//    @Autowired
//    ObjectMapper objectMapper;// json으로 변환
    //repository도 포함되므로 Mockito는 필요 x
    @Autowired // 권한 처리용
    AccountService accountService;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    AppProperties appProperties;

    @BeforeEach
    void beforeEach(){
        //각 method 실행 전에 db 비우기
        eventRepository.deleteAll();
        accountRepository.deleteAll();
    }

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
                            .header(HttpHeaders.AUTHORIZATION, "Barer "+getAuth())
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
                            .header(HttpHeaders.AUTHORIZATION, "Barer "+getAuth())
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
                        .header(HttpHeaders.AUTHORIZATION, "Barer "+getAuth())
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
                        .header(HttpHeaders.AUTHORIZATION, "Barer "+getAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(eventDto))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].field").exists())
                .andExpect(jsonPath("errors[0].objectName").exists())
                .andExpect(jsonPath("errors[0].code").exists())
                .andExpect(jsonPath("errors[0].defaultMessage").exists())
                .andExpect(jsonPath("errors[0].rejectedValue").exists())
                .andExpect(jsonPath("_links.index").exists()) //error때 이동할 api index
                .andDo(print());
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
    @Test
    @DisplayName(value = "HATEOAS : 정상 작동시 링크 생성 확인")
    void createWithLink() throws Exception{
        EventDto eventDto = inputDataObject("이름", "설명 설명", 100, 200, "서울시 어딘가");
        mockMvc.perform(post("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, "Barer "+getAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(eventDto))

            )
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE,MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.query-events").exists())
                .andExpect(jsonPath("_links.update-event").exists())
                .andDo(print());
    }

    @Test
    @DisplayName(value = "spring rest docs처리 확인")
    void restDocsBasic()throws Exception{
        EventDto eventDto = inputDataObject("이름", "설명 설명", 100, 200, "서울시 어딘가");
        mockMvc.perform(post("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, "Barer "+getAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(eventDto))
                )
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE,MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.query-events").exists())
                .andExpect(jsonPath("_links.update-event").exists())
//                .andDo(document("create_event"))
                ;
    }


    /**
     * API 문서 조각 만들기
     *  요청 본문 문서화
     *  응답 본문 문서화
     *  링크 문서화
     *    * self, query, update
     *    * profile 링크(문서 완성 후)
     *  요청 헤더 문서화
     *  요청 필드 문서화
     *  응답 헤더 문서화
     *  응답 필드 문서화
     * @throws Exception
     */
    @Test
    @DisplayName(value = "spring rest docs 문서 조각(스니펫) 만들기")
    void restDocsField() throws Exception{
        EventDto eventDto = inputDataObject("이름", "설명 설명", 100, 200, "서울시 어딘가");
        mockMvc.perform(post("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, "Barer "+getAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(eventDto))
                )
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("id").exists())
                //document에서 체크하는 경우, 특히 링크는 두 번 체크하므로 굳이 체크할 필요 없음.
//                .andExpect(jsonPath("_links.self").exists())
//                .andExpect(jsonPath("_link.profile").exists()) //html 문서 확인 후 작성
//                .andExpect(jsonPath("_links.query-events").exists())
//                .andExpect(jsonPath("_links.update-event").exists())
                //이곳에서 작성
                .andDo(document("create-event",
                        links(
                                linkWithRel("self").description("Link to self"),
                                linkWithRel("query-events").description("Link to query events"),
                                linkWithRel("update-event").description("Link to update an existing event"),
                                linkWithRel("profile").description("Link to profile page")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("header:"+MediaTypes.HAL_JSON_VALUE),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("contentType: "+MediaType.APPLICATION_JSON)
                        ),
                        requestFields(
                                fieldWithPath("name").description("event name"),
                                fieldWithPath("description").description("information of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date and time of begin of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date and time of expire of new event"),
                                fieldWithPath("beginEventDateTime").description("date and time of start of event"),
                                fieldWithPath("endEventDateTime").description("date and time of finish of event"),
                                fieldWithPath("location").description("address of event, null is online event"),
                                fieldWithPath("basePrice").description("minimum price to join event"),
                                fieldWithPath("maxPrice").description("maximum price to join event"),
                                fieldWithPath("limitOfEnrollment").description("maximum user number")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("address of event"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("contentType"+MediaTypes.HAL_JSON_VALUE)
                        ),
                        //
                        responseFields(
                                fieldWithPath("id").description("event id"),
                                fieldWithPath("name").type(JsonFieldType.STRING).description("event name"),
                                fieldWithPath("description").description("information of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date and time of begin of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date and time of expire of new event"),
                                fieldWithPath("beginEventDateTime").description("date and time of start of event"),
                                fieldWithPath("endEventDateTime").description("date and time of finish of event"),
                                fieldWithPath("location").description("address of event, null is online event"),
                                fieldWithPath("basePrice").description("minimum price to join event"),
                                fieldWithPath("maxPrice").description("maximum price to join event"),
                                fieldWithPath("limitOfEnrollment").description("maximum user number"),
                                fieldWithPath("offline").description("has location ? true : false"),
                                fieldWithPath("free").description("has basePrice or maxPrice ? false : true"),
                                fieldWithPath("eventStatus").description("event's current status"),
                                //optional fields
                                fieldWithPath("_links.self.href").type(JsonFieldType.STRING).description("my href").optional(),
                                fieldWithPath("_links.query-events.href").type(JsonFieldType.STRING).description("my href").optional(),
                                fieldWithPath("_links.update-event.href").type(JsonFieldType.STRING).description("my href").optional(),
                                fieldWithPath("_links.profile.href").description("this profile").optional()
                        )
                ));
    }

    @Test
    @DisplayName(value="30개의 이벤트를 10개씩 조회 - 2page")
    void queryEvents() throws Exception{
        //given
        IntStream.range(0,30).forEach(this::generateEvent);
/**
*         IntStream.range(0,30).forEach(i->{
*             this.generateEvent(i);
*         });
*/
        //when & then
        this.mockMvc.perform(get("/api/events")
                        .param("page","1")//paging data
                        .param("size","10")
                        .param("sort","name,DESC")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists()) //paging data
                .andExpect(jsonPath("_links.first").exists()) //paging link
                .andExpect(jsonPath("_links.prev").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.next").exists())
                .andExpect(jsonPath("_links.last").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.query-events").exists())//개별 link
                .andExpect(jsonPath("_embedded.eventList[0]._links.update-event").exists())
                .andDo(document("query-events",
                        links(
                                linkWithRel("first").description("Link to first page"),
                                linkWithRel("prev").description("Link to previous page"),
                                linkWithRel("self").description("Link to self"),
                                linkWithRel("next").description("Link to next page"),
                                linkWithRel("last").description("Link to last page"),
                                linkWithRel("profile").description("Link to profile page")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("contentType"+MediaTypes.HAL_JSON_VALUE)
                        ),
                        responseFields(
                                subsectionWithPath("_embedded").description("An array of events"),//여러 요소를 포함한 객체인 경우
                                subsectionWithPath("_embedded.eventList").ignored(),//array 등 을 대상으로 하위 요소를 무시할 경우
                                //page data
                                fieldWithPath("page.size").description("page size is 10 rows"),
                                fieldWithPath("page.totalElements").description("total number"),
                                fieldWithPath("page.totalPages").description("total pages"),
                                fieldWithPath("page.number").description("index of page"),
                                //link data
                                fieldWithPath("_links.first.href").description("Link to first page").optional(),
                                fieldWithPath("_links.prev.href").description("Link to previous page").optional(),
                                fieldWithPath("_links.self.href").description("Link to self").optional(),
                                fieldWithPath("_links.next.href").description("Link to next page").optional(),
                                fieldWithPath("_links.last.href").description("Link to last page").optional(),
                                fieldWithPath("_links.profile.href").description("Link to profile page").optional()
                        )
                ));
    }

    @Test
    @DisplayName(value="기존 이벤트 중 하나 조회하기")
    void getEventOne() throws Exception{
        //given
        Event event = this.generateEvent(100);

        //when
        ResultActions perform = this.mockMvc.perform(get("/api/events/{id}",event.getId()));
        //then
        perform.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists()) //index.adoc 파일의 링크 참조
//                .andExpect(jsonPath("_links.update").exists()) 유저 정보  처리 후 권한에 따른 update 처리
                .andDo(document("get-event",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-events").description("link to data"),
                                linkWithRel("update-event").description("link to update for author"),
                                linkWithRel("profile").description("this event info")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description(MediaTypes.HAL_JSON_VALUE)
                        ),
                        responseFields(
                                fieldWithPath("id").description("event id"),
                                fieldWithPath("name").type(JsonFieldType.STRING).description("event name"),
                                fieldWithPath("description").description("information of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date and time of begin of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date and time of expire of new event"),
                                fieldWithPath("beginEventDateTime").description("date and time of start of event"),
                                fieldWithPath("endEventDateTime").description("date and time of finish of event"),
                                fieldWithPath("location").description("address of event, null is online event"),
                                fieldWithPath("basePrice").description("minimum price to join event"),
                                fieldWithPath("maxPrice").description("maximum price to join event"),
                                fieldWithPath("limitOfEnrollment").description("maximum user number"),
                                fieldWithPath("offline").description("has location ? true : false"),
                                fieldWithPath("free").description("has basePrice or maxPrice ? false : true"),
                                fieldWithPath("eventStatus").description("event's current status"),
                                fieldWithPath("author").description("author"),
                                //optional fields
                                fieldWithPath("_links.self.href").type(JsonFieldType.STRING).description("my href").optional(),
                                fieldWithPath("_links.query-events.href").type(JsonFieldType.STRING).description("my href").optional(),
                                fieldWithPath("_links.update-event.href").type(JsonFieldType.STRING).description("my href").optional(),
                                fieldWithPath("_links.profile.href").description("this profile").optional()
                        )
                ));
    }
    @Test
    @DisplayName(value="없는 이벤트인 경우 404 에러")
    void notFoundEvent() throws Exception{
        //given
        //when
        ResultActions perform = this.mockMvc.perform(get("/api/events/1000"));
        //then
        perform.andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName(value="업데이트 처리")
    void updateEvent() throws Exception{
        //given
        Event event = this.generateEvent(100);
        EventDto updateDto = inputDataObject("updated event Name", "test is success", 111, 222, "test city");
        //when
        ResultActions perform = this.mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, "Barer "+getAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .content(objectMapper.writeValueAsString(updateDto))
        );
        //then
        perform.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("name").value(updateDto.getName()))
                .andExpect(jsonPath("location").value(updateDto.getLocation()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("update-event",
                    links(
                            linkWithRel("self").description("link to self"),
                            linkWithRel("query-events").description("link to list"),
                            linkWithRel("update-event").description("link to update"),
                            linkWithRel("profile").description("this event")
                    ),
                    requestHeaders(
                            headerWithName(HttpHeaders.CONTENT_TYPE).description(MediaType.APPLICATION_JSON),
                            headerWithName(HttpHeaders.ACCEPT).description(MediaTypes.HAL_JSON_VALUE)
                    ),
                        requestFields(
                                fieldWithPath("name").description("event name"),
                                fieldWithPath("description").description("information of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date and time of begin of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date and time of expire of new event"),
                                fieldWithPath("beginEventDateTime").description("date and time of start of event"),
                                fieldWithPath("endEventDateTime").description("date and time of finish of event"),
                                fieldWithPath("location").description("address of event, null is online event"),
                                fieldWithPath("basePrice").description("minimum price to join event"),
                                fieldWithPath("maxPrice").description("maximum price to join event"),
                                fieldWithPath("limitOfEnrollment").description("maximum user number")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("address of event"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("contentType"+MediaTypes.HAL_JSON_VALUE)
                        ),
                        responseFields(
                                fieldWithPath("id").description("event id"),
                                fieldWithPath("name").type(JsonFieldType.STRING).description("event name"),
                                fieldWithPath("description").description("information of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date and time of begin of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date and time of expire of new event"),
                                fieldWithPath("beginEventDateTime").description("date and time of start of event"),
                                fieldWithPath("endEventDateTime").description("date and time of finish of event"),
                                fieldWithPath("location").description("address of event, null is online event"),
                                fieldWithPath("basePrice").description("minimum price to join event"),
                                fieldWithPath("maxPrice").description("maximum price to join event"),
                                fieldWithPath("limitOfEnrollment").description("maximum user number"),
                                fieldWithPath("offline").description("has location ? true : false"),
                                fieldWithPath("free").description("has basePrice or maxPrice ? false : true"),
                                fieldWithPath("eventStatus").description("event's current status"),
                                fieldWithPath("author").description("author"),
                                //optional fields
                                fieldWithPath("_links.self.href").ignored(),
                                fieldWithPath("_links.query-events.href").ignored(),
                                fieldWithPath("_links.update-event.href").ignored(),
                                fieldWithPath("_links.profile.href").ignored()
                        )
        ));
    }
    @Test
    @DisplayName("업데이트 할때 id 값의 대상이 없는 경우")
    void updateNotFound()throws Exception{
        //given
//        Event is null
        Integer noSavedId = 1000;
        EventDto eventDto = inputDataObject("not","found",0,0,"data");
        //when
        ResultActions perform = this.mockMvc.perform(put("/api/events/{id}", noSavedId)
                .header(HttpHeaders.AUTHORIZATION, "Barer "+getAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .content(objectMapper.writeValueAsString(eventDto))
        );
        //then
        perform.andExpect(status().isNotFound()).andDo(print());
    }
    @Test
    @DisplayName(value = "업데이트할때 값이 빈 경우")
    void updateNullData()throws Exception{
        //given
        Event originEvent = this.generateEvent(100);
        //when
        EventDto eventDto = new EventDto();
        ResultActions perform = this.mockMvc.perform(put("/api/events/{id}", originEvent.getId())
                .header(HttpHeaders.AUTHORIZATION, "Barer "+getAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .content(objectMapper.writeValueAsString(eventDto))
        );
        //then
        perform.andExpect(status().isBadRequest()).andDo(print());
    }
    @Test
    @DisplayName(value = "업데이트할때 값이 이상한 경우")
    void updateBadRequest()throws Exception{
        //given
        Event originalEvent = this.generateEvent(100);
        //when
        EventDto updateEvent = inputDataObject("basePrice","bigger than",1000,500,"maxPrice");
        ResultActions perform = this.mockMvc.perform(put("/api/events/{id}", originalEvent.getId())
                .header(HttpHeaders.AUTHORIZATION, "Barer "+getAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .content(objectMapper.writeValueAsString(updateEvent))
        );
        //then
        perform.andExpect(status().isBadRequest())
                .andDo(print());
    }

    /*=== Private Method ===*/
    private Event generateEvent(int i) {
        int basePrice = i%3 == 0 ? 0 :100;
        int maxPrice = i%3 == 1 ? 0 : 200;
        String location = i%2 == 0 ? "" : "서울시 어딘가" ;
        Event event = Event.builder()
                .name("event"+i)
                .description("test event")
                .beginEnrollmentDateTime(LocalDateTime.of(2018,11,12,13,21))
                .closeEnrollmentDateTime(LocalDateTime.of(2018,12,30,11,12))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 14,10,5))
                .endEventDateTime(LocalDateTime.of(2019,12,1,23,1))
                .basePrice(basePrice)
                .maxPrice(maxPrice)
                .location(location)
                .build();
        return this.eventRepository.save(event);
    }

    private String getAuth() throws Exception {
        //save user data
        String email=appProperties.getUserUsername();
        String password =appProperties.getUserPassword();
        Account temp = Account.builder()
                .email(email)
                .password(password)
                .roles(Set.of(AccountRole.USER, AccountRole.ADMOIN))
                .build();
        this.accountService.saveAccount(temp);
        //get Token
        ResultActions perform = this.mockMvc.perform(post("/oauth/token")//url은 자동처리
                .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))// request header 생성
                .param("username",email)//인증 정보 삽입
                .param("password",password)
                .param("grant_type","password")
        );
        //return token from response
        String responseStr = perform.andReturn().getResponse().getContentAsString();
        Jackson2JsonParser parser = new Jackson2JsonParser();
        return parser.parseMap(responseStr).get("access_token").toString();
    }
    private static EventDto inputDataObject(String name, String description, int basePrice, int maxPrice, String location) {
        return EventDto.builder()
                .name(name)
                .description(description)
                .beginEnrollmentDateTime(LocalDateTime.of(2023,11,12,13,21))
                .closeEnrollmentDateTime(LocalDateTime.of(2023,12,30,11,12))
                .beginEventDateTime(LocalDateTime.of(2023, 11, 14,10,5))
                .endEventDateTime(LocalDateTime.of(2024,12,1,23,1))
                .basePrice(basePrice)
                .maxPrice(maxPrice)
                .location(location)
                .build();
    }
    /*=== /Private Method ===*/
}
