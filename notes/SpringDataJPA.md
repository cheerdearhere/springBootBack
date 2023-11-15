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

### 2. JPA에 mybatis 적용
#### a. dependency
```xml
<!-- https://mvnrepository.com/artifact/org.mybatis.spring.boot/mybatis-spring-boot-starter -->
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>2.3.1</version>
</dependency>
```
#### b. config class
```java
@Configuration
@MapperScan(basePackages = {"com.backkeesun.inflearnrestapi.mapper"}, sqlSessionFactoryRef = "sqlSessionFactory", sqlSessionTemplateRef = "sqlSessionTemplate")
public class MyBatisConfig {
    @Bean
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource")DataSource dataSource)throws Exception{
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        //resultType선언시 공통 package 생략
        sqlSessionFactoryBean.setTypeAliasesPackage("com.backkeesun.inflearnrestapi.dto");
        //mybatis 설정 파일 위치 지정
      sqlSessionFactoryBean.setConfigLocation(new PathMatchingResourcePatternResolver().getResource("classpath:mybatis/mybatis-config.xml"));
      //mapper.xml 위치
      sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResource("classpath:mybatis/mapper/**/*.xml"));
        return sqlSessionFactoryBean.getObject();
    }
    @Bean
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception{
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
```
#### c. mybatis 설정
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN" "http://mybatis.org/dtd/mybatis-3-config.dtd">

<configuration>
    <settings>
        <!--    camelCase to under_score    -->
        <setting name="mapUnderscoreToCamelCase" value="true"/>
        <!--    not null field    -->
        <setting name="callSetterOnNulls" value="true"/>
        <!--    accept null parameter    -->
        <setting name="jdbcTypeForNull" value="NULL"/>
    </settings>
</configuration>
```
#### d. mapper.class-mapper.xml 연결
mapper package
```java
@Repository
public interface AccountMapper {
    public List<Account> findUserList() throws Exception;
}
```
mapper resources
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.backkeesun.inflearnrestapi.mapper.AccountMapper"><!--  mapper 연결  -->
    <select id="findUserList" resultType="AccountListDto">
        SELECT
            ACCOUNT_ID
        ,   EMAIL
        ,   ROLES
        FROM
            ACCOUNT
        LIMIT 0, 10 
        ORDER BY ACCOUNT_ID DESC
    </select>
</mapper>
```
dto 등 필요한건 만들어야겠지?

### 3. JPA와 mybatis 적용시 config 파일을 따로 안만드는 경우
application.properties/yml이나 main method가 있는 실행파일에서 해도됨.
직접 설정할 경우 참조

hikari에서 값 가져오기
```java
@Configuration
@EnableJpaRepositories(
        basePackages = "com.backkeesun.inflearnrestapi",
        entityManagerFactoryRef = "entityManagerFactory"
)
@MapperScan(
        basePackages = {"com.backkeesun.inflearnrestapi.mapper"},
        sqlSessionFactoryRef = "sqlSessionFactory",
        sqlSessionTemplateRef = "sqlSessionTemplate"
)
public class DatabaseConnectionConfig {
    /*=== Database context config ===*/
    /**
     * hikari 기본 설정
     */
    @Bean(name="hikariConfig")
    @ConfigurationProperties(prefix="spring.datasource.hikari")
    public HikariConfig hikariConfig(){
        return new HikariConfig();
    }
    /**
     *  datasource for mybatis
     * @param hikariConfig
     */
    @Bean(name="dataSource")
    public HikariDataSource dataSource(@Qualifier("hikariConfig") HikariConfig hikariConfig){
        return new HikariDataSource(hikariConfig);
    }
    /**
     * datasource for jdbc
     * @param hikariConfig
     */
    @Bean(name = "jpaDataSource")
    public HikariDataSource jdbcDataSource(@Qualifier("hikariConfig") HikariConfig hikariConfig){
        return new HikariDataSource(hikariConfig);
    }
```
SqlSession 관련 
```java
    /*=== SqlSession config ===*/
    /**
     * sqlSessionFactoryBean
     * @param dataSource
     */
    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource")DataSource dataSource)throws Exception{
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        //resultType 선언시 공통 package 생략
        sqlSessionFactoryBean.setTypeAliasesPackage("com.backkeesun.inflearnrestapi.dto");
        //mapper.xml 위치
        sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResource("classpath:mybatis/**/**/*.xml"));
        //mybatis 설정 파일 위치 지정
//        sqlSessionFactoryBean.setConfigLocation(new PathMatchingResourcePatternResolver().getResource("classpath:mybatis/mybatis-config.xml"));
        //설정을 직접 진행하는 경우
        org.apache.ibatis.session.Configuration sessionFactoryConfiguration = Objects.requireNonNull(sqlSessionFactoryBean.getObject()).getConfiguration();
        sessionFactoryConfiguration.setMapUnderscoreToCamelCase(true);
        sessionFactoryConfiguration.setCallSettersOnNulls(true);
        sessionFactoryConfiguration.setJdbcTypeForNull(JdbcType.NULL);
        return sqlSessionFactoryBean.getObject();
    }
    /**
     * sqlSessionTemplate
     * @param sqlSessionFactory
     */
    @Bean(name = "sqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
```
datasource 설정하기
```java
    /*=== JPA EntityManager config ===*/
    @Bean
    public EntityManagerFactory entityManagerFactory(@Qualifier("jpaDataSource") DataSource dataSource){
        LocalContainerEntityManagerFactoryBean jpaEntityFactory = new LocalContainerEntityManagerFactoryBean();
        jpaEntityFactory.setPackagesToScan("com.backkeesun.inflearnrestapi");
        jpaEntityFactory.setDataSource(dataSource);
        jpaEntityFactory.setPersistenceUnitName("Postgres");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter .setShowSql(true);//application.yml에서 지정한 sql 보여주기 설정
        jpaEntityFactory.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>(); //기타 hiberate 관련 설정
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
        properties.put("hibernate.format_sql", true);
        properties.put("hibernate.show_sql", false);  // sql은 log4j로 출력 org.hibernate.SQL=DEBUG
        properties.put("hibernate.globally_quoted_identifiers", true);  // 예약어 컬럼명 사용 허용

        jpaEntityFactory.setJpaPropertyMap(properties);
        jpaEntityFactory.afterPropertiesSet();//설정 끝

        return jpaEntityFactory.getObject();
    }
```
[Spring boot는 transaction따로 처리 안해도 됨](https://velog.io/@maxxyoung/Spring-MyBatis%EC%99%80-JPA-%EB%8F%99%EC%8B%9C-%EC%A0%81%EC%9A%A9%EA%B3%BC-%ED%8A%B8%EB%9E%9C%EC%9E%AD%EC%85%98)
```java
    /*=== Transaction config ===*/
    @Bean(name = "txManager")
    public PlatformTransactionManager txManager(@Qualifier("dataSource") DataSource dataSource){
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);
        dataSourceTransactionManager.setNestedTransactionAllowed(true);
        return dataSourceTransactionManager;
    }
    @Bean(name = "jpaTxManager")
    public PlatformTransactionManager jpaTxManager(EntityManagerFactory entityManagerFactory){
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(entityManagerFactory);
        return jpaTransactionManager;
    }
}
```