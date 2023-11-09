package com.backkeesun.inflearnrestapi.account;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AccountServiceTest {
//mockmvc는 사용안함
    @Autowired
    AccountService accountService;
    @Autowired
    AccountRepository accountRepository;


    @Test
    @DisplayName(value = "이름으로 유저정보 찾기")
    void findByUsername(){
        //given
        String username = "aaa@bbb.com";
        String password = "username";
        Account account = createUserData(username,password);
        this.accountRepository.save(account);
        //when
        UserDetailsService userDetailsService = accountService;
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        //then
        assertThat(userDetails.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName(value = "찾는 유저가 없는 경우")
    void notFoundUsername(){
        String username = "known@Account.com";
        //에러타입 확인
        assertThrows(UsernameNotFoundException.class,()->this.accountService.loadUserByUsername(username));

        // try-catch로 에러 메세지 점검하기
        try{
            this.accountService.loadUserByUsername(username);
            fail();
        }catch (UsernameNotFoundException ue){
            assertThat(ue.getMessage()).containsSequence(username);
        }


    }

//     Junit4에서만
//    @Rule
//    public ExpectedException expectedException = ExpectedException.name();
//    @Test
//    void test(){
//        //given
//        String username = "known@Account.com";
//        //expected
//        expectedException.expect(UsernameNotFoundException.class);
//        expectedException.expectMessage(Matchers.containsString(username));
//        //when
//        this.accountService.loadUserByUsername(username);
//    }


    private Account createUserData(String email, String password) {
        return Account.builder()
                .email(email)
                .password(password)
                .roles(Set.of(AccountRole.USER, AccountRole.ADMOIN))
                .build();

    }
}