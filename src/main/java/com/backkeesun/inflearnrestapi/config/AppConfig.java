package com.backkeesun.inflearnrestapi.config;

import com.backkeesun.inflearnrestapi.account.Account;
import com.backkeesun.inflearnrestapi.account.AccountRepository;
import com.backkeesun.inflearnrestapi.account.AccountRole;
import com.backkeesun.inflearnrestapi.account.AccountService;
import com.backkeesun.inflearnrestapi.common.AppProperties;
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
            // Test용.. 단순 테스트용일뿐... 반복적으로 테스트가 일어날 경우 에러 발생
            @Autowired
            AccountService accountService;

            @Autowired
            AppProperties appProperties;
            @Override
            public void run(ApplicationArguments args) throws Exception {
                Account userAccount = Account.builder()
                        .email(appProperties.getUserUsername())
                        .password(appProperties.getUserPassword())
                        .roles(Set.of(AccountRole.USER))
                        .build();
                accountService.saveAccount(userAccount);
                Account adminAccount = Account.builder()
                        .email(appProperties.getAdminUsername())
                        .password(appProperties.getAdminPassword())
                        .roles(Set.of(AccountRole.USER, AccountRole.ADMOIN))
                        .build();
                accountService.saveAccount(adminAccount);
            }
        };
    }
}
