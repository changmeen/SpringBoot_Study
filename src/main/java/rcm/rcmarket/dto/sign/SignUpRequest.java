package rcm.rcmarket.dto.sign;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;
import rcm.rcmarket.entity.member.Member;
import rcm.rcmarket.entity.member.Role;

import java.util.List;

// 여러 계층에서 단순히 데이터 전달용으로 사용되기에 @Data를 쓴다
// @Data는 Getter, Setter, EqualsAndHashCode, ToString 등을 만들어준다함
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
