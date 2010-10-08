package base;

import java.util.LinkedList;

/**
 * @author Anton Chepurov
 */
public class HierarchyLocation {

	private final LinkedList<String> locations = new LinkedList<String>();
	
	public void addLocation(String location) {

		locations.addFirst(location);
	}

	@Override
	public String toString() {

		if (locations.isEmpty()) {
			return "";
		}

		StringBuilder sb = new StringBuilder("#");

		for (String location : locations) {

			sb.append(location).append("#");

		}

		return sb.toString();
	}

	public boolean isTopLevel() {
		return locations.isEmpty();
	}
}
