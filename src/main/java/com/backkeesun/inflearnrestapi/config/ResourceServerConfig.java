package com.backkeesun.inflearnrestapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
    @Override //resource의 id를 설정
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId("event");//설정하지 않은 부분은 기본설정으로 유지됨
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.anonymous()
                .and()
//                .csrf().disable()
            .authorizeRequests()
                .mvcMatchers(HttpMethod.GET, "/api/**").anonymous()
                .anyRequest().authenticated()
                .and()
            .exceptionHandling()//에러가난 경우 처리를 OAuth2의 핸들러에 맡김
                .accessDeniedHandler(new OAuth2AccessDeniedHandler());
    }
}
