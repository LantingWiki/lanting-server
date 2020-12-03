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
public class ArchiveBasicInfoEntity implements Serializable {

    public String title;

    public String author;

    public String publisher;

    public String date;
}