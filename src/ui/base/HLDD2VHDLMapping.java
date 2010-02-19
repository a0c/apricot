package ui.base;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 18.12.2008
 * <br>Time: 1:38:12
 */
public class HLDD2VHDLMapping {
    Map<? super AbstractItem, Collection<Integer>> mapping = new HashMap<AbstractItem, Collection<Integer>>();


    public void addMapping(AbstractItem newItem, Collection<Integer> lines) {
        mapping.put(newItem, lines);
    }


    public Collection<Integer> getLinesFor(Collection<? extends AbstractItem> uncoveredItems) {
        Collection<Integer> lines = new HashSet<Integer>();
        for (AbstractItem uncoveredItem : uncoveredItems) {
            if (mapping.containsKey(uncoveredItem)) {
                lines.addAll(mapping.get(uncoveredItem));
            }
        }
        return lines;
    }
}
