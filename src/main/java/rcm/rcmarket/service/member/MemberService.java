package rcm.rcmarket.service.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rcm.rcmarket.dto.member.MemberDto;
import rcm.rcmarket.exception.MemberNotFoundException;
import rcm.rcmarket.repository.member.MemberRepository;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberDto read(Long id) {
        return MemberDto.toDto(memberRepository.findById(id).orElseThrow(MemberNotFoundException::new));
    }

    // 찾고자 하는게 있으면 음수이기에 if문 실행 안함
    // 찾고자 하는게 없으면 양수이기에 if문 실행 -> MemberNotFoundException 실행
    // 찾고자 하는게 있으면 delete 실행
    @Transactional
    public void delete(Long id){
        if(notExistsMember(id)) throw new MemberNotFoundException();
        memberRepository.deleteById(id);
    }

    // 찾고자 하는 아이디가 있으면 음수를 없으면 양수를 return
    private boolean notExistsMember(Long id){
        return !memberRepository.existsById(id);
    }
}
