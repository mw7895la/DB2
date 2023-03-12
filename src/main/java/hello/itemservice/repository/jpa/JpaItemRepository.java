package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@Transactional  /** JPA의 모든 데이터 변경(save나 update)은 트랜잭션 안에서 이뤄진다. */
public class JpaItemRepository implements ItemRepository {

    /** JPA에서는 의존관계 주입을 받아야 한다. */
    private final EntityManager em;
    //이게 바로 JPA다. 여기다 저장 및 조회같은것들 한다. 스프링이랑 통합된것이기 때문에 스프링에서 자동으로 만들어준다.
    //DataSource도 넣어주고 그런 세팅 과정도 다 해준다. 트랜잭션이나 데이터소스나 이런거 알아서 다~ 설정해주고 우리는 주입받아서 쓰기만 하면 된다.

    @Autowired
    public JpaItemRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public Item save(Item item) {
        em.persist(item);
        return item;
        //persist 하면, 매핑정보를 만들어서 DB에 저장한다. id값이 처음에는 없는데 IDENTITY조회해서 넣어준다
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item findItem = em.find(Item.class, itemId);//먼저 itemId로 찾고
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
        // JPA가 조회 시점에 스냅샷을 떠 놓고 어떤 데이터가 바뀌었는지 JPA가 다 안다 ! 이걸 언제 데이터베이스에 업데이트 쿼리를 날리냐?
        // 트랜잭션이 커밋되는 시점에  위 3줄 업데이트 쿼리를 만들어 데이터베이스에 날린다. 그리고 커밋된다.
        //@Transactional은 메소드 끝날때 디비에 커밋 날라가는것.

        //이제 update된거 저장을 해야될거 같은데 ...??? 안해도 된다 !!
    }

    @Override
    public Optional<Item> findById(Long id) {
        Item item = em.find(Item.class, id);//타입, pk
        return Optional.ofNullable(item);
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        // 하나를 조회할때는 find를 쓰면 되는데.
        String jpql = "select i from Item i";       //테이블을 대상으로 하는게 아닌 매핑된 Item 클래스라고 봐야한다.

        //JPA도 동적쿼리는 좀 힘들다.
        Integer maxPrice = cond.getMaxPrice();
        String itemName = cond.getItemName();

/*        //Item 은 우리가 엔티티 매핑해준 Item클래스다.
        // i  는 alias i다  ,  i는 엔티티 자체를 말하는 것.

        List<Item> result = em.createQuery(jpql, Item.class).getResultList();//반환 타입은 Item.class

        return result;*/

        if (StringUtils.hasText(itemName) || maxPrice != null) {
            jpql += " where";
        }
        boolean andFlag = false;
        if (StringUtils.hasText(itemName)) {
            jpql += " i.itemName like concat('%',:itemName,'%')";
            andFlag = true;
        }
        if (maxPrice != null) {
            if (andFlag) {
                jpql += " and";
            }
            jpql += " i.price <= :maxPrice";
        }
        log.info("jpql={}", jpql);
        TypedQuery<Item> query = em.createQuery(jpql, Item.class);
        if (StringUtils.hasText(itemName)) {
            query.setParameter("itemName", itemName);
        }
        if (maxPrice != null) {
            query.setParameter("maxPrice", maxPrice);
        }
        return query.getResultList();
    }


}
