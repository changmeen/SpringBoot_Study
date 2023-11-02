package rcm.rcmarket.service.sign;

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
// 하나의 메소드를 하나의 트랜잭션으로 묶어준다
@Transactional(readOnly = true)
public class SignService {

    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Transactional
    public void signUp(SignUpRequest req) {
        validateSignUpInfo(req) {
            memberRepository.save(SignUpRequest.toEntity(req,
                    roleRepository.findByRoleType(RoleType.ROLE_NORMAL).orElseThrow(RoleNotFoundException::new),
                    passwordEncoder));
        }
    }

    public SignInResponse signIn(SignInRequest req) {
        Member member = memberRepository.findByEmail(req.getEmail()).orElseThrow(LoginFailureException::new);
        validatePassword(req, member);
        String subject = createSubject(member);
        String accessToken = tokenService.createAccessToken(subject);
        String refreshToken = tokenService.createRefreshToken(subject);

        return new SignInResponse(accessToken, refreshToken);
    }

    private void validateSignUpInfo(SignUpRequest req) {
        if(memberRepository.existsByEmail(req.getEmail()))
            throw new MemberEmailAlreadyExistsException(req.getEmail());
        if(memberRepository.existsByNickname(req.getNickname()))
            throw new MemberNicknameAlreadyExistsException(req.getNickname());
    }

    private void validatePassword(SignInRequest req, Member member) {
        if(!passwordEncoder.matches(req.getPassword(), member.getPassword()))
            throw new LoginFailureException();
    }

    private String createSubject(Member member) {
        return String.valueOf(member.getId());
    }
}
