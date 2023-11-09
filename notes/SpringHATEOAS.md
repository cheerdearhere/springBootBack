## A. Spring HATEOAS library
스프링에서 RestFul Api을 더 잘 구성하도록 돕는 라이브러리.
Spring boot의 도움으로 별도의 어노테이션이나 설정 없이 사용가능

```asciidoc
    HATEOAS: 수신된 response만으로도 app과 client 사이의 동작을 동적으로 소통할 수 있는 상태. (rest api)
```
[HATEOAS 문서](https://docs.spring.io/spring-hateoas/docs/current/reference/html/)

[이 프로젝트에서 쓰고있는 버전](https://docs.spring.io/spring-hateoas/docs/1.0.1.RELEASE/reference/html/)

### 1. 핵심 기능
```markdown
a. 링크를 만드는 기능 
    - 문자열로 생성
    - 생성 메서드(LinkTo method)
b. 리소스를 만드는 기능
    - resources data + link
c. 링크를 찾아주는 기능
    - Traberison
    - LinkDiscovers
```
### 2. 링크에 들어가야할 정보
```markdown
- HREF  : link
- REL   : 현재 정보와의 관계 - self, profile, transfer, update, query-event, withdraw, ... etc.
```
### 3. 버전에 따라 클래스 명이 달라짐을 주의
![img_2.png](img_2.png)

### 4. resource 사용하기
#### a. 방법 1 :  RepresntationModel(구 ResourceSupport)
RepresentationModel(구 ResourceSupport) 사용: Entity를 eventResource로 만들때 적용
```java
    public class EventResource extends RepresentationModel {
        @JsonUnwrapped
        private Event event;
        public EventResource (Event event){
            this.event = event;
            eventResource.add(linkTo(EventController.class).withRel("query-events"));
            eventResource.add(selfLinkBuilder.withSelfRel());)
            eventResource.add(selfLinkBuilder.withRel("update-event"));//HttpMethod 차이일뿐 링크는 같을 수 있음)
        }
        public Event getEvent() {
            return event;
        }
    }
```
* @JsonUnwrapped : entity를 분해해서 내부 내용을 입력하게함(별도로 setter과정 생략)

controller에서 사용
```java
//        ControllerLinkBuilder
  WebMvcLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(newEvent.getId());
  URI createdUri = selfLinkBuilder.toUri();
  EventResource eventResource = new EventResource(event);
  //new Link("url")도 가능:         add(new Link("http://localhost:8181/api/events/"+event.getId()));
  //데이터가 변경되면 하나하나 직접 변경해야흐므로 아래를 권장
  eventResource.add(linkTo(EventController.class).withRel("query-events"));
  eventResource.add(selfLinkBuilder.withSelfRel());)
  eventResource.add(selfLinkBuilder.withRel("update-event"));//HttpMethod 차이일뿐 링크는 같을 수 있음)
  return ResponseEntity.created(createdUri).body(eventResource);
```
#### b. 방법 2 : EntityModel(구 Resource) 사용하기
RepresentationModel와 달리 EntityModel은 별도로 JsonUnWrappted annotation을 사용하지 않아도 매핑됨.
```java
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import java.util.Arrays;

public class EventResource extends EntityModel<Event> {
    public EventResource(Event content, Link... links){
        super(content, Arrays.asList(links));
        // 컨트롤러가 아닌 resource의 생성 단계에서 추가도 가능(권장)
        
    }
}
```