## C. Spring REST DOCS
### 1. REST DOCS 소개
RESTful API의 문서를 제작하는데 도움을 주는 tool

test에서 체크한 정보를 모아서 snippets을 제공해 docs html을 만들 수 있음

자주 사용하는 문서 관련 tools
```markdown
    Swagger는 코드 자체에서 문서화
    Rest docs는 테스트에서 문서화
    Postman은 API의 연결정보 확인에 유용
```
최소 사용 환경:
```markdown
    java 8
    Spring Framework 5.0.2 
```
아래의 tool을 사용 :
```markdown
    MockMVC
    WebTestClient
    REST Assured
    Slate
    TestNG
    JUnit5
```
방법:
- 일반 Spring: mockMvc 객체 생성시 .apply(documentationConfiguration(this.restDocumentation))을 넣고 build()
- SpringBoot: @AutoConfigureRestDocs를 테스트 class 위에 적용
```java
    @AutoConfigureRestDocs // REST Docs용
    class EventControllerTests {
```
- 해당 테스트에 andDo()로 스니펫 작성
```java
    .andDo(document("create_event")) ...
```
결과 : ASCII Docs으로 이뤄진 html 문서
![img_3.png](img_3.png)

### 2. 구체적 사용
#### a. REST DOCS form 설정하기: RestDocsConfigure class test 폴더에..
설정 class
```java
    import org.springframework.boot.test.autoconfigure.restdocs.RestDocsMockMvcConfigurationCustomizer;
    import org.springframework.boot.test.context.TestConfiguration;
    import org.springframework.context.annotation.Bean;
    import org.springframework.restdocs.mockmvc.MockMvcRestDocumentationConfigurer;
    
    @TestConfiguration // test 관련 설정임을 알림
    public class RestDocsConfiguration {
        @Bean
        public RestDocsMockMvcConfigurationCustomizer restDocsMockMvcConfigurationCustomizer(){
            return new RestDocsMockMvcConfigurationCustomizer() {
                @Override
                public void customize(MockMvcRestDocumentationConfigurer configurer) {
                    //여기까지는 자동생성. configurer를 정의
                    configurer.operationPreprocessors() // 처리과정 정의
                        .withResponseDefaults(prettyPrint()) //response 결과를 보기 좋게
                        .withRequestDefaults(prettyPrint()); //request 표시를 보기 좋게
                }
            };
        }
    }
```
간단하게 람다식으로 표현(처음부터 그러면 좋겠지만.....)
```java
    @TestConfiguration // test 관련 설정임을 알림
    public class RestDocsConfiguration {
        @Bean
        public RestDocsMockMvcConfigurationCustomizer restDocsMockMvcConfigurationCustomizer(){
            return configurer -> configurer.operationPreprocessors() 
                    .withResponseDefaults(prettyPrint()) 
                    .withRequestDefaults(prettyPrint());
        }
    }
```
해당 test class에 import
```java
    @Test
    @Import(RestDocsConfiguration.class)
    void testName(){
            ...
    }
```
prettyPrint() 결과
![img_4.png](img_4.png)
이외에도 많은 프로세서가 있음: 필요에 따라 개인 공부

#### b. 링크, 필드, 헤더 문서화: API 문서 조각 만들기
```java
  @Test
  @DisplayName(value = "spring rest docs 문서 조각(스니펫) 만들기")
  void restDocsField() throws Exception{ 
      ...
```
- 요청 본문 문서화(기본) - 위의 내용 참조
- 응답 본문 문서화(기본) - 위의 내용 참조
  이 곳에서 document()로 지정한 이름으로 asciidocs가 연결되므로 이름 주의
```java
    ...
    .andDo(document("create_event");
```
- 링크 문서화
    * self, query, update
    * profile 링크(문서 완성 후 진행 예정)
```java
    ...
  .andDo(document("create_event",
      links(
          linkWithRel("self").description("Link to self"),
          linkWithRel("query-events").description("Link to query events"),
          linkWithRel("update-event").description("Link to update an existing event")
      ...
```
결과:

![img_5.png](img_5.png)

- 요청 헤더 문서화
```java
    requestHeaders(
        headerWithName(HttpHeaders.ACCEPT).description("header: accept 설정"),
        headerWithName(HttpHeaders.CONTENT_TYPE).description("header: contentType 설정"),
    )
```
- 요청 필드 문서화
```java
    requestFields(
          fieldWithPath("name").description("event name"),
          fieldWithPath("description").description("information of new event"),
```
- 응답 헤더 문서화
```java
    responseHeaders(
            headerWithName(HttpHeaders.LOCATION).description("address of event"),
            headerWithName(HttpHeaders.CONTENT_TYPE).description("contentType"+MediaTypes.HAL_JSON_VALUE)
    )
```
- 응답 필드 문서화
```java
    responseFields(
            fieldWithPath("id").description("event id"),
            fieldWithPath("name").description("event name"),
            ...
```
링크를 따로 문서화하는 경우 response에서 검증하지 않았다고 에러가 난다.
```markdown
      방법1: relexedResponseFields() 사용 
      방법2: fieldsWithPath("해당").ignored() 사용
      방법3: 뒤에 .optional() 삽입 
        fieldWithPath("_links.self.href").description("my href").optional()****
    
```
rest docs 문서에 반환될 값들의 타입을 강하게 테스트 하고 싶은 경우:
```java
  fieldWithPath("_links.self.href").type(JsonFieldType.STRING).description("my href"),
```
결과로 생성된 asciidocs

![img_6.png](img_6.png)

#### c. 만들어진 문서조각으로 문서(html) 빌드하기
pom.xml에 관련 플러그인 추가([사이트](https://docs.spring.io/spring-restdocs/docs/2.0.2.RELEASE/reference/html5/) 참조):
```xml
    <plugin>
        <artifactId>maven-resources-plugin</artifactId>
        <version>2.7</version>
        <executions>
          ...
        </executions>
    </plugin>
```
문서를 보관할 directory 생성
```markdown
    src/main/asciidoc/index.adoc

    https://gitlab.com/whiteship/natural 에서 같은 path에 있는 파일을 open raw
    그 내용 복붙후 수정
```
테스트 중 에러가 있는 경우 빌드가 진행되지 않으므로 예전 테스트중 에러가 발생하는 테스트의 경우 삭제
```markdown
    maven package 또는 intellij의 Maven 메뉴 > lifecycle > pacakge 실행
```
target 폴더에서 생성된 내용 확인
```markdown
    각 snippets과 통합 index.html 확인 가능(빌드된 파일 path)
```
#### d. 문서 완성 후 프로필 추가하기
```markdown
resource 처리하는 곳에서 추가
테스트 코드에 추가
테스트 코드의 document 처리부분에 추가
```