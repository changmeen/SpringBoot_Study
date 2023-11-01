package rcm.rcmarket.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import rcm.rcmarket.entity.member.Member;
import rcm.rcmarket.entity.member.MemberRole;
import rcm.rcmarket.entity.member.Role;
import rcm.rcmarket.entity.member.RoleType;
import rcm.rcmarket.repository.member.MemberRepository;
import rcm.rcmarket.repository.role.RoleRepository;
import rcm.rcmarket.exception.MemberNotFoundException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
public class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    RoleRepository roleRepository;

    @PersistenceContext
    EntityManager em;

    private void clear() {
        em.flush();
        em.clear();
    }

    private Member createMemberWithRoles(List<Role> roles) {
        return new Member("email", "password",
                "username", "nickname",
                roles);
    }

    private Member createMember(String email, String password,
                                String username, String nickname) {
        return new Member(email, password, username, nickname, emptyList());
    }

    private Member createMember() {
        return new Member("email", "password",
                "username", "nickname", emptyList());
    }

    @Test
    void createAndReadTest() {

        Member member = createMember();

        memberRepository.save(member);
        clear();

        Member foundMember = memberRepository.findById(member.getId())
                .orElseThrow(MemberNotFoundException::new);
        assertThat(foundMember.getId()).isEqualTo(member.getId());

    }

    @Test
    void memberDateTest() {

        Member member = createMember();

        memberRepository.save(member);
        clear();

        Member foundMember = memberRepository.findById(member.getId())
                .orElseThrow(MemberNotFoundException::new);
        assertThat(foundMember.getCreatedAt()).isNotNull();
        assertThat(foundMember.getModifiedAt()).isNotNull();
        assertThat(foundMember.getCreatedAt())
                .isEqualTo(foundMember.getModifiedAt());

    }

    @Test
    void updateTest() {

        String updateNickname = "updated";
        Member member = memberRepository.save(createMember());
        clear();

        Member foundMember = memberRepository.findById(member.getId())
                .orElseThrow(MemberNotFoundException::new);
        foundMember.updateNickname(updateNickname);
        clear();

        Member updatedMember = memberRepository.findById(member.getId())
                .orElseThrow(MemberNotFoundException::new);
        assertThat(updatedMember.getNickname()).isEqualTo(updateNickname);

    }

    @Test
    void deleteTest() {

        Member member = memberRepository.save(createMember());
        clear();

        memberRepository.delete(member);
        clear();

        assertThatThrownBy(() -> memberRepository.findById(member.getId())
                .orElseThrow(MemberNotFoundException::new))
                .isInstanceOf(MemberNotFoundException.class);

    }

    @Test
    void findByEmailTest() {

        Member member = memberRepository.save(createMember());
        clear();

        Member foundMember = memberRepository.findByEmail(member.getEmail())
                .orElseThrow(MemberNotFoundException::new);

        assertThat(foundMember.getEmail()).isEqualTo(member.getEmail());

    }

    @Test
    void findByNicknameTest() {

        Member member = memberRepository.save(createMember());
        clear();

        Member foundMember = memberRepository.findByNickname(member.getNickname())
                .orElseThrow(MemberNotFoundException::new);

        assertThat(foundMember.getNickname()).isEqualTo(member.getNickname());

    }

    @Test
    void uniqueEmailTest() {

        Member member = memberRepository.save(createMember("email1", "password1",
                "username1", "nickname1"));
        clear();

        assertThatThrownBy(() -> memberRepository.save(createMember(member.getEmail(),
                "password2", "username2", "nickname2")))
                .isInstanceOf(DataIntegrityViolationException.class);

    }

    @Test
    void uniqueNicknameTest() {
        Member member = memberRepository.save(createMember("email1", "password1",
                "username1", "nickname1"));
        clear();

        assertThatThrownBy(() -> memberRepository.save(createMember("email2",
                "password2", "username2", member.getNickname())))
                .isInstanceOf(DataIntegrityViolationException.class);

    }

    @Test
    void existsByEmailTest() {
        Member member = memberRepository.save(createMember());
        clear();

        assertThat(memberRepository.existsByEmail(member.getEmail())).isTrue();
        assertThat(memberRepository.existsByEmail(member.getEmail() + "test"))
                .isFalse();

    }

    @Test
    void existsByNicknameTest() {
        Member member = memberRepository.save(createMember());
        clear();

        assertThat(memberRepository.existsByNickname(member.getNickname())).isTrue();
        assertThat(memberRepository.existsByNickname(member.getNickname() + "test"))
                .isFalse();

    }

    @Test
    void memberRoleCascadePersistTest() {
        // Member entity가 one to many관계로 가지고 있는 memberRole이 cascasde(연달아서) persist(저장)
        // 되는지 검증하기 위한 테스트
        
        List<RoleType> roleTypes = List.of(RoleType.ROLE_NORMAL,
                RoleType.ROLE_SPECIAL_BUYER,
                RoleType.ROLE_ADMIN);
        List<Role> roles = roleTypes.stream().map(roleType -> new Role(roleType))
                .collect(Collectors.toList());
        roleRepository.saveAll(roles);
        clear();

        Member member = memberRepository
                .save(createMemberWithRoles(roleRepository.findAll()));
        clear();

        Member foundMember = memberRepository
                .findById(member.getId())
                .orElseThrow(MemberNotFoundException::new);
        Set<MemberRole> memberRoles = foundMember.getRoles();

        assertThat(memberRoles.size()).isEqualTo(roles.size());

    }

    @Test
    void memberRoleCascadeDeleteTest() {
        // Member를 제거할 때 memberRole도 같이 제거되는지 테스트

        List<RoleType> roleTypes = List.of(RoleType.ROLE_NORMAL,
                RoleType.ROLE_SPECIAL_BUYER,
                RoleType.ROLE_ADMIN);
        List<Role> roles = roleTypes.stream().map(roleType -> new Role(roleType))
                .collect(Collectors.toList());
        roleRepository.saveAll(roles);
        clear();

        Member member = memberRepository
                .save(createMemberWithRoles(roleRepository.findAll()));
        clear();

        memberRepository.deleteById(member.getId());
        clear();

        List<MemberRole> result = em.createQuery("select mr from MemberRole mr",
                MemberRole.class).getResultList();
        assertThat(result.size()).isZero();

    }
}













