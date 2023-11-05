package rcm.rcmarket.entity.member;

import rcm.rcmarket.entity.common.EntityDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends EntityDate{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, length = 30, unique = true)
    private String email;

    private String password;

    @Column(nullable = false, length = 20)
    private String username;

    @Column(nullable = false, unique = true, length = 20)
    private String nickname;

    // Role은 직급이 아니라 가지고 있는 권한이기에 여러 개를 가질 수 있음
    // 따라서 member와 Role의 관계는 1:N 관계임
    @OneToMany(mappedBy = "member", cascade =
            CascadeType.ALL, orphanRemoval = true)
    private Set<MemberRole> roles;

    public Member(String email, String password, String username, String nickname, List<Role> roles) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.nickname = nickname;
        this.roles = roles.stream().map(r -> new MemberRole(this, r)).collect(toSet());
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
