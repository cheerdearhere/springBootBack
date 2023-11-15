package com.backkeesun.inflearnrestapi.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

//@Configuration
//@EnableJpaRepositories(
//        basePackages = "com.backkeesun.inflearnrestapi",
//        entityManagerFactoryRef = "entityManagerFactory"
//)
//@MapperScan(
//        basePackages = {"com.backkeesun.inflearnrestapi.mapper"},
//        sqlSessionFactoryRef = "sqlSessionFactory",
//        sqlSessionTemplateRef = "sqlSessionTemplate"
//)
//public class DatabaseConnectionConfig {
//    /*=== Database context config ===*/
//    /**
//     * hikari 기본 설정
//     */
//    @Bean(name="hikariConfig")
//    @ConfigurationProperties(prefix="spring.datasource.hikari")
//    public HikariConfig hikariConfig(){
//        return new HikariConfig();
//    }
//
//    /**
//     *  datasource for mybatis
//     * @param hikariConfig
//     */
//    @Bean(name="dataSource")
//    public HikariDataSource dataSource(@Qualifier("hikariConfig") HikariConfig hikariConfig){
//        return new HikariDataSource(hikariConfig);
//    }
//
//    /**
//     * datasource for jdbc
//     * @param hikariConfig
//     */
//    @Bean(name = "jpaDataSource")
//    public HikariDataSource jdbcDataSource(@Qualifier("hikariConfig") HikariConfig hikariConfig){
//        return new HikariDataSource(hikariConfig);
//    }
//
//    /*=== SqlSession config ===*/
//    /**
//     * sqlSessionFactoryBean
//     * @param dataSource
//     */
//    @Bean(name = "sqlSessionFactory")
//    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource")DataSource dataSource)throws Exception{
//        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
//        sqlSessionFactoryBean.setDataSource(dataSource);
//        //resultType 선언시 공통 package 생략
//        sqlSessionFactoryBean.setTypeAliasesPackage("com.backkeesun.inflearnrestapi.dto");
//        //mapper.xml 위치
//        sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResource("classpath:mybatis/**/**/*.xml"));
//        //mybatis 설정 파일 위치 지정
////        sqlSessionFactoryBean.setConfigLocation(new PathMatchingResourcePatternResolver().getResource("classpath:mybatis/mybatis-config.xml"));
//        //설정을 직접 진행하는 경우
//        org.apache.ibatis.session.Configuration sessionFactoryConfiguration = Objects.requireNonNull(sqlSessionFactoryBean.getObject()).getConfiguration();
//        sessionFactoryConfiguration.setMapUnderscoreToCamelCase(true);
//        sessionFactoryConfiguration.setCallSettersOnNulls(true);
//        sessionFactoryConfiguration.setJdbcTypeForNull(JdbcType.NULL);
//        return sqlSessionFactoryBean.getObject();
//    }
//
//    /**
//     * sqlSessionTemplate
//     * @param sqlSessionFactory
//     */
//    @Bean(name = "sqlSessionTemplate")
//    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
//        return new SqlSessionTemplate(sqlSessionFactory);
//    }
//    /*=== JPA EntityManager config ===*/
//    @Bean
//    public EntityManagerFactory entityManagerFactory(@Qualifier("jpaDataSource") DataSource dataSource){
//        LocalContainerEntityManagerFactoryBean jpaEntityFactory = new LocalContainerEntityManagerFactoryBean();
//        jpaEntityFactory.setPackagesToScan("com.backkeesun.inflearnrestapi");
//        jpaEntityFactory.setDataSource(dataSource);
//        jpaEntityFactory.setPersistenceUnitName("Postgres");
//
//        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
//        vendorAdapter .setShowSql(true);//application.yml에서 지정한 sql 보여주기 설정
//        jpaEntityFactory.setJpaVendorAdapter(vendorAdapter);
//
//        Map<String, Object> properties = new HashMap<>(); //기타 hiberate 관련 설정
//        properties.put("hibernate.hbm2ddl.auto", "none");
//        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
//        properties.put("hibernate.format_sql", true);
//        properties.put("hibernate.show_sql", false);  // sql은 log4j로 출력 org.hibernate.SQL=DEBUG
//        properties.put("hibernate.globally_quoted_identifiers", true);  // 예약어 컬럼명 사용 허용
//
//        jpaEntityFactory.setJpaPropertyMap(properties);
//        jpaEntityFactory.afterPropertiesSet();//설정 끝
//
//        return jpaEntityFactory.getObject();
//    }
//
//    /*=== Transaction config ===*/
//    //https://velog.io/@maxxyoung/Spring-MyBatis%EC%99%80-JPA-%EB%8F%99%EC%8B%9C-%EC%A0%81%EC%9A%A9%EA%B3%BC-%ED%8A%B8%EB%9E%9C%EC%9E%AD%EC%85%98
//    @Bean(name = "txManager")
//    public PlatformTransactionManager txManager(@Qualifier("dataSource") DataSource dataSource){
//        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);
//        dataSourceTransactionManager.setNestedTransactionAllowed(true);
//        return dataSourceTransactionManager;
//    }
//    @Bean(name = "jpaTxManager")
//    public PlatformTransactionManager jpaTxManager(EntityManagerFactory entityManagerFactory){
//        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
//        jpaTransactionManager.setEntityManagerFactory(entityManagerFactory);
//        return jpaTransactionManager;
//    }
//}
