package jpabook.jpashop;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

@Component
@RequiredArgsConstructor
public class InitDb {
    
    private final InitService initService;
    
    @PostConstruct
    public void init() {
        initService.dbIniit1();
        initService.dbIniit2();
        
    }
    
    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {
    
        private final EntityManager em;
    
        public void dbIniit1() {
            Member member = createMember("userA", "서울", "1", "1111");
            em.persist(member);
    
            Book book1 = createBook("JAP1 BOOK", 10000, 100);
            em.persist(book1);
    
            Book book2 = createBook("JAP2 BOOK", 20000, 100);
            em.persist(book2);
    
            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 2);
    
            Delivery delivery = createDelivery(member);
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);
            
        }
    
        public void dbIniit2() {
            Member member = createMember("userB", "진주", "2", "2222");
            em.persist(member);
    
            Book book1 = createBook("SPRING1 BOOK", 20000, 200);
            em.persist(book1);
    
            Book book2 = createBook("SPRING2 BOOK", 40000, 300);
            em.persist(book2);
        
            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 20000, 3);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 40000, 4);
    
            Delivery delivery = createDelivery(member);
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);
        
        }
    
        private Delivery createDelivery(Member member) {
            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());
            return delivery;
        }
    
        private Book createBook(String name, int price, int stockQuantity) {
            Book book1 = new Book();
            book1.setName(name);
            book1.setPrice(price);
            book1.setStockQuantity(stockQuantity);
            return book1;
        }
    
        private Member createMember(String name, String coty, String street, String zipcode) {
                Member member = new Member();
                member.setName(name);
            member.setAddress(new Address(coty, street, zipcode));
            return member;
        }
    }
    
}
