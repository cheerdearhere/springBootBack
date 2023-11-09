## A. Annotation 처리 결과 보기: target 폴더
target 폴더에 생성된 class들을 보면 어노테이션으로 넘어간 method, data, constructor 등의 결과를 직접 확인할 수 있다.
## B. @EqualsAndHashCode(of= {"id", "name"}) / @EqualsAndHashCode(of= "id")
equals와 hashcode 비교 메서드 처리시 입력한 값을 기준으로만 해당 객체 비교. 단, 연관관계가 있는 데이터가 있는 경우에는 구현체에서 서로 무한 비교하다가 stackOverFlow가 발생할 수 있어 사용 x
## C. Entity에 @Data를 쓰지않는 이유
@Data에는 아래의 어노테이션을 모두 포함하지만 @EqualsAndHashCode는 모든 properties를 검색하도록하므로 stack over flow를 발생시킬 수 있다.
```java
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @EqualsAndHashCode(of= "id") //지정해서 사용할 것을 권장
```
## D. URI 조형하기
```java
    // 그냥 생성자로 만들기
    URI createdUri = linkTo(EventController.class).slash("{id}").toUri();
    // 해당 entity의 method 사용
    URI createdUri = linkTo(methodOn(EventController.class).createEvent()).slash("{id}").toUri();
```
## C. ResponseEntity의 body에 객체 넣기
```java
    return ResponseEntity.created(createdUri).body(event);//.build() 대신 body() 사용
```
## D. JPA로 repository 만들기: 인터페이스로 상속받아서 구현 처리
```java
    public interface EventRepository extends JpaRepository<Event,Integer> 
```
## E. entity validation 관리
    - Jacksons library를 사용
    - Entity에 validation 관련 어노테이션 사용(편리하지만 코드가 복잡해짐)
    - 입력값을 받는 DTO를 따로 생성해서 관리(권장하지만 중복코드 발생)
        dto를 따로 생성함으로써 받지 않을 데이터(id, free 등 연산으로 만들거나 내부에서 관리할 데이터)는 거를 수 있다.
## F. DTO에서 Entity로 만드는 방법 :
### 1. 새로 빌드하기(생성하기) : 속도가 빠르고 안정성이 높음
```java
    Event unpersistEvent = Event.builder()
            .name(eventDto.getName())
            .description(eventDto.getDescription())
            .location(eventDto.getLocation())
            .beginEventDateTime(eventDto.getBeginEventDateTime())
                ...
            .build();
```
### 2. 라이브러리 사용: ModelMapper / reflection 발생 가능성 있음
dependency 주입
```xml
    <!-- https://mvnrepository.com/artifact/org.modelmapper/modelmapper -->
    <dependency>
        <groupId>org.modelmapper</groupId>
        <artifactId>modelmapper</artifactId>
        <version>2.4.5</version>
    </dependency>
```

공용으로 사용하는 경우가 많으므로 bean 생성
```java
    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }
```

사용할 곳에서 injection 후 사용
```java
    private final ModelMapper modelMapper;
        ...
    Event event = modelMapper.map(eventDto,Event.class);
```

## G. BadRequest:400 처리하기
### 1. 불필요한 값이 전달된경우 fail response 처리하기
불필요한 값을 무시할지 체크할지 개발자가 결정
ModelMapping library를 사용하는 경우 설정만 변경(application.yml/properties)
```yaml
    spring:
        jackson:
            deserialization:
                fail-on-unknown-properties: true # mapping 과정에서 불필요가 있는경우 fail 처리
```
### 2. Spring MVC 기능 이용하기
a. 값이 없는 경우 :controller에 @Valid 삽입, param으로 Error 객체 받음
```java
    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto, Errors errors){
        if(errors.hasErrors()){
            return ResponseEntity.badRequest().build();
        }
        ...
```

parameter로 사용하는 DTO에서 validation의 어노테이션 사용
```java
    @NotEmpty //String은 Empty
    private String name;
    @NotNull //값은 null
    private LocalDateTime endEventDateTime; // 종료 시간
    private String location; //optional
    @Min(0)
    private int basePrice; // optional이지만 양수
```

### 3. customized validation bean (만들어서 사용)
컴포넌트를 새로 작성하고
```java
    @Component
    public class EventValidation {
        public void validate(EventDto eventDto, Errors errors){
            if(eventDto.getBasePrice() > eventDto.getMaxPrice() && eventDto.getMaxPrice() > 0){
                errors.rejectValue("basePrice","wrong input value","basePrice can't be larger than maxPrice");
                errors.rejectValue("maxPrice","wrong input value","maxPrice can't be smaller than basePrice and 0");
            }
        }
    }
```

reject 두 종류: method에 따라 Errors 객체에 입력되는 위치(properties)가 달라진다.

reject() : globalError의 property
```java
errors.reject("globalError","just globalError test");
```
rejectValue() : fieldError의 property(주로 사용)
```java
errors.rejectValue("endEventDateTime","0001","endEventDateTime must be before other dateTimes");
```
컨트롤러에서 적용
```java
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
```
### 4. 에러 응답 메세지 본문 만들기
ResponseEntity 객체의 body에 삽입해 client가 원인을 확인하도록 함

but, java bean 규칙에 따른 properties를 갖는 Event 객체와 달리 errors는 에러를 발생한다.(아래와 같이 불가)

JSON으로 변환시킬때 ObjectMapper의 beanSerializer()를 쓰는데 이때 문제가 발생한다.
```java
    if(errors.hasErrors()){
        return ResponseEntity.badRequest().body(errors); 
    }
```
변환용 class를 생성해 bean 등록: springboot 2.3이후로는 Array부터 만드는게 불가함. JsonArray는

```java
    @JsonComponent //Spring boot에서 제공
    public class ErrorsSerializer extends JsonSerializer<Errors> {//JSON String으로 변환할 대상 지정
        @Override
        public void serialize(Errors errors, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            Logger logger = LoggerFactory.getLogger(getClass());
            gen.writeFieldName("errors");//spring boot 2.3부터 Jackson library가 더이상 Array부터 만드는 것을 금지함
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
```
변환 처리 후 controller의 ....body(errors);처리
### 5. 에러 응답 메세지 본문 만들기
에러때 event api의 index로 이동시키기
1. index 응답용 api
```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class IndexController {
    @GetMapping
    public RepresentationModel index(){
        var index= new RepresentationModel();
        index.add(linkTo(EventController.class).withRel("events"));
        return index;
    }
}
```
2. 해당 api를 응답resource로 만들 ErrorResource 구현
```java
public class ErrorResource extends EntityModel<Errors> {
    public ErrorResource(Errors errors, Link... links){
        super(errors, Arrays.asList(links));
        add(linkTo(methodOn(IndexController.class).index()).withRel("index"));
    }
}
```
3. 해당 에러 처리 과정에 추가
```java
    //controller validation part
  if(errors.hasErrors()){
      return badRequest(errors);
  }
  
  //use private method
  private static ResponseEntity<ErrorResource> badRequest(Errors errors) {
      return ResponseEntity.badRequest().body(new ErrorResource(errors));
  }
```
4. test code
```java
  .andExpect(jsonPath("errors[0].defaultMessage").exists())
  .andExpect(jsonPath("errors[0].rejectedValue").exists())
  .andExpect(jsonPath("_links.index").exists()) //error때 이동할 api index
```
## H. Not Found 404 에러
notFound의 경우 body를 처리할 method가 없어 생성자를 작성해 body를 작성할 수 있음.

```java
    private ResponseEntity<ErrorResource> notFound(Errors errors){
        ErrorResource errorResource=new ErrorResource(errors);
        return new ResponseEntity(errorResource,HttpStatus.NOT_FOUND);
    }
```
그것 없이 헤더만 사용해도됨
```java
    @GetMapping("/{id}")
    public ResponseEntity getEvent(@PathVariable Integer id){
        Optional<Event> optionalEvent = this.eventService.getEvent(id);
        if(optionalEvent.isEmpty()){
            return ResponseEntity.notFound()/*.header("headerName", "headerValue")*/.build();
        }
        Event event = optionalEvent.get();
        EventResource eventResource = new EventResource(event);
        return ResponseEntity.ok().body(eventResource);
    }
```