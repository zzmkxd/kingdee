package plugins.graph;
import java.math.BigDecimal;
import java.util.*;

public class KeyValueList {
    private List<Map.Entry<Object, Object>> entries;

    public KeyValueList() {
        this.entries = new ArrayList<>();
    }

    // 添加键值对
    public void add(Object key, Object value) {
        Objects.requireNonNull(key, "Key cannot be null");
        entries.add(new AbstractMap.SimpleEntry<>(key, value));
    }

    // 根据键获取值
    public Object get(Object key) {
        for (Map.Entry<Object, Object> entry : entries) {
            if (entry.getKey().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    // 检查是否包含键
    public boolean containsKey(Object key) {
        for (Map.Entry<Object, Object> entry : entries) {
            if (entry.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }

    // 获取所有键值对
    public List<Map.Entry<Object, Object>> getAllEntries() {
        return new ArrayList<>(entries); // 返回副本以保护内部数据
    }

    // 获取列表大小
    public int size() {
        return entries.size();
    }

    // 移除键值对
    public boolean remove(Object key) {
        return entries.removeIf(entry -> entry.getKey().equals(key));
    }

    // 清空列表
    public void clear() {
        entries.clear();
    }

    // 转换为 List<Map<Object, Object>> 格式
    public List<Map<Object, Object>> toListOfMaps() {
        List<Map<Object, Object>> data = new ArrayList<>();

        for (Map.Entry<Object, Object> entry : entries) {
            Map<Object, Object> item = new HashMap<>();
            item.put("category", entry.getKey());   // 键作为 "category"
            BigDecimal number = new BigDecimal(String.valueOf(entry.getValue()));
            // 2. 乘以 10
            BigDecimal result = number.multiply(BigDecimal.TEN);
            // 3. 转换为字符串，并去除不必要的 .0（可选）
            String resultStr = result.stripTrailingZeros().toPlainString();
            item.put("amount", resultStr);   // 值作为 "amount"
            data.add(item);
        }

        return data;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Object, Object> entry : entries) {
            sb.append("{").append(entry.getKey()).append(entry.getValue()).append("}");
        }
        return sb.toString();
    }
}