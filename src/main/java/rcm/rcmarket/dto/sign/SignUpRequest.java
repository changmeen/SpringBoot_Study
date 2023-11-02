package rcm.rcmarket.dto.sign;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;
import rcm.rcmarket.entity.member.Member;
import rcm.rcmarket.entity.member.Role;

import java.util.List;

@Data
@AllArgsConstructor
public class SignUpRequest {

    private String email;
    private String password;
    private String username;
    private String nickname;

    public static Member toEntity(SignUpRequest req, Role role, PasswordEncoder encoder) {
        return new Member(req.email, encoder.encode(req.password), req.username, req.nickname, List.of(role));
    }
}
