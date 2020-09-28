package jpabook.jpashop.service;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemService itemService;

    /**
     * 주문
     */
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {
        //엔티티 조회
        Member member = memberRepository.findOne(memberId);
        Item item = itemService.findOne(itemId);

        //배송정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());

        //주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        //주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);

        //주문 저장
        /*
         * Order 클래스에 있는 cascade 속성
         *
            @OneToMany(mappedBy = "order", cascade = ALL)
            private List<OrderItem> orderItems = new ArrayList<>();

            @OneToOne(fetch = LAZY, cascade = ALL)
            @JoinColumn(name = "delivery_id")
            private Delivery delivery;
         *
         * orderItems 와 delivery 에 대해 cascade 속성이 ALL 로 되어 있기 때문에
         * order 하나만 저장해도 전파되어 함께 저장.
         *
         * cascade 속성은 가능하면 구조가 단순하고, 라이프 사이클이 동일하며
         * 자원에 액세스하는 경로가 하나인 경우에 고려해야 운영상에 복잡도가 증가하지 않음.
         * └─> 어디서 전파되어 수정 되었는지 역추적이 어려움.
         */
        orderRepository.save(order);

        return order.getId();
    }

    /**
     * 주문 취소
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        //주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);

        //주문 취소
        order.cancel();
    }
    
    /**
     * 주문 검색
     */
    public List<Order> findOrders(OrderSearch orderSearch) {
        return orderRepository.findAllByString(orderSearch);
    }
}
