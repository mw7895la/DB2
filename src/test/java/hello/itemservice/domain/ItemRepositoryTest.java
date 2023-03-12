package hello.itemservice.domain;

import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import hello.itemservice.repository.memory.MemoryItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.PlatformTransactionManager;

import org.springframework.transaction.annotation.Transactional;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Transactional
@SpringBootTest
class ItemRepositoryTest {

    @Autowired
    ItemRepository itemRepository;      //이미 ItemRepository를 구현한 MemoeryItemRepository가 있기 때문에 그게 주입이 될 것.

/*    @Autowired
    PlatformTransactionManager transactionManager;
    //DataSource와 PlatformTransactionManager는 스프링 부트가 자동으로 빈으로 등록한다고 했었다.
    TransactionStatus status;

    @BeforeEach
    void beforEach() {
        //트랜잭션 시작
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());
    }*/

    @AfterEach
    void afterEach() {
        //MemoryItemRepository 의 경우 제한적으로 사용
        //인터페이스에는 clearStore()가 없기 때문에 이렇게 했다.

        if (itemRepository instanceof MemoryItemRepository) {
            ((MemoryItemRepository) itemRepository).clearStore();
        }

        //트랜잭션 롤백
        //transactionManager.rollback(status);
    }

    //@Commit
    @Test
    void save() {
        //given
        Item item = new Item("itemA", 10000, 10);

        //when
        Item savedItem = itemRepository.save(item);

        //then
        Item findItem = itemRepository.findById(item.getId()).get();
        assertThat(findItem).isEqualTo(savedItem);
    }

    @Test
    @Commit
    void updateItem() {
        //given
        Item item = new Item("item1", 10000, 10);
        Item savedItem = itemRepository.save(item);
        Long itemId = savedItem.getId();

        //when
        ItemUpdateDto updateParam = new ItemUpdateDto("item2", 20000, 30);
        itemRepository.update(itemId, updateParam);

        //then
        Item findItem = itemRepository.findById(itemId).get();
        assertThat(findItem.getItemName()).isEqualTo(updateParam.getItemName());
        assertThat(findItem.getPrice()).isEqualTo(updateParam.getPrice());
        assertThat(findItem.getQuantity()).isEqualTo(updateParam.getQuantity());
    }

    //여기는 DB에 데이터가 없다고 가정하고 검증하는 부분이라는 것.
    @Test
    void findItems() {
        //given
        Item item1 = new Item("itemA-1", 10000, 10);
        Item item2 = new Item("itemA-2", 20000, 20);
        Item item3 = new Item("itemB-1", 30000, 30);

        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);

        //둘 다 없음 검증
        test(null, null, item1, item2, item3);
        //이 부분에서 현재 내가 예제 작성하면서 데이터가 많이 쌓여있다. 그런데 Name과 price가 null이니 아마 데이터를 다 가져올 것이다.
        //그래서 아래 test() 에서 기대한것은 3개일 텐데 너무 많아서 그런것.
        //즉 이 테스트는 위에 저장한 3개가 디비에 있는지 검증하는 것이었다.
        test("", null, item1, item2, item3);

        //itemName 검증
        test("itemA", null, item1, item2);
        test("temA", null, item1, item2);
        test("itemB", null, item3);

        //maxPrice 검증
        test(null, 10000, item1);

        //둘 다 있음 검증
        test("itemA", 10000, item1);
    }

    void test(String itemName, Integer maxPrice, Item... items) {
        List<Item> result = itemRepository.findAll(new ItemSearchCond(itemName, maxPrice));
        //검색조건이 없으면 , 전부다 출력이다.
        assertThat(result).containsExactly(items); //exactly면 순서도 다 맞아야 한다 //findItems()에서 save할 때 item1,2,3 순으로 했었기 때문.
    }
}
