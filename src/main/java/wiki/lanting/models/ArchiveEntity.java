package wiki.lanting.models;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wang.boyang
 */
@Data
@TableName("archives")
public class ArchiveEntity implements Serializable {

    @TableId
    public Long id;

    public Long createdAt;
    public Long updatedAt;
    public String title;
    public Integer publishYear;
    public Integer publishMonth;
    public String chapter;
}