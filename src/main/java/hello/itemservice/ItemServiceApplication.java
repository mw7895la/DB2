package hello.itemservice;

import hello.itemservice.config.*;
import hello.itemservice.repository.ItemRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

//@Import(MemoryConfig.class)
@Import(JdbcTemplateV1Config.class)
@SpringBootApplication(scanBasePackages = "hello.itemservice.web")	//이 Web이하만 컴포넌트 스캔하고 나머진 수동 등록하겠다 !
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


}
