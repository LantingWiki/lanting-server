package wiki.lanting.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Component;
import wiki.lanting.models.UserEntity;

/**
 * @author wang.boyang
 */
@Component("userMapper")
public interface UserMapper extends BaseMapper<UserEntity> {
}
