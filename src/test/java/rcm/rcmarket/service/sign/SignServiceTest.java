package rcm.rcmarket.service.sign;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import rcm.rcmarket.dto.sign.SignInRequest;
import rcm.rcmarket.dto.sign.SignInResponse;
import rcm.rcmarket.dto.sign.SignUpRequest;
import rcm.rcmarket.entity.member.Member;
import rcm.rcmarket.entity.member.Role;
import rcm.rcmarket.entity.member.RoleType;
import rcm.rcmarket.exception.LoginFailureException;
import rcm.rcmarket.exception.MemberEmailAlreadyExistsException;
import rcm.rcmarket.exception.MemberNicknameAlreadyExistsException;
import rcm.rcmarket.exception.RoleNotFoundException;
import rcm.rcmarket.repository.member.MemberRepository;
import rcm.rcmarket.repository.role.RoleRepository;

import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SignServiceTest {

    // Mockito 프레임워크는 테스트를 위한 것이다
    // 테스트를 위해 의존하고 있는 객체들을 가짜로 만들어서 SignService에 주입해준다
    @InjectMocks SignService signService;
    @Mock MemberRepository memberRepository;
    @Mock RoleRepository roleRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock TokenService tokenService;

    private SignUpRequest createSignUpRequest() {
        return new SignUpRequest("email", "password", "username", "nickname");
    }

    private Member createMember() {
        return new Member("email", "password", "username", "nickname", emptyList());
    }

    // verify를 이용해 passwordEncoder가 encode를 수행했는지, memberRepository가 save를 수행했는지 확인함
    @Test
    void SignUpTest() {
        // given
        SignUpRequest req = createSignUpRequest();
        given(roleRepository.findByRoleType(RoleType.ROLE_NORMAL)).willReturn(Optional.of(new Role(RoleType.ROLE_NORMAL)));

        // when
        signService.signUp(req);

        // then
        verify(passwordEncoder).encode(req.getPassword());
        verify(memberRepository).save(any());
    }

    @Test
    void validateSignUpByDuplicateEmailTest() {
        // given
        given(memberRepository.existsByEmail(anyString())).willReturn(true);

        // when, then
        assertThatThrownBy(() -> signService.signUp(createSignUpRequest()))
                .isInstanceOf(MemberEmailAlreadyExistsException.class);
    }

    @Test
    void validateSignUpByDuplicateNicknameTest() {
        // given
        given(memberRepository.existsByNickname(anyString())).willReturn(true);

        // when, then
        assertThatThrownBy(() -> signService.signUp(createSignUpRequest()))
                .isInstanceOf(MemberNicknameAlreadyExistsException.class);
    }

    @Test
    void signUpRoleNotFoundTest() {
        // given
        given(roleRepository.findByRoleType(RoleType.ROLE_NORMAL)).willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> signService.signUp(createSignUpRequest()))
                .isInstanceOf(RoleNotFoundException.class);
    }

    @Test
    void signInTest() {
        // given
        given(memberRepository.findByEmail(any())).willReturn(Optional.of(createMember()));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(tokenService.createAccessToken(anyString())).willReturn("access");
        given(tokenService.createRefreshToken(anyString())).willReturn("refresh");

        // when
        SignInResponse res = signService.signIn(new SignInRequest("email", "password"));

        // then
        assertThat(res.getAccessToken()).isEqualTo("access");
        assertThat(res.getRefreshToken()).isEqualTo("refresh");
    }

    @Test
    void signInExceptionByNoneMemberTest() {
        // given
        given(memberRepository.findByEmail(any())).willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> signService.signIn(new SignInRequest("email", "password")))
                .isInstanceOf(LoginFailureException.class);
    }

    @Test
    void signInExceptionByInvalidPasswordTest() {
        // given
        given(memberRepository.findByEmail(any())).willReturn(Optional.of(createMember()));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        // when, then
        assertThatThrownBy(() -> signService.signIn(new SignInRequest("email", "password")))
                .isInstanceOf(LoginFailureException.class);
    }
}
