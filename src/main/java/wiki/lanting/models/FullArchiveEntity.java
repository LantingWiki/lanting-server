package wiki.lanting.models;

import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wang.boyang
 */
@Data
public class FullArchiveEntity implements Serializable {
    public Long id;
    public Long createdAt;
    public Long updatedAt;
    public String title;
    public Integer publishYear;
    public Integer publishMonth;
    public String chapter;
    public List<String> author = new ArrayList<>();
    public List<String> tag = new ArrayList<>();
    public List<String> origs = new ArrayList<>();
    public String publisher;
    public String date;
    public String remarks;
    public Integer likes = 0;

    public FullArchiveEntity(
            ArchiveEntity archiveEntity,
            List<String> author, List<String> tag, List<String> origs,
            String publisher, String remarks, Integer likes) {

        BeanUtils.copyProperties(archiveEntity, this);
        this.author = author;
        this.tag = tag;
        this.origs = origs;
        this.publisher = publisher;
        this.remarks = remarks;
        this.likes = likes;
    }

    public FullArchiveEntity(ArchiveEntity archiveEntity) {
        BeanUtils.copyProperties(archiveEntity, this);
    }
}
