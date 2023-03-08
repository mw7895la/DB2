package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * JDBC 템플릿
 */
@Slf4j
@Repository
public class JdbcTemplateItemRepositoryV1 implements ItemRepository {

    private final JdbcTemplate template;

    public JdbcTemplateItemRepositoryV1(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    @Override
    public Item save(Item item) {
        String sql = "insert into item(item_name, price , quantity) values(?,?,?)";
        //id를 안넣었기 때문에 자동 생성하는 값을 만들어 줘야하고 그 값을 세팅해서 반환해야돼.
        //Jdbc 템플릿을 쓸 때 DB에서 생성해준 id 값을 가져오려면 KeyHolder 필요
        KeyHolder keyHolder = new GeneratedKeyHolder();     //DB에서 만든 id값을 select 한 것.

        //tempalte.update는 영향받은 로우 수를 반환한다
        template.update(connection-> {
            //커넥션 넘기고 keyHolder 넘기고..   keyHolder 때문에 로직을 좀 더 넣어야 한다. 그래서 아래처럼 한 것.

            //자동 증가 키
            PreparedStatement pstmt = connection.prepareStatement(sql, new String[]{"id"});
            //column name인 id  2번째 인자 new String[]{"id}는 자동 생성되는 키 컬럼 목록을 지정할 때 사용,
            //INSERT 할 때 생성된 id값이 뒤에서 keyHolder 객체로 넘겨져, getKey() 메서드로 이 값을 꺼내어 사용할 수 있게 된다.
            pstmt.setString(1, item.getItemName());
            pstmt.setInt(2, item.getPrice());
            pstmt.setInt(3, item.getQuantity());
            return pstmt;       //return 된 pstmt를 가지고 jdbctemplate이 실행하겠지.
        },keyHolder);           //여기까지가 데이터베이스 insert


        //keyholder를 넘겼기 때문에 꺼낼 수 있다.
        long Key = keyHolder.getKey().longValue();
        item.setId(Key);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "update item set item_name=?, price=?, quantity=? where id=?";

        template.update(sql, updateParam.getItemName(), updateParam.getPrice(), updateParam.getQuantity(), itemId);
        //sql문을 가지고 template로 가서 쿼리를 수행한다.
    }

    @Override
    public Optional<Item> findById(Long id) {
        String sql = "select id, item_name, price ,quantity from item where id =?";
        try {
            Item item = template.queryForObject(sql, itemRowMapper(), id);
            //첫 번째 파라미터 sql문과 RowMapper에서 수행된걸 가지고 JdbcTemplate의 queryForObject로 간다.
            //거기서도 결국 query() 메소드로 가서 sql로 doInstatement를 수행한다. sql 쿼리 수행후 rs에 담고 그 rs를 RowMapper에 넣어 추출 후 return 한다.
            //결과 resultSet을 item 객체로 바꾸는 코드가 필요  , queryForObject는 결과 하나 가져올때 사용
            //queryForObject는 결과가 없으면 Exception이 터진다.
            //itemRowMapper()를 통해서 실행하면 데이터에 대한 매핑결과를 받아서 item으로 받은것
            return Optional.of(item);
        }catch(EmptyResultDataAccessException e){
            return Optional.empty(); //데이터 결과 없으면 이걸 리턴.
        }

        /* 익명 구현 객체로 하는 방법. 물론 예외는 해줘야함.
        //RowMapper 의 mapRow는 오버라이드 된 여기가 실행 될 것.
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
        //RowMapper는 데이터베이스의 반환 결과인 ResultSet을 객체로 변환해주는 클래스이다.. 한개 이상의 결과같은 경우 list를 뽑아주는 코드가 숨겨져있다.
        //RowMapper에서 만들어진 User 오브젝트는 템플릿이 미리 준비한 List 컬렉션에 추가되며,
        //작업을 마치면 모든 로우에 대한 User 오브젝트를 담고 있는 List 오브젝트가 리턴된다
        return ((rs,rowNum)-> {
            Item item = new Item();
            item.setId(rs.getLong("id"));
            item.setItemName(rs.getString("item_name"));
            item.setPrice(rs.getInt("price"));
            item.setQuantity(rs.getInt("quantity"));
            return item;
        });
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();      //item Search시 사용한 조건들.

        String sql = "select id, item_name, price ,quantity from item";
        //동적 쿼리를 작성해야 되는데..
        if (StringUtils.hasText(itemName) || maxPrice != null) {
            sql += " where";
        }
        boolean andFlag = false;
        List<Object> param = new ArrayList<>();

        if (StringUtils.hasText(itemName)) {
            sql += " item_name like concat('%',?,'%')";
            param.add(itemName);
            andFlag = true;
        }

        if (maxPrice != null) {
            if (andFlag) {
                sql += " and";
            }
            sql += " price <= ?";
            param.add(maxPrice);
        }
        log.info("sql={}", sql);

        return template.query(sql, itemRowMapper(),param.toArray());   //query() 는 리스트 같은것들 가져올때 사용


    }
}
