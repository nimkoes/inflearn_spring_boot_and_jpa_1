package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * xToOne 관계에 대한 성능 최적화 (ManyToOne, OneToOne)
 * <p>
 * Order
 * Order -> Member
 * Order -> Delivery
 *
 * [쿼리 최적화 권장 순서]
 *      1. 우선 엔티티를 DTO 로 변환하는 방법을 선택  (DTO 에 파싱)           :: v2
 *      2. 필요하면 fetch join 으로 성능 최적화 (대부분의 이슈 해결)          :: v3
 *      3. 그래도 안되면 DTO 를 직접 조회하는 방법 사용 (ex, 네트워크 트래픽) :: v4
 *      4. 최후의 방법은 JPA 가 제공하는 네이티브 SQL 이나
 *         스프링 JDBC Template 을 사용해서 SQL 을 직접 작성
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
    
    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;
    
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();       // Lazy 강제 초기화
            order.getDelivery().getAddress();  // Lazy 강제 초기화
        }
        return all;
    }
    
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        return orderRepository.findAllByString(new OrderSearch()).stream()
                .map(SimpleOrderDto::new)
                .collect(toList());
        /*
            =============================================================================
            == EXECUTE QUERY
            =============================================================================
            select
                order0_.order_id as order_id1_6_,
                order0_.delivery_id as delivery4_6_,
                order0_.member_id as member_i5_6_,
                order0_.order_date as order_da2_6_,
                order0_.status as status3_6_
            from
                orders order0_
            inner join
                member member1_
                    on order0_.member_id=member1_.member_id
            where
                1=1 limit ?
                
            =============================================================================
            select
                member0_.member_id as member_i1_4_0_,
                member0_.city as city2_4_0_,
                member0_.street as street3_4_0_,
                member0_.zipcode as zipcode4_4_0_,
                member0_.name as name5_4_0_
            from
                member member0_
            where
                member0_.member_id=?
            =============================================================================
            select
                delivery0_.delivery_id as delivery1_2_0_,
                delivery0_.city as city2_2_0_,
                delivery0_.street as street3_2_0_,
                delivery0_.zipcode as zipcode4_2_0_,
                delivery0_.status as status5_2_0_
            from
                delivery delivery0_
            where
                delivery0_.delivery_id=?
            =============================================================================
            select
                member0_.member_id as member_i1_4_0_,
                member0_.city as city2_4_0_,
                member0_.street as street3_4_0_,
                member0_.zipcode as zipcode4_4_0_,
                member0_.name as name5_4_0_
            from
                member member0_
            where
                member0_.member_id=?
            =============================================================================
            select
                delivery0_.delivery_id as delivery1_2_0_,
                delivery0_.city as city2_2_0_,
                delivery0_.street as street3_2_0_,
                delivery0_.zipcode as zipcode4_2_0_,
                delivery0_.status as status5_2_0_
            from
                delivery delivery0_
            where
                delivery0_.delivery_id=?
            =============================================================================
         */
    }
    
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        return orders.stream()
                .map(SimpleOrderDto::new)
                .collect(toList());
        /*
            =============================================================================
            == EXECUTE QUERY
            =============================================================================
            select
                order0_.order_id as order_id1_6_0_,
                member1_.member_id as member_i1_4_1_,
                delivery2_.delivery_id as delivery1_2_2_,
                order0_.delivery_id as delivery4_6_0_,
                order0_.member_id as member_i5_6_0_,
                order0_.order_date as order_da2_6_0_,
                order0_.status as status3_6_0_,
                member1_.city as city2_4_1_,
                member1_.street as street3_4_1_,
                member1_.zipcode as zipcode4_4_1_,
                member1_.name as name5_4_1_,
                delivery2_.city as city2_2_2_,
                delivery2_.street as street3_2_2_,
                delivery2_.zipcode as zipcode4_2_2_,
                delivery2_.status as status5_2_2_
            from
                orders order0_
            inner join
                member member1_
                    on order0_.member_id=member1_.member_id
            inner join
                delivery delivery2_
                    on order0_.delivery_id=delivery2_.delivery_id
            =============================================================================
         */
    }
    
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderSimpleQueryRepository.findOrderDtos();
        /*
            =============================================================================
            == EXECUTE QUERY
            =============================================================================
            select
                order0_.order_id as col_0_0_,
                member1_.name as col_1_0_,
                order0_.order_date as col_2_0_,
                order0_.status as col_3_0_,
                delivery2_.city as col_4_0_,
                delivery2_.street as col_4_1_,
                delivery2_.zipcode as col_4_2_
            from
                orders order0_
            inner join
                member member1_
                    on order0_.member_id=member1_.member_id
            inner join
                delivery delivery2_
                    on order0_.delivery_id=delivery2_.delivery_id
            =============================================================================
         */
    }
    
    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
    
        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();         // LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // LAZY 초기화
        }
    }
}
