package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * 회원 가입
     */
    @Transactional(readOnly = false)
    /*
     * 클래스 레벨에서는 readOnly 를 true 로 주고
     * 읽기 이외의 작업을 하는 메소드에 대해
     * 메소드 레벨에서 readOnly 값을 false 또는 @Transactional 을 붙여준다.
     * -> 기본값이 false 이므로 속성값을 넣지 않아도 된다.
     * -> 메소드 레벨의 설정이 더 우선권을 가지기 때문에 가능.
     *
     * readOnly 가 true 인 경우 읽기 전용의 설정을 통해 나름의 최적화 작업이 이루어 지기 때문에
     * 어느 정도의 성능 최적화를 이룰 수 있다.
     */
    public Long join(Member member) {
        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    /**
     * 회원 전체 조회
     */
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    /**
     * 회원 한명 조회
     */
    public Member findOne(Long memberId) {
//        return memberRepository.findOne(memberId);
        return memberRepository.findById(memberId).get();
    }
    
    @Transactional
    public void update(Long id, String name) {
//        Member member = memberRepository.findOne(id);
        Member member = memberRepository.findById(id).get();
        member.setName(name);
    }
}
