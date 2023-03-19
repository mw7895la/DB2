package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * NamedParameterJdbcTemplate
 * SqlParameterSource
 * - BeanPropertySqlParameterSource
 * - MapSqlParameterSource
 *
 * Map
 *
 * BeanPropertyRowMapper
 */
@Slf4j
@Repository
public class JdbcTemplateItemRepositoryV2 implements ItemRepository {

//    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate template;      //이러면 파라미터를 순서가 아닌 이름 기반으로 바인딩 할 수 있다.
    public JdbcTemplateItemRepositoryV2(DataSource dataSource) {
        this.template = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public Item save(Item item) {
        //이름 기반의 sql 문
        String sql = "insert into item(item_name, price , quantity) values(:itemName,:price,:quantity)";

        SqlParameterSource param = new BeanPropertySqlParameterSource(item);
        //파라미터로 넘어온 item객체를 가지고 Item 클래스의 필드들과 이름이 같은 파라미터(param)를 만든다
        //자바빈 프로퍼티 규약을 통해서 자동으로 파라미터 객체를 생성한다고 보면 된다. ex) key = itemName //value = 넘어온 item에 들어있는 itemName 값.

        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql,param,keyHolder);

        //keyholder를 넘겼기 때문에 꺼낼 수 있다.
        long Key = keyHolder.getKey().longValue();
        item.setId(Key);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "update item set item_name=:itemName, price=:price, quantity=:quantity where id=:id";
        
        //save() 에서 처럼  SqlParameterSource param = new BeanPropertySqlParameterSource(item); 을 쓸 수도 있고(근데 id를 처리하기 힘들다 ItemUpdateDto는 id가 없다), 아래처럼도 가능하다
        //SqlParameterSource 은 sql 파라미터를 관리해주는 인터페이스 인데, MapSqlParameterSource 이 구현체다
        // . . . 찍은게 메서드 체인.
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("itemName", updateParam.getItemName())
                .addValue("price", updateParam.getPrice())
                .addValue("quantity", updateParam.getQuantity())
                .addValue("id", itemId);

        template.update(sql, param);

    }

    @Override
    public Optional<Item> findById(Long id) {
        String sql = "select id, item_name, price ,quantity from item where id =:id";
        try {
            Map<String, Object> param = Map.of("id", id);       //key는 id value는 id  , 위에 :id와 key가 매핑이 되고  value인 id로 치환이 된다.
            Item item = template.queryForObject(sql, param,itemRowMapper());//param은 2번째 파라미터로 들어간다. Map으로도 쓸 수 있음.

            //결과 resultSet을 item 객체로 바꾸는 코드가 필요  , queryForObject는 결과 하나 가져올때 사용
            //queryForObject는 결과가 없으면 Exception이 터진다.
            //itemRowMapper()를 통해서 실행하면 데이터에 대한 매핑결과를 받아서 item으로 받은것
            return Optional.of(item);
        }catch(EmptyResultDataAccessException e){
            return Optional.empty(); //데이터 결과 없으면 이걸 리턴.
        }

        /* 익명 구현 객체로 하는 방법. 물론 예외는 해줘야함.
        Item item = template.queryForObject(sql, new RowMapper<Member>(){
            @Override
            public Item mapRow(ResultSet rs, int rowNum) throws SQLException {
                Item item = new Item();
                item.setId(rs.getLong("id"));
                item.setItemName(rs.getString("item_name"));
                item.setPrice(rs.getInt("price"));
                item.setQuantity(rs.getInt("quantity"));
                return item;
            }
        }, id);*/
    }

    private RowMapper<Item> itemRowMapper() {
        //데이터베이스의 조회 결과를 객체로 변환할 때 사용. 한개 이상의 결과같은 경우 list를 뽑아주는 코드가 숨겨져있다.
        //RowMapper에서 만들어진 User 오브젝트는 템플릿이 미리 준비한 List 컬렉션에 추가되며,
        //작업을 마치면 모든 로우에 대한 User 오브젝트를 담고 있는 List 오브젝트가 리턴된다
        /*return ((rs,rowNum)-> {
            Item item = new Item();
            item.setId(rs.getLong("id"));
            item.setItemName(rs.getString("item_name"));
            item.setPrice(rs.getInt("price"));
            item.setQuantity(rs.getInt("quantity"));
            return item;
        });*/

        return BeanPropertyRowMapper.newInstance(Item.class);       //camel 표기법 지원 ex) itemName // 언더스코어 표기법을 카멜로도 변환해준다 item_name -> itemName
        //위에것을 스프링이 제공하는 BeanPropertyRowMapper를 사용해서 Item.class 넣어주면 끝
        //그러면 resultSet 가지고  Item클래스의 각 필드 이름으로 해서 데이터를 set하고 만들어서 리턴. (자바 빈 프로퍼티)
        //BeanPropertyRowMapper 는 RowMapper 인터페이스의 구현객체

    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();      //item Search시 사용한 조건들.

        SqlParameterSource param = new BeanPropertySqlParameterSource(cond);
        //여기도 cond을 가지고 param에 데이터를 그대로 옮기는 것. ItemSearchCond와 필드 이름 같게

        String sql = "select id, item_name, price ,quantity from item";
        //동적 쿼리를 작성해야 되는데..
        if (StringUtils.hasText(itemName) || maxPrice != null) {
            sql += " where";
        }
        boolean andFlag = false;

        if (StringUtils.hasText(itemName)) {
            sql += " item_name like concat('%',:itemName,'%')";

            andFlag = true;
        }

        if (maxPrice != null) {
            if (andFlag) {
                sql += " and";
            }
            sql += " price <= :maxPrice";

        }
        log.info("sql={}", sql);

        return template.query(sql,param, itemRowMapper());   //query() 는 리스트 같은것들 가져올때 사용


    }
}
