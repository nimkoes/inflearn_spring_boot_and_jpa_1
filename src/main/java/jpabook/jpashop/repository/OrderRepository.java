package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    public List<Order> findAllByString(OrderSearch orderSearch) {
        /*
         * 아래와 같이 구현하면 좋겠으나, 동적쿼리 문제가 있음.
         * CASE 1 : 상태와 이름 정보 둘 다 있음.
         * CASE 2 : 상태 정보만 있음.
         * CASE 3 : 이름 정보만 있음.
         * CASE 4 : 아무 정보가 없어서 모든 값을 조회.
         *
        return em.createQuery("select o from Order o join o.member m" +
                " where o.status = :status " +
                " and m.name like :name", Order.class)
                .setParameter("status", orderSearch.getOrderStatus())
                .setParameter("name", orderSearch.getMemberName())
                .setMaxResults(1000)        //최대 1000건
                .getResultList();
        */
        
        /**
         * SOLUTION 1 : JPQL 문자열 동적 구성
         */
        /*
        String jpql = "select o from Order o join o.member m";
        boolean isFirstCondition = true;
        
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
        
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> qurey = em.createQuery(jpql, Order.class)
                .setMaxResults(1000);

        if (orderSearch.getOrderStatus() != null) {
            qurey = qurey.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            qurey = qurey.setParameter("name", orderSearch.getMemberName());
        }
        
        return qurey.getResultList();
        */

        
        /**
         * SOLUTION 2 : JPA Criteria
         * └─> jpql 을 java 코드로 작성할 때, JPA 에서 제공하는 동적 쿼리를 생성하는 표준으로 제공해주는 기능
         *     사람이 사용할게 못된다.
         */
        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Object, Object> m = o.join("member", JoinType.INNER);

        List<Predicate> criteria = new ArrayList<>();
        
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name = cb.like(m.get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);
        
        return query.getResultList();
        
        
        /**
         * SOLUTION 3 : QueryDSL 사용
         * └─> 다음에 다룰 예정
         */
    }
    
    /*
     * fetch join 을 사용해서 내용을 한번에 즉, 쿼리 한번으로 모든 내용을 조회해온다.
     * └─> LAZY Loading 에 따른 N + 1 이슈
     */
    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).getResultList();
    }
    
}
