package rcm.rcmarket.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
    }
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()  // http basic 인증 방식을 비 활성화
                .formLogin().disable()  // form login 비 활성화
                .csrf().disable()   // csrf 관련 설정 비 활성화
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    // 세션을 유지 하지 않도록 설정 (disable로 설정해도 쿠키가 발급된다)
                .and()
                .authorizeRequests()
                    .antMatchers("/**").permitAll();    // 모든 url에 대해 접근 허용
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
