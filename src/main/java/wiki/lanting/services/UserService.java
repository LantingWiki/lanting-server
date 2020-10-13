package wiki.lanting.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import wiki.lanting.mappers.UserMapper;
import wiki.lanting.models.UserEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author wang.boyang
 */

@Slf4j
@Service
public class UserService {

    public static final String USER_SERVICE_KAFKA_TOPIC = "wiki-lanting-services-UserService";
    final KafkaTemplate<String, String> template;
    final RedisTemplate<String, String> redisTemplate;
    final JdbcTemplate jdbcTemplate;
    final UserMapper userMapper;

    public UserService(JdbcTemplate jdbcTemplate, UserMapper userMapper, RedisTemplate<String, String> redisTemplate, KafkaTemplate<String, String> template) {
        this.jdbcTemplate = jdbcTemplate;
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
        this.template = template;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaManualAckListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(new HashMap<>(8) {{
            this.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            this.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            this.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        }}));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    @KafkaListener(topics = USER_SERVICE_KAFKA_TOPIC, containerFactory = "kafkaManualAckListenerContainerFactory", groupId = "lanting-group")
    public void listen(ConsumerRecord<String, String> cr, Acknowledgment ack) throws JsonProcessingException {
        log.info("consumerRecord: {}", cr.toString());
        @SuppressWarnings("Convert2Diamond")
        List<UserEntity> userEntities = new ObjectMapper().readValue(cr.value(),
                new TypeReference<List<UserEntity>>() {
                });
        //TODO: 创建用户, 并更新redis, 减少users to create count
        int toCreateUserCount = userEntities.size();
        log.info("total users to create {}", toCreateUserCount);
        for (int i =0;i<toCreateUserCount;i++) {
            this.createUser(userEntities.get(i));
            redisTemplate.opsForValue().set("userPendingCreate", Integer.toString(toCreateUserCount-i-1));
            log.info("user pending Creation {}", toCreateUserCount-i-1);
        }
        //2
        ack.acknowledge();
        //3
    }

    /**
     * 使用JDBC:
     *   List<UserEntity> result = jdbcTemplate.query("select * from abe.users", (rs, rowNum) -> {
     *   log.info("in row mapper: {} {}", rs, rowNum);
     *   UserEntity userEntity = new UserEntity();
     *   userEntity.id = rs.getLong(1);
     *   return userEntity;
     *   });
     *   return result.size() > 0 ? result.get(0) : null;
     *
     * 使用RedisTemplate
     *   Integer test1 = redisTemplate.opsForValue().append("test1", "111");
     *   log.error("test1 {}", test1);
     */
    public UserEntity readUser(long id) {
        UserEntity userEntity = userMapper.selectById(id);
        log.info("in readUser, id: {}, user: {}", id, userEntity);
        return userEntity;
    }

    public UserEntity createUser(UserEntity userEntity) {
        int insert = userMapper.insert(userEntity);
        log.info("in createUser, user: {}", insert);
        return userEntity;
    }

    public int updateUser(UserEntity userEntity) {
        int result = userMapper.updateById(userEntity);
        log.info("in updateUser, result: {}, user: {}", result, userEntity);
        return result;
    }

    public int deleteUser(long id) {
        int result = userMapper.deleteById(id);
        log.info("in updateUser, result: {}, userid: {}", result, id);
        return result;
    }

    public int countUser() {
        int result = userMapper.selectCount(null);
        log.info("in countUser, result: {}", result);
        return result;
    }

    @Cacheable(value = "wiki.lanting.services.UserService.searchUser", key = "#userEntity.nickname")
    public List<UserEntity> searchUser(UserEntity userEntity) {
        List<UserEntity> results = userMapper.selectByMap(Map.of("nickname", userEntity.nickname));
        log.info("in searchUser, results: {}, nickname: {}", results, userEntity.nickname);
        return results;
    }

    public Boolean massCreateUser(List<UserEntity> userEntities) throws JsonProcessingException {
        // send a message to Kafka
        ListenableFuture<SendResult<String, String>> send = this.template.send(
                USER_SERVICE_KAFKA_TOPIC, new ObjectMapper().writeValueAsString(userEntities));
        try {
            SendResult<String, String> sendResult = send.get();
            log.info("in massCreateUser, sent: {}, metadata: {}",
                    sendResult.getProducerRecord(), sendResult.getRecordMetadata());
            //TODO: save to redis, we now have X new users to create
            return true;
        } catch (ExecutionException | InterruptedException e) {
            log.error("in massCreateUser", e);
            return false;
        }
    }

    public int checkPendingCreation() {
        String result =  redisTemplate.opsForValue().get("userPendingCreate");
        log.info("remained users to create: {}", result);
        if (result!=null){
            return Integer.parseInt(result);
        }else {
            return 0;
        }
    }
}
