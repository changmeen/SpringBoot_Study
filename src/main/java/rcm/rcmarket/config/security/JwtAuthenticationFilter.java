package rcm.rcmarket.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.GenericFilterBean;
import rcm.rcmarket.service.sign.TokenService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

// GenericFilterBean을 상속받아 필터를 구현
// @Component를 선언하면 자동으로 필터 체인에 등록되기에 중복 방지를 위해 Component 생략
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final TokenService tokenService;
    private final CustomUserDetailsService userDetailsService;

    private String extractToken(ServletRequest request) {
        return ((HttpServletRequest)request).getHeader("Authorization");
    }

    private boolean validateAccessToken(String token) {
        return token != null && tokenService.validateAccessToken(token);
    }

    private boolean validateRefreshToken(String token) {
        return token != null && tokenService.validateRefreshToken(token);
    }

    private void setAccessAuthentication(String type, String token) {
        String userId = tokenService.extractAccessTokenSubject(token);
        CustomUserDetails userDetails = userDetailsService.loadUserByUsername(userId);
        SecurityContextHolder.getContext().setAuthentication(new CustomAuthenticationToken(type, userDetails, userDetails.getAuthorities()));
    }

    private void setRefreshAuthentication(String type, String token) {
        String userId = tokenService.extractRefreshTokenSubject(token);
        CustomUserDetails userDetails = userDetailsService.loadUserByUsername(userId);
        SecurityContextHolder.getContext().setAuthentication(new CustomAuthenticationToken(type, userDetails, userDetails.getAuthorities()));
    }

    // 요청으로 전달 받은 Authorization 헤더에서 토큰 값을 꺼내오고,
    // 토큰이 유효하다면 SpringSecurity가 관리해주는 컨텍스트에 사용자 정보를 등록한다
    // 정확히는 SecurityContextHolder에 있는 ContextHolder에다가
    // Authentication 인터페이스의 구현체 CustomAuthenticationToken를
    // 등록해주는 작업

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {
        String token = extractToken(request);

        if(validateAccessToken(token)) {
            setAccessAuthentication("access", token);
        } else if (validateRefreshToken(token)) {
            setRefreshAuthentication("refresh", token);
        }
        chain.doFilter(request, response);
    }
}
