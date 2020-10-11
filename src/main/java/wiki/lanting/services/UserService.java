package wiki.lanting.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import wiki.lanting.mappers.UserMapper;
import wiki.lanting.models.UserEntity;

import java.util.List;
import java.util.Map;

/**
 * @author wang.boyang
 */

@Slf4j
@Service
public class UserService {

    final RedisTemplate<String, String> redisTemplate;
    final JdbcTemplate jdbcTemplate;
    final UserMapper userMapper;

    public UserService(JdbcTemplate jdbcTemplate, UserMapper userMapper, RedisTemplate<String, String> redisTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
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

    public Boolean massCreateUser(Integer count) {
        // send a message to Kafka
        return null;
    }
}
