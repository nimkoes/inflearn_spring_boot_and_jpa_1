package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item) {
        if (item.getId() == null) {
            /*
             * item.getId() 결과가 null 이라는 것은
             * generate value 인 id가 없다는 것이므로
             * 아예 새로 생성한 객체임을 뜻함.
             * 그렇기 때문에 아예 새로 persist 작업 수행, 신규 등록.
             */
            em.persist(item);
        } else {
            /*
             * id 값이 있다는것은 데이터에비스에 저장되어 있는 값임을 뜻함.
             */
            em.merge(item);
        }
    }

    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }
}
