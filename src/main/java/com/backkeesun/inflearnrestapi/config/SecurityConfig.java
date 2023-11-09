package com.backkeesun.inflearnrestapi.config;

import com.backkeesun.inflearnrestapi.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

@Configuration
@EnableWebSecurity //@EnableGlobalMethodSecurity, @EnableWebFluxSecurity도 가능
//@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter { //이곳에 정의한 설정만 적용
    @Autowired
    AccountService accountService;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Bean //TokenStore: InMemoryTokenStore - 토큰을 보관할 Bean 생성
    TokenStore tokenStore(){
        return new InMemoryTokenStore();
    }

    @Bean//AuthenticationManagerBean: 토큰 발행, 관리할 서버를 Bean으로 노툴
    @Override
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManagerBean();
    }
    /* configure(AuthenticationManagerBuidler auth)로 설정 추가 */

    /**
     * 인증 처리 대상 서비스, 이때 사용할 인코더 지정
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth)throws Exception{
        auth.userDetailsService(accountService)     //사용할 서비스
                .passwordEncoder(passwordEncoder);  // 사용할 인코더
    }

    /**
     * security filter를 적용 안함
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception{
        web.ignoring().mvcMatchers("/docs/index.html");// 안내
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());//springBoot제공 : 모든 정적 resources들의 기본위치
    }

    /**
     * security filter는 진행하지만 보안처리는 안함.
     * 처리는 안한다해도 결국 서버를 통과하므로 굳이 보안 처리가 필요없다면 외부에서 처리하는 filter 무시가 권장
     * @param http
     * @throws Exception
     */
//    @Override
//    protected void configure(HttpSecurity http)throws Exception{
//        http.authorizeRequests()
//                .mvcMatchers("/docs/index.html").anonymous()
//                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).anonymous()
//        ;
//    }

    @Override
    protected void configure(HttpSecurity http)throws Exception{
        http.anonymous()//비인증 접근 허용
                .and()//설정 병렬로 지정
            .formLogin()//form login 설정
//                .loginPage()//로그인 페이지 url
//                .passwordParameter()//파라미터명
//                .usernameParameter()
//                .failureForwardUrl()//실패시 이동시킬 url
//                .successForwardUrl()//성공시 이동시킬 url
                //안해도 자동처리됨. 테스트용에서는 처리 안해도 기본페이지 제공
                .and()
            .authorizeRequests()//요청에 대한 처리지정
                .mvcMatchers(HttpMethod.GET,"/api/**").anonymous() // 해당 /api/를 포함한 Get method 요청은 비로그인으로 처리
                .anyRequest().authenticated();//그 외 나머지 요청은 다 요청
    }
}
