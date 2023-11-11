package rcm.rcmarket.config.security.guard;

import org.springframework.security.core.context.SecurityContextHolder;
import rcm.rcmarket.config.security.CustomAuthenticationToken;
import rcm.rcmarket.config.security.CustomUserDetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import rcm.rcmarket.entity.member.RoleType;

import java.util.Set;
import java.util.stream.Collectors;

// 사용자 인증 정보를 추출하기 위해 도와주는 클래스
@Component
@Slf4j
public class AuthHelper {
    public boolean isAuthenticated() {
        return getAuthentication() instanceof CustomAuthenticationToken &&
                getAuthentication().isAuthenticated();
    }

    public Long extractMemberId() {
        return Long.valueOf(getUserDetails().getUserId());
    }

    public Set<RoleType> extractMemberRoles() {
        return getUserDetails().getAuthorities()
                .stream()
                .map(authority -> authority.getAuthority())
                .map(strAuth -> RoleType.valueOf(strAuth))
                .collect(Collectors.toSet());
    }

    private CustomUserDetails getUserDetails() {
        return (CustomUserDetails) getAuthentication().getPrincipal();
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
