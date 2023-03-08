package hello.itemservice.config;


import hello.itemservice.repository.ItemRepository;

import hello.itemservice.repository.jdbctemplate.JdbcTemplateItemRepositoryV2;
import hello.itemservice.service.ItemService;
import hello.itemservice.service.ItemServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor        //dataSource 주입받으려고.
public class JdbcTemplateV2Config {

    private final DataSource dataSource;      //이건 스프링이 애초에 등록해놓은 것이다.

    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(itemRepository());
    }

    @Bean
    public ItemRepository itemRepository() {
        return new JdbcTemplateItemRepositoryV2(dataSource);
    }
}
