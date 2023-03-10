package hello.itemservice.repository.mybatis;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MyBatisItemRepository implements ItemRepository {  //단순 Mapper에 위임하는 기능이라 보면된다.

    private final ItemMapper itemMapper;        
    //우리가 만든 인터페이스인 ItemMapper는 @Mapper 어노테이션이 붙어있다. 이러면 마이바티스 스프링 모듈에서 자동으로 인식하고
    //마이바티스 스프링 모듈이 만들어낸 동적 프록시 구현체를 만들어내서(메소드 읽어서 xml호출하는 등.) 스프링 빈에 등록해준다. 그래서 의존관계 주입을 받을 수 있던 것.

    @Override
    public Item save(Item item) {
        log.info("itemMapper class ={}", itemMapper.getClass());
        itemMapper.save(item);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        itemMapper.update(itemId, updateParam);
    }

    @Override
    public Optional<Item> findById(Long id) {
        return itemMapper.findById(id);
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        return itemMapper.findALl(cond);
    }
}
