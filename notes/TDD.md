## A. TDD 방식
### 1. 요구사항 확인 및 구조 분석
### 2. 개발 순서 구조화
### 3. 순차적으로 진행
```markdown
> 해당 단위 test 코드 작성
> test 실패 확인
> 해당 코드 적용
> 해당 단위를 포함하는 class 전체 테스트
> refecter
> 다음 단위 작성 ... 반복
```
[TDD](https://ko.wikipedia.org/wiki/%ED%85%8C%EC%8A%A4%ED%8A%B8_%EC%A3%BC%EB%8F%84_%EA%B0%9C%EB%B0%9C#/media/%ED%8C%8C%EC%9D%BC:TDD_Global_Lifecycle.png)

## B. 단위 테스트
### 1. 기본운 given-when-then
가장 작은 단위(method 구현을 위한 테스트)
```java
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
```
### 2. 예외를 테스트하는 경우
#### a. 일반 적인 에러 확인
```java
    @Test
    void notFoundUsername(){
        String username = "known@Account.com";
        assertThrows(UsernameNotFoundException.class,()->this.accountService.loadUserByUsername(username));
    }
```
#### b. try-catch로 에러 메세지 점검하기
```java
    try{
        this.accountService.loadUserByUsername(username);
        fail();
    }catch (UsernameNotFoundException ue){
        assertThat(ue.getMessage()).containsSequence(username);
    }
```
#### c. only Junit4: @Rule을 사용해 에러 확인
테스트 클래스 내부에 예상 예외를 담도록 빈 예외 준비
```java
    @Rule
    public ExpectedException expectedException = ExpectedException.name();
```
예상 예외를 적고 테스트 수행
```java
    @Test
    void test(){
        //given
        String username = "known@Account.com";
        //expected
        expectedException.expect(UsernameNotFoundException.class);
        expectedException.expectMessage(Matchers.containsString(username));
        //when
        this.accountService.loadUserByUsername(username);
    }
```
## C. 웹 계층 테스트
단위테스트만큼 가볍지는 않지만 웹 계층의 이벤트와 request, response 등을 처리함. 서버는 띄우지 않지만 dispatcherServlet까지는 띄움
```java
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
```
## D. TDD 진행시 주의사항
### 1. 가능한 정해진 variable을 사용한다
하드코딩은 최소화하고 변수를 사용. 가능하다면 기존 데이터를 쓰는것을 권장
```java
    .andExpect(header().exists(HttpHeaders.LOCATION)) //"location"보다는 HttpHeaders.Location
    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))\
```
### 2. TDD는 보통 데이터 3개 정도를 넣고 진행
테스트를 진행할때는 데이터 여러개를 테스트
```java
import java.util.Stream.IntStream;

    IntStream.range(0,10).forEach(this::EventResource);
```
### 3. Test 진행시 test 명칭 변경하기
#### a. 테스트 코드의 명칭을 직접 변경(한글도 가능) 단, 띄어쓰기 불가
```java
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
```
#### b. Junit5인경우: @DisplayName(value) 사용하기
```java
    @Test
    @DisplayName(value = "정상 처리된 경우 확인")
    void createEvent() throws Exception {
        //given
        EventDto event = EventDto.builder()
        ...
```
#### c. Junit4인경우: 직접 test용 description 작성하기
```java
    @Target(ElementType.METHOD) // 대상
    @Retention(RetentionPolicy.SOURCE) // life cycle
    public @interface TestDescription {
        String value(); //입력값.
        String useDefault() default "a"; //기본값을 지정하는 경우
    }
```

작성 후 test에서 사용주석 대신 사용... test 코드는 그대로 나옴
```java
    @Test
    @TestDescription(value = "잘못된 값이 입력 됐을때 response code 체크")
    void createEvent_BadRequest_WrongData() throws Exception{
```
Junit 5 사용을 권장.

### 4. 전달된 json 값 확인하기
error메세지 확인용 test : errors 객체에 배열로 들어있음 그중 첫 데이터만 확인
```java
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
```
### 5. parameter 변경에 따른 테스트인 경우 중복이 많을 수 있다. 이때 쓰면 좋은 library
junit4 일때 [여기](https://www.baeldung.com/junit-params) /
junit5 일때 [여기](https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-params)

주의!! junit 버전과 일치 확인
```java
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
    //테스트 설정
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
```

수행결과
![img_1.png](img_1.png)

## E. MockMvc를 사용할때 데이터 처리
### 1. queryParam 테스트: 요청할때 param추가
```java
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
        //when
        this.mockMvc.perform(get("/api/events")
                        .param("page","1")//paging data
                        .param("size","10")
                        .param("sort","name,DESC")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists()) //Pageable을 사용한 경우
        ;
        //then
    }

    private void generateEvent(int i) {
        Event event = Event.builder()
                .name("event"+i)
                    ...
                .location("서울시 어딘가")
                .build();
        this.eventRepository.save(event);
    }
```
### 2. pathVariable을 사용하는 경우
```java
    @Test
    @DisplayName(value="기존 이벤트 중 하나 조회하기")
    void getEventOne() throws Exception{
        //given
        Event event = this.generateEvent(100);
        //when
        ResultActions perform = this.mockMvc.perform(get("/api/events/{id}",event.getId()));
        //then
        perform.andDo(print())
                .andExpect(jsonPath("id").exists())
        ;
    }
```
### 3. request의 body에 넣는 경우
컨텐츠 타입을 설정 한 후 content에 대입
```java
    @Test
    @DisplayName(value = "업데이트할때 값이 빈 경우")
    void updateNullData()throws Exception{
        //given
        Event originEvent = this.generateEvent(100);
        //when
        EventDto eventDto = new EventDto();
        ResultActions perform = this.mockMvc.perform(put("/api/events/{id}", originEvent.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON_VALUE)
                .content(objectMapper.writeValueAsString(eventDto))
        );
        //then
        perform.andExpect(status().isBadRequest()).andDo(print());
    }
```
## F. TEST code refactoring tips
### 1. 테스트 클래스들의 중복 어노테이션 줄이기: 상속
아래와 같이 유사한 테스트들마다 어노테이션이 반복된다.
```java
@SpringBootTest
@AutoConfigureMockMvc 
@AutoConfigureRestDocs 
@Import(RestDocsConfiguration.class)
@ActiveProfiles("test")
```
이를 위해 유사한 테스트에서는 반복코드를 최소화 하도록 상속으로 이어준다.
#### a. test폴더의 common package에 해당 유형의 class 생성
#### b. 기존 테스트의 annotation 가져와서 붙여넣기
```java
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
@ActiveProfiles("test")
//@Ignore Junit4에서
@Disabled //Junit5
public class WebMockControllerTest {

}

```
#### c. 유사 테스트에서 사용될 의존성들을 미리 주입
```java
...
public class WebMockControllerTest {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ModelMapper modelMapper;
    @Autowired
    protected ObjectMapper objectMapper;
}

```
#### d. 준비된 데이터들을 기존 test class에서 지우고 상속
```java
class EventControllerTests extends WebMockControllerTest {
    @Autowired //이테스트에서만 쓰는 경우는 남김
    EventRepository eventRepository;
    @Test
    @DisplayName(value = "정상 처리된 경우 확인")
    void createEvent() throws Exception { 
        ...
```
