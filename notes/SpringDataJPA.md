## B. Spring data JPA
### 1. org.springframework.data.domain.Pageable
라이브러리가 제공하는 Pageable class로 paging에 필요한 기본 정보를 parameter로 받을 수 있다.
#### a. controller에서 Pageable로 받기
```java
    @GetMapping
    //Pageable로 paging에 필요한 parameter를 받음(Spring data JPA가 제공)
    public ResponseEntity queryEvents(Pageable pageable){
        return ResponseEntity.ok(this.eventService.queryEvents(pageable));
    }
```
#### b. service에서 repository로 처리
findAll에서 pageable을 받아 Page 객체로 전달
```java
    public Page<Event> queryEvents(Pageable pageable){
        return eventRepository.findAll(pageable);
    }
```
#### c. page 관련 정보를 resource의 형태로 제공
이전페이지, 이후 페이지의 링크, 관련 정보를 resource로 전달
```java
    public ResponseEntity queryEvents(Pageable pageable, PagedResourcesAssembler<Event> pagedResourcesAssembler){
        Page<Event> page = this.eventService.queryEvents(pageable);
        //assembler 사용 > resource(지금은 EntityModel)
        PagedModel<EntityModel<Event>> pageEntityModel = pagedResourcesAssembler.toModel(page);
        return ResponseEntity.ok(pageEntityModel);
    }
```
#### d. response data
![img_8.png](img_8.png)

page 객체를 전달하면 pageable로 표시되고 링크는 전달되지 않는다.

#### e. 각각의 데이터(event)마다 접근할 수 있는 링크생성(HATEOAS 만족)
```java
    @GetMapping
    public ResponseEntity queryEvents(Pageable pageable, PagedResourcesAssembler<Event> pagedResourcesAssembler){
        Page<Event> page = this.eventService.queryEvents(pageable);
        //각 event마다 eventResource 적용
        PagedModel<EntityModel<Event>> pageEntityModel = pagedResourcesAssembler.toModel(page,e -> new EventResource(e));
        //var pageEntityModel = pagedResourcesAssembler.toModel(page, EventResource::new);로 축약 가능
        return ResponseEntity.ok(pageEntityModel);
    }
```
- test
```java
  .andExpect(jsonPath("page").exists()) //paging data
  .andExpect(jsonPath("_links").exists()) //paging link
  .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())//개별 link
```
- response
  ![img_9.png](img_9.png)

#### f. 자체 프로필 문서 작성 후 profile 링크 추가
entityModel(resource)이 만들어졌다면 바로 링크 추가하면 됨
```java
  pageEntityModel.add(Link.of("/docs/index.html#resources-query-events").withRel("profile"));
```
하위요소가 있는 경우의 문서화(array 등)
```java
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
```