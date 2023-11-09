package com.backkeesun.inflearnrestapi.config;

import com.backkeesun.inflearnrestapi.account.Account;
import com.backkeesun.inflearnrestapi.account.AccountRole;
import com.backkeesun.inflearnrestapi.account.AccountService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class AppConfig {
    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public ApplicationRunner applicationRunner(){    // 서버가 시작할때 반드시 해야할 동작이 있는 경우 구현
        return new ApplicationRunner() {
            // Test용.. 기존사용자를 매번 만들경우
            @Autowired
            AccountService accountService;
            @Override
            public void run(ApplicationArguments args) throws Exception {
                Account testAccount = Account.builder()
                        .email("dream-ik89@naver.com")
                        .password("k1234")
                        .roles(Set.of(AccountRole.USER, AccountRole.ADMOIN))
                        .build();
                accountService.saveAccount(testAccount);
            }
        };
    }
}
