package hello.itemservice;

import hello.itemservice.config.*;
import hello.itemservice.repository.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Slf4j
//@Import(MemoryConfig.class)
@Import(MyBatisConfig.class)
@SpringBootApplication(scanBasePackages = "hello.itemservice.web")    //이 Web이하만 컴포넌트 스캔하고 나머진 수동 등록하겠다 !
public class ItemServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ItemServiceApplication.class, args);
	}

	@Bean
	@Profile("local")
	public TestDataInit testDataInit(ItemRepository itemRepository) {
		return new TestDataInit(itemRepository);
	}
	//빈으로 등록을 해줘야 @EventListener가 동작한다 @Profile은 뭐냐? 로컬pc 운영환경 테스트실행 등 다양한 환경에 따라 다른 설정을 할 때 사용한다.
	//application.properties에서 spring.profiles.active=local 의 값을 다른걸로 하면 초기 데이터 값이 없다.

/*
	@Bean
	@Profile("test")
	public DataSource dataSource(){
		//여기가 동작하는 경우에는 내가 데이터소스를 직접 등록해서 사용하겠다.
		log.info("메모리 데이터베이스 초기화");
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.h2.Driver");	//h2 데이터베이스 드라이버를 지정해준것.
		dataSource.setUrl("jdbc:h2:mem:db;DB_CLOSE_DELAY=-1");		//위 드라이버를 쓰겠다는 것.
		dataSource.setUsername("sa");
		dataSource.setPassword("");
		return dataSource;
		//mem 으로 되어있으면 JVM내에 데이터베이스를 만들고 거기에 데이터를 쌓는다.
	}*/
}
