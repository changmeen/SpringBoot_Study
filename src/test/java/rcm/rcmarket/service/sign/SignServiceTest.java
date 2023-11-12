package rcm.rcmarket.service.sign;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import rcm.rcmarket.config.token.TokenHelper;
import rcm.rcmarket.dto.sign.RefreshTokenResponse;
import rcm.rcmarket.dto.sign.SignInResponse;
import rcm.rcmarket.dto.sign.SignUpRequest;
import rcm.rcmarket.entity.member.RoleType;
import rcm.rcmarket.exception.*;
import rcm.rcmarket.repository.member.MemberRepository;
import rcm.rcmarket.repository.role.RoleRepository;

import java.util.Optional;

import static rcm.rcmarket.factory.dto.SignInRequestFactory.*;
import static rcm.rcmarket.factory.dto.SignUpRequestFactory.*;
import static rcm.rcmarket.factory.entity.MemberFactory.*;
import static rcm.rcmarket.factory.entity.RoleFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class SignServiceTest {

    // Mockito 프레임워크는 테스트를 위한 것이다
    // 테스트를 위해 의존하고 있는 객체들을 가짜로 만들어서 SignService에 주입해준다
    SignService signService;
    @Mock MemberRepository memberRepository;
    @Mock RoleRepository roleRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock TokenHelper accessTokenHelper;
    @Mock TokenHelper refreshTokenHelper;

    // verify를 이용해 passwordEncoder가 encode를 수행했는지, memberRepository가 save를 수행했는지 확인함
    @BeforeEach
    void beforeEach() {
        signService = new SignService(memberRepository, roleRepository, passwordEncoder, accessTokenHelper, refreshTokenHelper);
    }

    @Test
    void SignUpTest() {
        // given
        SignUpRequest req = createSignUpRequest();
        given(roleRepository.findByRoleType(RoleType.ROLE_NORMAL)).willReturn(Optional.of(createRole()));

        // when
        signService.signUp(req);

        // then
        verify(passwordEncoder).encode(req.getPassword());
        verify(memberRepository).save(any());
    }

    // email이 중복되었는지 확인
    // 중복되었을 경우 true를 반환하게 되고 true일 경우 memberEmailAlreadyExistsException 발생
    @Test
    void validateSignUpByDuplicateEmailTest() {
        // given
        given(memberRepository.existsByEmail(anyString())).willReturn(true);

        // when, then
        assertThatThrownBy(() -> signService.signUp(createSignUpRequest()))
                .isInstanceOf(MemberEmailAlreadyExistsException.class);
    }

    // nickname이 중복되었는지 확인
    // 중복되었을 경우 true를 반환하게 되고 true일 경우 memberNicknameAlreadyExistsException 발생
    @Test
    void validateSignUpByDuplicateNicknameTest() {
        // given
        given(memberRepository.existsByNickname(anyString())).willReturn(true);

        // when, then
        assertThatThrownBy(() -> signService.signUp(createSignUpRequest()))
                .isInstanceOf(MemberNicknameAlreadyExistsException.class);
    }

    // 등록되지 않은 권한 등급으로 회원가입을 수행하려하면 해당 권한 등급은 찾을 수 없기에 null(empty)이 반환됨
    // 따라서 Optional.Empty()가 반환되면 RoleNotFoundException을 발생시킴
    @Test
    void signUpRoleNotFoundTest() {
        // given
        given(roleRepository.findByRoleType(RoleType.ROLE_NORMAL)).willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> signService.signUp(createSignUpRequest()))
                .isInstanceOf(RoleNotFoundException.class);
    }

    // 정상적으로 로그인 처리가 되는지 확인하는 테스트
    // 정상적으로 수행되면 accessToken과 refreshToken을 가지고있는 SignInResponse 가 반환된다.
    // willReturn은 준비된 값을 의미하며 검증해볼 수 있도록 해주는 역할을 수행한다
    @Test
    void signInTest() {
        // given
        given(memberRepository.findByEmail(any())).willReturn(Optional.of(createMember()));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(accessTokenHelper.createToken(anyString())).willReturn("access");
        given(refreshTokenHelper.createToken(anyString())).willReturn("refresh");

        // when
        SignInResponse res = signService.signIn(createSignInRequest("email", "password"));

        // then
        assertThat(res.getAccessToken()).isEqualTo("access");
        assertThat(res.getRefreshToken()).isEqualTo("refresh");
    }

    // 등록된 이메일이 아니라면 찾을 수 없기에 Optional.Empty()가 반환되고
    // LoginFailureException을 발생시킨다.
    @Test
    void signInExceptionByNoneMemberTest() {
        // given
        given(memberRepository.findByEmail(any())).willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> signService.signIn(createSignInRequest("email", "password")))
                .isInstanceOf(LoginFailureException.class);
    }

    // passwordEncoder.matches가 false를 반환하면 -> 즉 비밀번호가 유효하지 않으면
    // LoginFailureException이 발생한다.
    @Test
    void signInExceptionByInvalidPasswordTest() {
        // given
        given(memberRepository.findByEmail(any())).willReturn(Optional.of(createMember()));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        // when, then
        assertThatThrownBy(() -> signService.signIn(createSignInRequest("email", "password")))
                .isInstanceOf(LoginFailureException.class);
    }

    @Test
    void refreshTokenTest() {
        // given
        String refreshToken = "refreshToken";
        String subject = "subject";
        String accessToken = "accessToken";
        given(refreshTokenHelper.validate(refreshToken)).willReturn(true);
        given(refreshTokenHelper.extractSubject(refreshToken)).willReturn(subject);
        given(accessTokenHelper.createToken(subject)).willReturn(accessToken);

        // when
        RefreshTokenResponse res = signService.refreshToken(refreshToken);

        // then
        assertThat(res.getAccessToken()).isEqualTo(accessToken);
    }

    @Test
    void refreshTokenExceptionByInvalidTokenTest() {
        // given
        String refreshToken = "refreshToken";
        given(refreshTokenHelper.validate(refreshToken)).willReturn(false);

        // when, then
        assertThatThrownBy(() -> signService.refreshToken(refreshToken))
                .isInstanceOf(AuthenticationEntryPointException.class);
    }
}
