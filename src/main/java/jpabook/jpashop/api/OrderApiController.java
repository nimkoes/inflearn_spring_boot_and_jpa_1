package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;
    
    @GetMapping("/api/v1/orders")
    public List<Order> orderV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return all;
    }
    
    
    @GetMapping("/api/v2/orders")
    public List<OrderDto> orderV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return result;
    }
    
    
    @GetMapping("/api/v3/orders")
    public List<OrderDto> orderV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return result;
    }
    
    
    /*
     * xToMany 에서 page 처리 문제 해결 방법
     *    1. xToOne 관계에 대해 모두 fetch join 한다.    --> orderRepository.findAllWithMemberDelivery();
     *       └─> xToOne 관계는 페이징에 영향을 주지 않기 때문
     *    2. 설정에서 'default_batch_fetch_size' 값을 할당
     *       └─> 이 size 만큼 in query 를 날려 한번에 여러건을 가져온다.
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> orderV3_page(
            @RequestParam(value = "offset", defaultValue = "0"  ) int offset,
            @RequestParam(value = "limit" , defaultValue = "100") int limit)
    {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return result;
    }
    
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> orderV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }
    
    
    
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> orderV5() {
        return orderQueryRepository.findAllByDto_optimization();
        
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
            select
                orderitem0_.order_id as col_0_0_,
                item1_.name as col_1_0_,
                orderitem0_.order_price as col_2_0_,
                orderitem0_.count as col_3_0_
            from
                order_item orderitem0_
            inner join
                item item1_
                    on orderitem0_.item_id=item1_.item_id
            where
                orderitem0_.order_id in (
                    ? , ?
                )
            =============================================================================
         */
    }
    
    @GetMapping("/api/v6/orders")
    public List<OrderFlatDto> orderV6() {
        return orderQueryRepository.findAllByDto_flat();
        
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
                delivery2_.zipcode as col_4_2_,
                item4_.name as col_5_0_,
                orderitems3_.order_price as col_6_0_,
                orderitems3_.count as col_7_0_
            from
                orders order0_
            inner join
                member member1_
                    on order0_.member_id=member1_.member_id
            inner join
                delivery delivery2_
                    on order0_.delivery_id=delivery2_.delivery_id
            inner join
                order_item orderitems3_
                    on order0_.order_id=orderitems3_.order_id
            inner join
                item item4_
                    on orderitems3_.item_id=item4_.item_id
            =============================================================================
            
         */
    }
    
    
    @GetMapping("/api/v6.1/orders")
    public List<OrderQueryDto> orderV6_mapping_OrderQueryDto() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();
    
        return flats.stream()
                .collect(
                        groupingBy(
                                o -> new OrderQueryDto(
                                        o.getOrderId()
                                        , o.getName()
                                        , o.getOrderDate()
                                        , o.getOrderStatus()
                                        , o.getAddress()
                                ),
                                mapping(o -> new OrderItemQueryDto(
                                        o.getOrderId()
                                        , o.getItemName()
                                        , o.getOrderPrice()
                                        , o.getCount())
                                        , toList()
                                )
                        )
                )
                .entrySet().stream()
                    .map(
                            e -> new OrderQueryDto(
                                e.getKey().getOrderId()
                                , e.getKey().getName()
                                , e.getKey().getOrderDate()
                                , e.getKey().getOrderStatus()
                                , e.getKey().getAddress()
                                , e.getValue()
                            )
                    ).collect(toList());
    }
    
    
    /*
     * DTO 를 만들었지만, 그 안에 Entity 를 그대로 사용하는 부분이 있음.
     * └─> List<OrderItem>   :: 이것에 대해서도 DTO를 따로 만들어 줘야 한다.
     */
    /*
    @Getter
    static class OrderDto {
        
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItem> orderItems;         // warning :: using entity !!!
        
        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            order.getOrderItems().stream().forEach(o -> o.getItem().getName());
            orderItems = order.getOrderItems();
        }
    }
    */
    
    /*
     * 위에 사용한 DTO 를 개선한 DTO
     */
    @Getter
    static class OrderDto {
        
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;
        
        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .collect(toList());
        }
    }
    
    @Getter
    static class OrderItemDto {
        private String itemName;    // 상품 명
        private int orderPrice;     // 주문 가격
        private int count;          // 주문 수량
    
        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
    
}
