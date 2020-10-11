package wiki.lanting;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * @author wang.boyang
 */
@Slf4j
@EnableCaching
@SpringBootApplication
@MapperScan(basePackages = {"wiki.lanting.mappers"})
public class LantingServerApplication {


	@Autowired
	private KafkaTemplate<String, String> template;

	public void run(String... args) {
		log.info("producer");
		this.template.send("myTopic", "foo1");
		this.template.send("myTopic", "foo2");
		this.template.send("myTopic", "foo3");
	}

	@KafkaListener(topics = "myTopic")
	public void listen(ConsumerRecord<?, ?> cr) {
		log.info("consumer: {}", cr.toString());
	}

	public static void main(String[] args) {
		SpringApplication.run(LantingServerApplication.class, args);
	}
}
