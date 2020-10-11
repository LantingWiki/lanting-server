package wiki.lanting;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @author wang.boyang
 */
@EnableCaching
@SpringBootApplication
@MapperScan(basePackages = {"wiki.lanting.mappers"})
public class LantingServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(LantingServerApplication.class, args);
	}
}
