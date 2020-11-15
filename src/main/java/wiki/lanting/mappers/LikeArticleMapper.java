package wiki.lanting.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Component;
import wiki.lanting.models.LikeArticleEntity;

/**
 * @author wang.boyang
 */
@Component("likeArticleMapper")
public interface LikeArticleMapper extends BaseMapper<LikeArticleEntity> {

}
