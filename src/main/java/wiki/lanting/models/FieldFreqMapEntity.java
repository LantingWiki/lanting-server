package wiki.lanting.models;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

@Data
public class FieldFreqMapEntity implements Serializable {
    public Map<String, Integer> author = new HashMap<>();
    public Map<String, Integer> publisher = new Hashtable<>();
    public Map<String, Integer> date = new Hashtable<>();
    public Map<String, Integer> tag = new Hashtable<>();
}
