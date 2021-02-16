package wiki.lanting.models;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wang.boyang
 */
@Data
public class CompiledArchivesEntity implements Serializable {
    public Map<Long, FullArchiveEntity> archives = new HashMap<>();
    public FieldFreqMapEntity fieldFreqMapEntity = new FieldFreqMapEntity();
}
