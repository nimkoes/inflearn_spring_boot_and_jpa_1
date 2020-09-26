package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
// └─> 스프링을 사용해서 테스트를 실행할 때 넣는 annotation
@SpringBootTest
// └─> 스프링 부트를 띄운 상태로 테스트를 진행하기위해 넣는 annotation
//     이게 없으면 @Autowired 등이 실패한다. (스프링 컨테이너 안에서 실행하는 테스트이기 때문)
@Transactional
// └─> 테스트 실행 이후 모든 database 작업을 rollback 하기 위한 annotation
//     테스트이기 때문에 rollback 수행. 일반적인 @Service, @Repository 에서 사용할 때는 rollback 하지 않음.
public class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired EntityManager em;

    @Test
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("nimkoes");

        //when
        Long savedId = memberService.join(member);

        //then
        em.flush();
        assertEquals(member, memberRepository.findOne(savedId));
    }

    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("nimkoes");

        Member member2 = new Member();
        member2.setName("nimkoes");

        //when
        memberService.join(member1);
        memberService.join(member2);    //예외 발생 예상 지점

        //then
        fail("예외가 발생 해야 합니다.");
    }

}