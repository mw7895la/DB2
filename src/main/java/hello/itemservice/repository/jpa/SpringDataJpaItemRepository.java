package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataJpaItemRepository extends JpaRepository<Item,Long> {
    //얘가 어떤것을 관리하는 Repository냐  Item 클래스고, Long은 pk의 타입이다.

    List<Item> findByItemNameLike(String itemName);     //where itemName = like('%itemName%') 이런식

    List<Item> findByPriceLessThanEqual(Integer price);

    //위 2개 같이 복합으로 조회하는 것   ( 아래 메서드와 같은 기능 수행 )
    List<Item> findByItemNameLikeAndPriceLessThanEqual(String itemName, Integer price);

    //쿼리 직접 실행
    @Query("select i from Item i where i.itemName like :itemName and i.price <= :price")
    List<Item> findItems(@Param("itemName") String itemName, @Param("price") Integer price);
    //@Param org.springframework.data.repository.query.Param
}
