package jpabook.jpashop.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.QMember;
import jpabook.jpashop.domain.QOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
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
        /*
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
        */
        
        /**
         * SOLUTION 3 : QueryDSL 사용
         * └─> querydsl 을 위한 빌드 설정을 한 다음 Q 파일을 생성하도록 compileQuerydsl 을 실행한다.
         *     현재 설정 기준으로 'src/main/generated' 하위에 Q 파일들이 생성된다.
         *     이것들을 활용해서 동적 쿼리를 작성한다.
         *
         *     static import, constructor di 등을 사용하면 더 축약 가능
         */
        JPAQueryFactory query = new JPAQueryFactory(em);
        QOrder order = QOrder.order;
        QMember member = QMember.member;
        
        return query
                .select(order)
                .from(order)
                .join(order.member, member)
//                .where(statusEq(orderSearch.getOrderStatus()))
//                .where(order.status.eq(orderSearch.getOrderStatus()), member.name.like(orderSearch.getMemberName()))
                .where(order.status.eq(orderSearch.getOrderStatus()), nameLike(orderSearch.getMemberName()))
                .limit(1000)
                .fetch();
    }
    
    
    // querydsl 조건절을 위한 메소드
    private BooleanExpression statusEq(OrderStatus statusCond) {
        if (statusCond == null) {
            return null;
        }
        return QOrder.order.status.eq(statusCond);
    }
    
    private BooleanExpression nameLike(String memberName) {
        if (StringUtils.hasText(memberName)) {
            return null;
        }
        return QMember.member.name.like(memberName);
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
    
    /*
     * JPA 에서의 distinct 키워드는
     * DB 에서와 조금 다르게 동작한다.
     *
     * DB 에서는 조회한 row 의 결과가 완전 동일한 경우 중복을 제거하는 반면
     * 지금의 경우 Order 객체를 조회하고 있기 때문에 ( -> from Order)
     * DB 에도 distinct 하고 애플리케이션 레벨에서 조회해온 Order 객체가 동일할 경우 중복을 제거해준다.
     *
     * 즉, 로그에 남은 조회 쿼리를 직접 실행해본 결과와
     * 이 메소드를 실행해서 받은 응답 결과가 다를 수 있다.
     *
     */
    public List<Order> findAllWithItem() {
        return em.createQuery(
                "select distinct o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi" +
                        " join fetch oi.item i", Order.class)
                .getResultList();
    }
    
    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
                )
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}
