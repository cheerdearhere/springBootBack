# springBootBack
from inflearn(https://www.inflearn.com/course/spring_rest-api)
### using Dependencies
java 11, spring boot 2.x.x(maven), postgresql, h2(test), JPA, Lombok, restdocs

# I. 강의 개요
2017년 네이버의 Deview 컨퍼런스에서 이응준님의 발표에서 영감을 받음. 현재 REST API라 불리고 쓰이는 것들이 과연 RestFUL한가에 대한 의문. 진짜 REST API 만들기
## A. 로이 필딩이 정의한 REST: REpresentational State Transfer
인터넷 상의 시스템 간의 상호 운용성(interoperability)을 제공하는 방법중 하나 

    REST API: REST 아키텍처 스타일을 따르는 API
        Client-Server
        Stateless
        Cache
        Uniform Interface
            Identification of resources
            manipulation of resources through representations
            self-descriptive messages : response만으로도 의미가 명확(설명을 위한 링크가 포함된다거나 Content-type header가 명확…)해야하고
            hypermedia as the engine of application state (HATEOAS) : 이후 움직임을 위한 미디어(url)을 포함해야한다
        Layered System
        Code-On-Demand (optional)
특히 self-descriptive messages와 HATEOAS가 무시되고 있음
## B. 이에 대한 대안
### 1. 미디어 타입을 구체적으로 정의 > IANA 등록 > 해당 타입을 resource로 전달할때 Content-type 헤더에 기록: 브라우저들마다 스펙 지원이 달라 처리가 안될 수 있음
### 2. profile 링크 헤더 추가 : 브라우저들마다 스펙 지원이 달라 처리가 안될 수 있음
### 3. 2에 대한 대안 : 헤더의 HAL에 링크 데이터로 profile 링크 추가
참고: https://docs.github.com/en/free-pro-team@latest/rest/issues/issues?apiVersion=2022-11-28#update-an-issue

# II. Spring boot 관련
## A. Annotation 처리 결과 보기: target 폴더
target 폴더에 생성된 class들을 보면 어노테이션으로 넘어간 method, data, constructor 등의 결과를 직접 확인할 수 있다.
## B. @EqualsAndHashCode(of= {"id", "name"}) / @EqualsAndHashCode(of= "id")
equals와 hashcode 비교 메서드 처리시 입력한 값을 기준으로만 해당 객체 비교. 단, 연관관계가 있는 데이터가 있는 경우에는 구현체에서 서로 무한 비교하다가 stackOverFlow가 발생할 수 있어 사용 x
## C. Entity에 @Data를 쓰지않는 이유
@Data에는 아래의 어노테이션을 모두 포함하지만 @EqualsAndHashCode는 모든 properties를 검색하도록하므로 stack over flow를 발생시킬 수 있다.

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @EqualsAndHashCode(of= "id") //지정해서 사용할 것을 권장
## D. URI 조형하기
        - 그냥 생성자로 만들기
        URI createdUri = linkTo(EventController.class).slash("{id}").toUri();
        - 해당 entity의 method 사용
        URI createdUri = linkTo(methodOn(EventController.class).createEvent()).slash("{id}").toUri();
## C. ResponseEntity의 body에 객체 넣기
        return ResponseEntity.created(createdUri).body(event);//.build() 대신 body() 사용
## D. JPA로 repository 만들기: 인터페이스로 상속받아서 구현 처리
    public interface EventRepository extends JpaRepository<Event,Integer> 
## E. entity validation 관리
    - Jacksons library를 사용
    - Entity에 validation 관련 어노테이션 사용(편리하지만 코드가 복잡해짐)
    - 입력값을 받는 DTO를 따로 생성해서 관리(권장하지만 중복코드 발생)
        dto를 따로 생성함으로써 받지 않을 데이터(id, free 등 연산으로 만들거나 내부에서 관리할 데이터)는 거를 수 있다.
## F. DTO에서 Entity로 만드는 방법 : 
### 1. 새로 빌드하기(생성하기) : 속도가 빠르고 안정성이 높음
    Event unpersistEvent = Event.builder()
            .name(eventDto.getName())
            .description(eventDto.getDescription())
            .location(eventDto.getLocation())
            .beginEventDateTime(eventDto.getBeginEventDateTime())
                ...
            .build();
### 2. 라이브러리 사용: ModelMapper / reflection 발생 가능성 있음
dependency 주입

    <!-- https://mvnrepository.com/artifact/org.modelmapper/modelmapper -->
    <dependency>
        <groupId>org.modelmapper</groupId>
        <artifactId>modelmapper</artifactId>
        <version>2.4.5</version>
    </dependency>

공용으로 사용하는 경우가 많으므로 bean 생성

    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }

사용할 곳에서 injection 후 사용

    private final ModelMapper modelMapper;
        ...
    Event event = modelMapper.map(eventDto,Event.class);

## G. BadRequest:400 처리하기
### 1. 불필요한 값이 전달된경우 fail response 처리하기
불필요한 값을 무시할지 체크할지 개발자가 결정
ModelMapping library를 사용하는 경우 설정만 변경(application.yml/properties)

    spring:
        jackson:
            deserialization:
                fail-on-unknown-properties: true # mapping 과정에서 불필요가 있는경우 fail 처리
### 2. Spring MVC 기능 이용하기
a. 값이 없는 경우 :controller에 @Valid 삽입, param으로 Error 객체 받음

    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto, Errors errors){
        if(errors.hasErrors()){
            return ResponseEntity.badRequest().build();
        }
        ...

parameter로 사용하는 DTO에서 validation의 어노테이션 사용

    @NotEmpty //String은 Empty
    private String name;
    @NotNull //값은 null
    private LocalDateTime endEventDateTime; // 종료 시간
    private String location; //optional
    @Min(0)
    private int basePrice; // optional이지만 양수

### 3. customized validation bean (만들어서 사용)
컴포넌트를 새로 작성하고

    @Component
    public class EventValidation {
        public void validate(EventDto eventDto, Errors errors){
            if(eventDto.getBasePrice() > eventDto.getMaxPrice() && eventDto.getMaxPrice() > 0){
                errors.rejectValue("basePrice","wrong input value","basePrice can't be larger than maxPrice");
                errors.rejectValue("maxPrice","wrong input value","maxPrice can't be smaller than basePrice and 0");
            }
        }
    }

reject 두 종류: method에 따라 Errors 객체에 입력되는 위치(properties)가 달라진다.

    reject() : globalError의 property
            errors.reject("globalError","just globalError test");

    rejectValue() : fieldError의 property(주로 사용)
        errors.rejectValue("endEventDateTime","0001","endEventDateTime must be before other dateTimes");


컨트롤러에서 적용

    public class EventController {
    
        private final EventRepository eventRepository;
        private final ModelMapper modelMapper;
        private final EventValidation eventValidation;
    
        @PostMapping
        public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto, Errors errors){
            eventValidation.validate(eventDto,errors);
            if(errors.hasErrors()){
                return ResponseEntity.badRequest().build();
            }
            ...
### 4. 에러 응답 메세지 본문 만들기
ResponseEntity 객체의 body에 삽입해 client가 원인을 확인하도록 함

but, java bean 규칙에 따른 properties를 갖는 Event 객체와 달리 errors는 에러를 발생한다.(아래와 같이 불가)
JSON으로 변환시킬때 ObjectMapper의 beanSerializer()를 쓰는데 이때 문제가 발생한다.

    if(errors.hasErrors()){
        return ResponseEntity.badRequest().body(errors); 
    }
변환용 class를 생성해 bean 등록

    @JsonComponent //Spring boot에서 제공
    public class ErrorsSerializer extends JsonSerializer<Errors> {//JSON String으로 변환할 대상 지정
        @Override
        public void serialize(Errors errors, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            Logger logger = LoggerFactory.getLogger(getClass());
            gen.writeStartArray(); // start generating error Array
            errors.getFieldErrors().forEach(e->{
                try{
                    gen.writeStartObject(); //start generating Errors Object
                    gen.writeStringField("field", e.getField());
                    gen.writeStringField("objectName", e.getObjectName());
                    gen.writeStringField("code", e.getCode());
                    gen.writeStringField("defaultMessage", e.getDefaultMessage());
                    Object rejectedValue = e.getRejectedValue();
                    if (rejectedValue != null) {
                        gen.writeStringField("rejectedValue", rejectedValue.toString());
                    }
                    gen.writeEndObject(); // finish generating Errors Object
                }catch (IOException ie){
                    logger.error(ie.getMessage());
                }
            });
            errors.getGlobalErrors().forEach(error->{
                ...
            });
            gen.writeEndArray(); // end generating error Array
        }
    }
변환 처리 후 controller의 ....body(errors);처리

# III. 비즈니스 로직 관련
## A. Event API 비즈니스 로직
### 1. parameters
    - name
    - description
    - beginEnrollmentDateTime
    - closeEnrollmentDateTime
    - beginEventDateTime
    - endEventDateTime
    - location
    - basePrice
    - maxPrice
    - limitOfEnrollment
### 2. price 로직
    base    max     logic
    0       100     선착순
    0       0       무료
    100     0       무제한 경매
    100     100     선착순 경매(순위꿘)
### 3. response data
    id
    name
    description
    beginEnrollmentDateTime
    closeEnrollmentDateTime
    beginEventDateTime
    endEventDateTime
    location
    basePrice
    maxPrice
    limitOfEnrollment
    eventStatus
    offline
    free
    _links
        profile
        self
        publish
        ...

# IV. 기타
## A. Postgresql 설치
    ver 13 사용.
    CREATE USER {ID} PASSWORD '{PASSWORD}' + 권한(테스트는 SUPERUSER);
    CREATE DATABASE {DATABASE_TITLE} OWNER {OWNER_ID};

## B. 웹 계층 테스트
단위테스트만큼 가볍지는 않지만 웹 계층의 이벤트와 request, response 등을 처리함. 서버는 띄우지 않지만 dispatcherServlet까지는 띄움

    @WebMvcTest // MockMvc를 주입받아 사용
    class EventControllerTests {
        @Autowired
        MockMvc mockMvc; //웹과 같은 환경으로 테스트(계층별 테스트: slicing test)
        @Autowired
        ObjectMapper objectMapper;// json으로 변환
        @MockBean // 이 bean을 MockBean으로 만들어 줌.
        EventRepository eventRepository;//repository는 webBean이 아니어서 주입되지 않음
    
        ...data...
   
        @Test
        void createEvent() throws Exception {
            
                mockMvc.perform( post("/api/events")//HTTPRequestServlet Method
                    .contentType(MediaType.APPLICATION_JSON) // request 구체적 구현
                    .accept(MediaTypes.HAL_JSON) // HAL JSON response 요구
                    .content(objectMapper.writeValueAsString(event))// object를 JsonString으로 변환
                )
                .andDo(print())//결과 프린팅
                .andExpect(status().isCreated()) //isCreated: 201
                .andExpect(jsonPath("id").exists());//연속적으로 데이터 확인 가능
            }
        }
    }

## C. Spring HATEOAS library
스프링에서 RestFul Api을 더 잘 구성하도록 돕는 라이브러리. 
Spring boot의 도움으로 별도의 어노테이션이나 설정 없이 사용가능

HATEOAS: 수신된 response만으로도 app과 client 사이의 동작을 동적으로 소통할 수 있는 상태. (rest api)

https://docs.spring.io/spring-hateoas/docs/current/reference/html/
https://docs.spring.io/spring-hateoas/docs/1.0.1.RELEASE/reference/html/
### 1. 핵심 기능
    a. 링크를 만드는 기능 
        - 문자열로 생성
        - 생성 메서드(LinkTo method)
    b. 리소스를 만드는 기능
        - resources data + link
    c. 링크를 찾아주는 기능
        - Traberison
        - LinkDiscovers
### 2. 링크에 들어가야할 정보
    - HREF  : link
    - REL   : 현재 정보와의 관계 - self, profile, transfer, update, query-event, withdraw, ... etc.
### 3. 버전에 따라 클래스 명이 달라짐을 주의
![img_2.png](img_2.png)

### 4. resource 사용하기 
#### a. 방법 1 :  RepresntationModel(구 ResourceSupport)
RepresntationModel(구 ResourceSupport) 사용: Entity를 eventResource로 만들때 적용
    
    public class EventResource extends RepresentationModel {
        @JsonUnwrapped
        private Event event;
        public EventResource (Event event){
            this.event = event;
            eventResource.add(linkTo(EventController.class).withRel("query-events"));
            eventResource.add&#40;selfLinkBuilder.withSelfRel&#40;&#41;&#41;;)
            eventResource.add&#40;selfLinkBuilder.withRel&#40;"update-event"&#41;&#41;;//HttpMethod 차이일뿐 링크는 같을 수 있음)
        }
        public Event getEvent() {
            return event;
        }
    }

@JsonUnwrapped : entity를 분해해서 내부 내용을 입력하게함(별도로 setter과정 생략)

controller에서 사용

    //        ControllerLinkBuilder
        WebMvcLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(newEvent.getId());
        URI createdUri = selfLinkBuilder.toUri();
        EventResource eventResource = new EventResource(event);
        //new Link("url")도 가능:         add(new Link("http://localhost:8181/api/events/"+event.getId()));
        //데이터가 변경되면 하나하나 직접 변경해야흐므로 아래를 권장
        eventResource.add(linkTo(EventController.class).withRel("query-events"));
        eventResource.add&#40;selfLinkBuilder.withSelfRel&#40;&#41;&#41;;)
        eventResource.add&#40;selfLinkBuilder.withRel&#40;"update-event"&#41;&#41;;//HttpMethod 차이일뿐 링크는 같을 수 있음)
        return ResponseEntity.created(createdUri).body(eventResource);

#### b. 방법 2 : EntityModel(구 Resource) 사용하기
RepresentationModel와 달리 EntityModel은 별도로 JsonUnWrappted annotation을 사용하지 않아도 매핑됨.

    import org.springframework.hateoas.EntityModel;
    import org.springframework.hateoas.Link;
    
    import java.util.Arrays;
    
    public class EventResource extends EntityModel<Event> {
        public EventResource(Event content, Link... links){
            super(content, Arrays.asList(links));
            // 컨트롤러가 아닌 resource의 생성 단계에서 추가도 가능(권장)
            
        }
    }

## D. TDD 진행시 주의사항
### 1. 가능한 정해진 variable을 사용한다
        .andExpect(header().exists(HttpHeaders.LOCATION)) //"location"보다는 HttpHeaders.Location
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))\
### 2. TDD는 보통 데이터 3개 정도를 넣고 진행
### 3. Test 진행시 test 명칭 변경하기
a. 테스트 코드의 명칭을 직접 변경(한글도 가능) 단, 띄어쓰기 불가

    @Test
    void 빌드확인(){
        //given
        //when
        Event event = Event.builder()
                .name("Inflearn Spring Boot")
                .description("REST API development with Spring boot")
                .build();
        //then
        assertThat(event).isNotNull();
    }
b. Junit5인경우: @DisplayName(value) 사용하기

    @Test
    @DisplayName(value = "정상 처리된 경우 확인")
    void createEvent() throws Exception {
        //given
        EventDto event = EventDto.builder()
        ...

c. Junit4인경우: 직접 test용 description 작성하기

    @Target(ElementType.METHOD) // 대상
    @Retention(RetentionPolicy.SOURCE) // life cycle
    public @interface TestDescription {
        String value(); //입력값.
        String useDefault() default "a"; //기본값을 지정하는 경우
    }
작성 후 test에서 사용주석 대신 사용... test 코드는 그대로 나옴 

    @Test
    @TestDescription(value = "잘못된 값이 입력 됐을때 response code 체크")
    void createEvent_BadRequest_WrongData() throws Exception{
Junit 5 사용을 권장.

### 4. 전달된 json 값 확인하기
error메세지 확인용 test : errors 객체에 배열로 들어있음 그중 첫 데이터만 확인

    mockMvc.perform(post("/api/events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaTypes.HAL_JSON)
                    .content(objectMapper.writeValueAsString(eventDto))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$[0].objectName").exists())
            .andExpect(jsonPath("$[0].field").exists())
            .andExpect(jsonPath("$[0].defaultMessage").exists())
            .andExpect(jsonPath("$[0].code").exists())
            .andExpect(jsonPath("$[0].rejectedValue").exists())
            .andDo(print());

### 5. parameter 변경에 따른 테스트인 경우 중복이 많을 수 있다. 이때 쓰면 좋은 library
junit4 : https://www.baeldung.com/junit-params
junit5 : https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-params 

주의!! junit 버전과 일치 확인

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
수행결과
![img_1.png](img_1.png)