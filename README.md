# springBootBack
from inflearn(https://www.inflearn.com/course/spring_rest-api)
### using Dependencies
java 11, spring boot 2.x.x(maven), postgresql(with docker), h2(test), JPA, Lombok, restdocs

# I. 강의 개요
2017년 네이버의 Deview 컨퍼런스에서 이응준님의 발표에서 영감을 받음. 현재 REST API라 불리고 쓰이는 것들이 과연 RestFUL한가에 대한 의문. 진짜 REST API 만들기
## A. 로이 필딩이 정의한 REST: REpresentational State Transfer
인터넷 상의 시스템 간의 상호 운용성(interoperability)을 제공하는 방법중 하나 
```markdown
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
```
특히 self-descriptive messages와 HATEOAS가 무시되고 있음
## B. 이에 대한 대안
1. 미디어 타입을 구체적으로 정의 > IANA 등록 > 해당 타입을 resource로 전달할때 Content-type 헤더에 기록: 브라우저들마다 스펙 지원이 달라 처리가 안될 수 있음
2. profile 링크 헤더 추가 : 브라우저들마다 스펙 지원이 달라 처리가 안될 수 있음
3. 2에 대한 대안 : 헤더의 HAL에 링크 데이터로 profile 링크 추가

참고: https://docs.github.com/en/free-pro-team@latest/rest/issues/issues?apiVersion=2022-11-28#update-an-issue

# II. Spring boot 관련
[ Springboot 문서로 이동](./notes/SpringBoot.md)

# III. 비즈니스 로직 관련
## A. Event API 비즈니스 로직
### 1. parameters
```markdown
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
```
### 2. price 로직
```markdown
    base    max     logic
    0       100     선착순
    0       0       무료
    100     0       무제한 경매
    100     100     선착순 경매(순위꿘)
```
### 3. response data
```markdown
    id
    name
        ...
    limitOfEnrollment
    eventStatus
    offline
    free
    _links
        profile
        self
        publish
            ...
```
## B. 계정 관리
### 1. account data
```markdown
- id
- email
- password
- roles: ADMIN or USER
```
### 2. 권한 처리
[ Spring security ](./notes/SpringSecurity.md)사용
# IV. 테스트 관련
[TDD 문서로 이동](./notes/TDD.md)
# V. 유용한 라이브러리
## A. [SpringHATEOAS](./notes/SpringHATEOAS.md)

## B. [SpringDataJPA](./notes/SpringDataJPA.md)

## C. [SpringRESTDocs](./notes/SpringRestDocs.md)

## D. [SpringSecurity](./notes/SpringSecurity.md)

# VI. 기타
## A. DB 관련
### 1. Postgresql Database 직접 설치
docker를 통해 설치하고 JPA를 사용하는경우 필요 x
```postgresql
  CREATE USER {ID} PASSWORD '{PASSWORD}' + 권한(테스트는 SUPERUSER);
  CREATE DATABASE {DATABASE_TITLE} OWNER {OWNER_ID};
```
### 2. docker를 사용해 컨테이너 실행
[dockerScript.md](./dockerScript.md)  참고