package com.backkeesun.inflearnrestapi.jwtFilter;

import com.backkeesun.inflearnrestapi.account.Account;
import com.backkeesun.inflearnrestapi.account.AccountService;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {
    /*=== 토큰 직접 발행 시도 1 ===*/
    @Value("${security.myKey}")
    private String SECRET_KEY;

    private final long TOKEN_VALID_TIME = 30 * 60 * 1000L;
    private final AccountService accountService;

    @PostConstruct//초기화
    protected void init(){
        SECRET_KEY = Base64.getEncoder().encodeToString(SECRET_KEY.getBytes());
    }

    public String createToken(String adminPk){
        //adminPk === userID
        Claims claims = Jwts.claims().setSubject(adminPk);//payload 정보단위
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims) // payload 시작
                .setIssuedAt(now) // 발행 시간
                .setExpiration(new Date(now.getTime()+TOKEN_VALID_TIME)) //만료 시간
                .signWith(SignatureAlgorithm.ES256,SECRET_KEY) // 암호화 알고리즘, secret key
                .compact();
    }

    public Authentication getAuthentication(String token){
        //SpringSecurity를 구현해서 사용한경우 해당 method를 재정의: public class AdminLoginDto implements UserDetails
        UserDetails userDetails = accountService.loadUserByUsername(this.getAdminPk(token));
        return new UsernamePasswordAuthenticationToken(userDetails.getUsername(),userDetails.getPassword());
    }

    public String getAdminPk(String token){
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody().getSubject();//payload에서 꺼내기
    }

    public boolean validateToken(String token){//유효성 체크
        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token);
            return !claimsJws.getBody().getExpiration().before(new Date());//만료시간이 현재보다 이전이면 false
        }catch (Exception e){
            log.debug(e.getMessage());
            return false;
        }
    }
    
    public String resolveToken(HttpServletRequest request){//토큰 가져오기
        return request.getHeader("Authorization");
    }
}
