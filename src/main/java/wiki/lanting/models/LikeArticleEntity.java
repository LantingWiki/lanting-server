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
@TableName("article_likes")
public class LikeArticleEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    public Long id;

    public Long articleId;
    public String clientId;
    public Long createdAt;
    public boolean isLike;
}