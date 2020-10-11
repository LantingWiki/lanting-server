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
@TableName("users")
public class UserEntity implements Serializable {

    public UserEntity(Long id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }

    public UserEntity(String nickname) {
        this.nickname = nickname;
    }

    public UserEntity() {
    }

    @TableId(type = IdType.AUTO)
    public Long id;

    public String nickname;
}