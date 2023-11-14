package com.backkeesun.inflearnrestapi.config;

import com.backkeesun.inflearnrestapi.account.AccountRole;
import com.backkeesun.inflearnrestapi.account.AccountService;
import com.backkeesun.inflearnrestapi.common.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

@Configuration
@EnableAuthorizationServer
@RequiredArgsConstructor
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {//"/oauth/token" url은 자동처리
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AccountService accountService;
    private final TokenStore tokenStore;
    private final AppProperties appProperties;

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.passwordEncoder(passwordEncoder);//client_secret을 처리할때
    }
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()//test용 실무에서는 .jdbc로 db처리
                .withClient(appProperties.getClientId())
                .authorizedGrantTypes("password","refresh_token")
                .scopes("read","write")
                .secret(passwordEncoder.encode(appProperties.getClientSecret()))
                //  토큰 만료시간(sec)
                .accessTokenValiditySeconds(30 * 60)
                .refreshTokenValiditySeconds(60 * 60)
        ;
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authenticationManager)//authentication처리때 사용할 이전에 등록한 bean 등록
                .userDetailsService(accountService)
                .tokenStore(tokenStore);
    }
}
