package rcm.rcmarket.dto.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rcm.rcmarket.entity.member.Member;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberDto {
    private Long id;
    private String email;
    private String username;
    private String nickname;

    public static MemberDto toDto(Member member) {
        return new MemberDto(member.getId(), member.getEmail(), member.getUsername(), member.getNickname());
    }
}
