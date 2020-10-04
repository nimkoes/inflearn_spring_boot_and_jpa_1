package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    
    /*
     * Spring data Jpa 에 convention 이 있는데,
     * findBy{xxx} 라고 추상 메소드를 작성하면 다음과 같은 쿼리를 작성해서 실행해준다.
     *
     * select m from Member m where m.name = ?
     *
     * 즉, 구현체를 제공해주지 않아도 된다.
     */
    List<Member> findByName(String name);
}
