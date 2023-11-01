package rcm.rcmarket.repository.member;

import org.springframework.data.jpa.repository.JpaRepository;
import rcm.rcmarket.entity.member.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByNickname(String nickname);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

}
