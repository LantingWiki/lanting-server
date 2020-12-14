package wiki.lanting.models;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wang.boyang
 */
@Data
@TableName("search_keywords")
public class SearchKeywordEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    public Long id;

    public String keyword;
    public int searchCount;
    public Long createdAt;
    public Long updatedAt;
}