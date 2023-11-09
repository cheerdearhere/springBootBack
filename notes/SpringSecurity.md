## A. 스프링 시큐리티
### 1. security 종류
| 종류       | 내용                            |
|----------|-------------------------------|
| 웹시큐리티    | 서블릿 기반(spring-webmvc)         |
|          | 웹플럭스기반(spring-webflux)/5.0 이후 |
| 메소드 시큐리티 | 웹과 관계없이 기능기반 시큐리티             |
* 모두 Security Interceptor 사용
![img.png](https://img1.daumcdn.net/thumb/R1920x0/?fname=http%3A%2F%2Fcfile23.uf.tistory.com%2Fimage%2F99A7223C5B6B29F003F5F0)
가지고 있는 정보(resource)에 접근을 허가할지 결정
이번 프로젝트에서는 주로 웹 시큐리티 예정

### 2. 구현방식(간략한 설명)
![img.png](시큐리티인터셉터 종류.png)
한 스레드에서 처리하는 경우 [ThreadLocal](https://docs.oracle.com/javase/8/docs/api/java/lang/ThreadLocal.html) 구현체로 구현된 SecurityContextHolder에서 인증을 체크

#### a. UserDetailsService(유저 정보 검색)와 PasswordEncoder(비밀번호 매칭 체크)를 사용해 인증(Authentication) 진행
#### b. securityContextHolder에 인증 정보 보관
#### c. 권한(Authorization) 확인 : 유저 정보의 role 
#### d. 사용 인가(AccessDecison) 결정
## B. [시큐리티](https://docs.spring.io/spring-security/reference/getting-spring-security.html)

## C. 적용하기
### 3. 의존성 추가
```xml
<!-- https://mvnrepository.com/artifact/org.springframework.security.oauth.boot/spring-security-oauth2-autoconfigure -->
<dependency>
    <groupId>org.springframework.security.oauth.boot</groupId>
    <artifactId>spring-security-oauth2-autoconfigure</artifactId>
    <version>2.6.8</version>
</dependency>
```

=> 의존성을 추가한 순간 SpringBoot의 모든 테스트는 fail. 이에대한 설정이 필요하다.

만약 로그로 security 관련사항이 보고싶다면 application.properties/yml에 로그수준 지정
```yaml
logging:
  level:
    org:
      springframework:
        security: DEBUG
```
### 4. 기본설정(필수!!)
#### a. 권한 배치 분류
- 로그인 없이 접근 가능: permitAll
- 비로그인만 접근 가능: isAnonymous
- 권한이 있는 사람만 : hasRole/hasAuthority or hasAnyRole/hasAnyAuthority
- 특정 IP : hasIpAddress
- 로그인한 사용자만 : rememberMe

  ...
ex)
```markdown
시큐리티 필터를 적용하기 않음...
/docs/index.html

로그인 없이 접근 가능
GET /api/events
GET /api/events/{id}

로그인 해야 접근 가능
나머지 다...
POST /api/events
PUT /api/events/{id{
...
```

#### b. 권한에 따른 SpringSecurity OAuth2.0 설정
```markdown
스프링 시큐리티 OAuth 2.0
- AuthorizationServer: OAuth2 토큰 발행(/oauth/token) 및 토큰 인증(/oauth/authorize)
    - Oder 0 (리소스 서버 보다 우선 순위가 높다.)
- ResourceServer: 리소스 요청 인증 처리 (OAuth 2 토큰 검사)
    - Oder 3 (이 값은 현재 고칠 수 없음)
```

#### c. 공통 설정(만료된 내용 주의)
설정 파일 만들기

legacy project에서는 */webapp/WEB-INF/spring*에서 .xml 파일로 관리함([예시 파일](security-context.xml)).

SpringBoot에서 Autoconfigurer를 사용했을 때( )
```java

@Configuration
@EnableWebSecurity //@EnableGlobalMethodSecurity, @EnableWebFluxSecurity도 가능
public class SecurityConfig extends WebSecurityConfigurerAdapter {//TODO 옛버전...만료됨 수정필요
    //사용할 의존성 주입
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;
    //이곳에 정의한 설정만 적용
}
```

설정용 bean 설정
* legacy
  [web.xml](web.xml) 파일에 적용
* boot
  *Application.class 또느 Config.class(@Configuration)에 bean으로 설정
```java
@Configuration
public class AppConfig {
    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder(); //다양한 비밀번호 인코더
    }
}
```
인코더 참조
![img.png](SpringframeworkSecurityPasswordEncoder.png)

#### d. 토큰 관리
예제용으로 TokenStore를 쓰지만 OAuth 버전이 올라가면서 deprecated 처리됨. 
우선 교육용으로 참고만하고 이후에 레디스 또는 다른 방법 고민 필요
- [2018 네이버 OAuth2.0 영상](https://tv.naver.com/v/4012597)
- [SpringSecurity Docs](https://docs.spring.io/spring-security/reference/getting-spring-security.html)
- [레디스로 구현한 블로그](https://velog.io/@minwest/Spring-Security-jwt%EB%A1%9C-%EB%A1%9C%EA%B7%B8%EC%9D%B8%EB%A1%9C%EA%B7%B8%EC%95%84%EC%9B%83-%EA%B5%AC%ED%98%84%ED%95%98%EA%B8%B0)

TokenStore: InMemoryTokenStore - 토큰을 보관할 Bean 생성
```java
    @Bean
    TokenStore tokenStore(){
        return new InMemoryTokenStore();
    }
```
AuthenticationManagerBean: 토큰 발행, 관리할 서버를 Bean으로 노툴
```java
    @Bean
    @Override
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManagerBean();
    }
```

#### e. 설정 진행 : configure
userDetailsService & passwordEncoder(AuthenticationManagerBuidler auth)
```java
    @Override
    protected void configure(AuthenticationManagerBuilder auth)throws Exception{
        auth.userDetailsService(accountService)     //사용할 서비스
                .passwordEncoder(passwordEncoder);  // 사용할 인코더
    }
```
#### f. 기본설정
security package 외부: docs와 기본적인 정적 자원들을 호출할때 security filter 사용 x
```java
    @Override
    public void configure(WebSecurity web) throws Exception{
        web.ignoring().mvcMatchers("/docs/index.html");// 안내
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());//springBoot제공 : 모든 정적 resources들의 기본위치
    }
```
security package 내부 : HttpSecurity http에서 anonymous 처리(필터는 처리함)

security filter는 진행하지만 보안처리는 안함.

처리는 안한다해도 결국 서버를 통과하므로 굳이 보안 처리가 필요없다면 외부에서 처리하는 filter 무시가 권장
```java
    @Override
    protected void configure(HttpSecurity http)throws Exception{
        http.authorizeRequests()
                .mvcMatchers("/docs/index.html").anonymous()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).anonymous()
        ;
    }
```
### 5. Tips...
#### a. 권한을 처리할때
Set을 SimpleGrantedAutority로 변환
```java
//UserDetails를 반환할때
        return new User(account.getEmail(),account.getPassword(),setAuthorities(account.getRoles()));

//변환 method 예시
    private Collection<? extends GrantedAuthority> setAuthorities(Set<AccountRole> roles) {
        return roles.stream().map(r->new SimpleGrantedAuthority("ROLE_"+r.name())).collect(Collectors.toSet());
    }
```
