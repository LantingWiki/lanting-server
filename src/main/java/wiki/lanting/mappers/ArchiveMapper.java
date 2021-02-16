package wiki.lanting.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Component;
import wiki.lanting.models.ArchiveEntity;

/**
 * @author wang.boyang
 */
@Component("archiveMapper")
public interface ArchiveMapper extends BaseMapper<ArchiveEntity> {
}
