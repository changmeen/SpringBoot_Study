package rcm.rcmarket.service.sign;

import rcm.rcmarket.config.token.TokenHelper;
import rcm.rcmarket.dto.sign.RefreshTokenResponse;
import rcm.rcmarket.dto.sign.SignInRequest;
import rcm.rcmarket.dto.sign.SignInResponse;
import rcm.rcmarket.dto.sign.SignUpRequest;
import rcm.rcmarket.entity.member.Member;
import rcm.rcmarket.entity.member.RoleType;
import rcm.rcmarket.exception.*;
import rcm.rcmarket.repository.member.MemberRepository;
import rcm.rcmarket.repository.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
// @RequiredArgsConstructor를 클래스 레벨에 선언하면,
// final로 선언된 인스턴스 변수들로 생성자를 만들어줍니다
@RequiredArgsConstructor
public class SignService {

    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenHelper accessTokenHelper;
    private final TokenHelper refreshTokenHelper;

    // 이메일과 닉네임의 중복성을 검색, 주어진 SignUpRequest를 Entity로 변환한다
    @Transactional
    public void signUp(SignUpRequest req) {
        validateSignUpInfo(req);
        memberRepository.save(SignUpRequest.toEntity(req,
                roleRepository.findByRoleType(RoleType.ROLE_NORMAL).orElseThrow(RoleNotFoundException::new),
                passwordEncoder));
    }

    // SignInRequest로 전달받은 email로 Member를 조회, 비밀번호 검증이 통과될 경우
    // AccessToken과 RefreshToken을 발급해준다.
    @Transactional(readOnly = true)
    public SignInResponse signIn(SignInRequest req) {
        Member member = memberRepository.findByEmail(req.getEmail()).orElseThrow(LoginFailureException::new);
        validatePassword(req, member);
        String subject = createSubject(member);
        String accessToken = accessTokenHelper.createToken(subject);
        String refreshToken = refreshTokenHelper.createToken(subject);
        return new SignInResponse(accessToken, refreshToken);
    }

    // 이메일과 닉네임의 중복을 검사하고 중복이 있으면 런타임 예외 발생
    private void validateSignUpInfo(SignUpRequest req) {
        if(memberRepository.existsByEmail(req.getEmail()))
            throw new MemberEmailAlreadyExistsException(req.getEmail());
        if(memberRepository.existsByNickname(req.getNickname()))
            throw new MemberNicknameAlreadyExistsException(req.getNickname());
    }

    private void validatePassword(SignInRequest req, Member member) {
        if(!passwordEncoder.matches(req.getPassword(), member.getPassword())) {
            throw new LoginFailureException();
        }
    }

    private String createSubject(Member member) {
        return String.valueOf(member.getId());
    }

    public RefreshTokenResponse refreshToken(String rToken) {
        validateRefreshToken(rToken);
        String subject = refreshTokenHelper.extractSubject(rToken);
        String accessToken = accessTokenHelper.createToken(subject);

        return new RefreshTokenResponse(accessToken);
    }

    private void validateRefreshToken(String rToken) {
        if(!refreshTokenHelper.validate(rToken)) {
            throw new AuthenticationEntryPointException();
        }
    }
}
