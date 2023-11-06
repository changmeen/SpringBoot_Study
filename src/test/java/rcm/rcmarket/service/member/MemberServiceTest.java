package rcm.rcmarket.service.member;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rcm.rcmarket.dto.member.MemberDto;
import rcm.rcmarket.entity.member.Member;
import rcm.rcmarket.exception.MemberNotFoundException;
import rcm.rcmarket.repository.member.MemberRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {
    @InjectMocks MemberService memberService;
    @Mock MemberRepository memberRepository;

    private Member createMember() {
        return new Member("email@email.com", "123456a!", "username", "nickname", List.of());
    }

    @Test
    void readTest() {
        // given
        Member member = createMember();
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));

        // when
        MemberDto result = memberService.read(1L);

        // then
        assertThat(result.getEmail()).isEqualTo(member.getEmail());
    }

    @Test
    void readExceptionByMemberNotFoundTest() {
        // given
        given(memberRepository.findById(any())).willReturn(Optional.ofNullable(null));

        // when, then
        assertThatThrownBy(() -> memberService.read(1L)).isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    void deleteTest() {
        // given
        given(memberRepository.existsById(anyLong())).willReturn(true);

        // when
        memberService.delete(1L);

        // then
        verify(memberRepository).deleteById(anyLong());
    }

    @Test
    void deleteExceptionByMemberNotFoundTest() {
        // given
        given(memberRepository.existsById(anyLong())).willReturn(false);

        // when, then
        assertThatThrownBy(() -> memberService.delete(1L)).isInstanceOf(MemberNotFoundException.class);
    }
}
