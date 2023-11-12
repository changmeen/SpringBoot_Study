package rcm.rcmarket.config.token;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rcm.rcmarket.handler.JwtHandler;

// Configuration이란 프로그램의 동작을 결정짓는 설정, 환경, 또는 구성 요소들의
// 모음을 가리킨다. 시스템이 어떻게 동작해야 하는지를 결정하는 설정 정보를 포함한다.
@Configuration
@RequiredArgsConstructor
public class TokenConfig {

    private final JwtHandler jwtHandler;

    @Bean
    public TokenHelper accessTokenHelper(
            @Value("${jwt.key.access}") String key,
            @Value("${jwt.max-age.access}") long maxAgeSeconds) {
        return new TokenHelper(jwtHandler, key, maxAgeSeconds);
    }

    @Bean
    public TokenHelper refreshTokenHelper(
            @Value("${jwt.key.refresh}") String key,
            @Value("${jwt.max-age.refresh}") long maxAgeSeconds) {
        return new TokenHelper(jwtHandler, key, maxAgeSeconds);
    }
}
