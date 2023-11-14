package com.backkeesun.inflearnrestapi.config;

import com.backkeesun.inflearnrestapi.account.Account;
import com.backkeesun.inflearnrestapi.account.AccountRole;
import com.backkeesun.inflearnrestapi.account.AccountService;
import com.backkeesun.inflearnrestapi.common.AppProperties;
import com.backkeesun.inflearnrestapi.common.WebMockControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthServerConfigTest extends WebMockControllerTest {
    @Autowired
    AccountService accountService;
    @Autowired
    AppProperties appProperties;
    @Test
    @DisplayName(value = "인증토큰(accessToken, refreshToken)을 발급받는다.")
    void getToken() throws Exception {
        //given
        //when
        ResultActions perform = this.mockMvc.perform(post("/oauth/token")//url은 자동처리
                .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))// request header 생성
                .param("username",appProperties.getUserUsername())//인증 정보 삽입
                .param("password",appProperties.getUserPassword())
                .param("grant_type","password")
        );//기본으로 제공될 handler
        // then
        perform
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists())
                .andExpect(jsonPath("refresh_token").isNotEmpty())
                .andExpect(jsonPath("token_type").value("bearer"))
                .andExpect(jsonPath("expires_in").isNumber())
                .andExpect(jsonPath("scope").value("read write"))
        ;
/*
 * [error 관련 검색내용]
    Token Store를 JWT 설정시 stackoverflow 오류가 발생하는 문제는 Spring Security 버전 문제가 아니였습니다.
    개발중인 프로젝트를 SpringBoot 2.0.x 도 다운그레이드 해도 동일한 오류가 발생하여
    JPA를 사용하고 있어서 OAuth 관련 테이블을 엔터티에서 사용한 연관관계 설정에 문제가 있어 발생한 문제 였습니다.
    Entity Class 를 삭제하고 OAuth 관련테이블을 Script 로 수동 생성후 해결되었습니다.
    JWT를 TokenStore로 사용하면 디버깅 해보니 oauth_client_details만 생성하면 됩니다.
    아마도 JWT Payload 에서 권한및 토큰의 만료기간등 가지고 있어서...
 */
    }

    private Account createUserData(String email, String password) {
        Account account = Account.builder()
                .email(email)
                .password(password)
                .roles(Set.of(AccountRole.USER, AccountRole.ADMOIN))
                .build();
        return this.accountService.saveAccount(account);
    }
}