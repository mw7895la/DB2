package hello.itemservice.repository.jpa;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hello.itemservice.domain.Item;
import hello.itemservice.domain.QItem;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static hello.itemservice.domain.QItem.item;


@Repository
@Transactional
public class JpaItemRepositoryV3 implements ItemRepository {
    /**
     * QueryDSL 사용
     */
    private final EntityManager em;
    private final JPAQueryFactory query;    //JPA를 쓰려면 JPAQueryFactory가 필요하다.

    public JpaItemRepositoryV3(EntityManager em) {
        this.em = em;
        this.query = new JPAQueryFactory(em);       //JPAQeuryFactory는 QueryDSL인데 거기 안에 EntityManager를 넣어준다.
    }

    @Override
    public Item save(Item item) {
        em.persist(item);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item findItem = em.find(Item.class, itemId);//먼저 itemId로 찾고
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
    }

    @Override
    public Optional<Item> findById(Long id) {
        Item item = em.find(Item.class, id);//타입, pk
        return Optional.ofNullable(item);
    }


    public List<Item> findAllOld(ItemSearchCond cond) {
        /**
         * Query DSL
         */
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        QItem item = QItem.item;
        BooleanBuilder builder = new BooleanBuilder();      //조건에 따라 넣기위해.
        if (StringUtils.hasText(itemName)) {
            builder.and(item.itemName.like("%" + itemName + "%"));      //item은 queryDSL의 item
        }
        if (maxPrice != null) {
            builder.and(item.price.loe(maxPrice));      //loe는 작거나 같다의 뜻.
        }

        List<Item> result = query.select(item).from(item).where(builder)
                .fetch();

        return result;
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        /**
         * Query DSL
         */
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        QItem item = QItem.item;
      //조건에 따라 넣기위해.


        List<Item> result = query.select(item).from(item).where(likeItemName(itemName), maxPrice(maxPrice))
                .fetch();                                       //아래의 함수에 itemName을 넣어서 반환

        return result;
    }

    private BooleanExpression likeItemName(String itemName) {
        if (StringUtils.hasText(itemName)) {
            return item.itemName.like("%" + itemName + "%");      //item은 queryDSL의 item
        }
        return null;
    }

    private Predicate maxPrice(Integer maxPrice) {
        if (maxPrice != null) {
           return item.price.loe(maxPrice);      //loe는 작거나 같다의 뜻.
        }
        return null;
    }
}
