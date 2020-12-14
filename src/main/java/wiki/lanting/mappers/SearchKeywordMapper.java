package wiki.lanting.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Component;
import wiki.lanting.models.SearchKeywordEntity;

/**
 * @author wang.boyang
 */
@Component("searchKeywordMapper")
public interface SearchKeywordMapper extends BaseMapper<SearchKeywordEntity> {

}
