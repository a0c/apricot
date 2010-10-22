package ui.base;

import base.SourceLocation;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Anton Chepurov
 */
public class HLDD2VHDLMapping {

	Map<? super AbstractItem, SourceLocation> mapping = new HashMap<AbstractItem, SourceLocation>();

	public void addMapping(AbstractItem newItem, SourceLocation lines) {
		mapping.put(newItem, lines);
	}

	public SourceLocation getSourceFor(Collection<? extends AbstractItem> uncoveredItems) {
		SourceLocation location = null;
		for (AbstractItem uncoveredItem : uncoveredItems) {
			if (mapping.containsKey(uncoveredItem)) {
				SourceLocation uncoveredLocation = mapping.get(uncoveredItem);
				if (location == null) {
					location = uncoveredLocation;
				} else {
					location = location.addSource(uncoveredLocation);
				}
			}
		}
		return location;
	}

	public SourceLocation getAllSources() {
		SourceLocation location = null;
		for (SourceLocation source : mapping.values()) {
			if (location == null) {
				location = source;
			} else {
				location = location.addSource(source);
			}
		}
		return location;
	}
}
