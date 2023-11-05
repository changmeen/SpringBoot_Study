package rcm.rcmarket.entity.member;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
@IdClass(MemberRoleId.class)
public class MemberRole {
// 하나의 member와 하나의 role과 묶이는 게 entity의 instance가 된다.
// Member Class에서는 이러한 MemberRole을 Set로 가져가서 여러 role을 가질 수 있게 표시

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;
}
