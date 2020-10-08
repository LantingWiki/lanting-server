package wiki.lanting.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import wiki.lanting.controllers.UserController.CreateUserRequestBody;
import wiki.lanting.mappers.UserMapper;
import wiki.lanting.models.UserEntity;

/**
 * @author wang.boyang
 */
@Slf4j
@Service
public class UserService {

    final JdbcTemplate jdbcTemplate;
    final UserMapper userMapper;

    public UserService(JdbcTemplate jdbcTemplate, UserMapper userMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userMapper = userMapper;
    }

    public UserEntity getUserById(long id) {
        log.info("in getUserById {}", id);
//        List<UserEntity> result = jdbcTemplate.query("select * from abe.users", (rs, rowNum) -> {
//            log.info("in row mapper: {} {}", rs, rowNum);
//            UserEntity userEntity = new UserEntity();
//            userEntity.id = rs.getLong(1);
//            return userEntity;
//        });
//        return result.size() > 0 ? result.get(0) : null;
        UserEntity userEntity = userMapper.selectById(id);
        return userEntity;
    }

    public UserEntity createUser(CreateUserRequestBody createUserRequestBody) {
        UserEntity userEntity = new UserEntity();
        userEntity.nickname = createUserRequestBody.nickname;
        int insert = userMapper.insert(userEntity);
        log.info("in createUser: {}", insert);
        return userEntity;
    }
}
