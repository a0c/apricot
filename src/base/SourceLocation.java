package base;

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 17.09.2010
 * <br>Time: 15:23:49
 */
public class SourceLocation {

	private final TreeSet<Integer> vhdlLines;

	public SourceLocation(Collection<Integer> sourceVhdlLines) {
		if (sourceVhdlLines == null) {
			throw new IllegalArgumentException("SourceLocation: NULL parameter passed to constructor");
		}
		if (sourceVhdlLines.isEmpty()) {
			throw new IllegalArgumentException("SourceLocation: EMPTY COLLECTION parameter passed to constructor");
		}

		vhdlLines = new TreeSet<Integer>(sourceVhdlLines);

		if (vhdlLines.first() < 0) {
			throw new IllegalArgumentException("SourceLocation: NEGATIVE SOURCE VHDL LINE passed to constructor");			
		}
	}

	public SourceLocation addSource(SourceLocation source) {
		if (source == null) {
			return this;
		}
		return createFrom(Arrays.asList(this, source));
	}

	public Integer getFirstLine() {
		return vhdlLines.first();
	}

	@Override
	public String toString() {

		StringBuilder localCollector = new StringBuilder();

		for (int vhdlLine : vhdlLines) localCollector.append(vhdlLine).append(", ");

		return localCollector.substring(0, localCollector.length() - 2);
	}

	public static SourceLocation createFrom(Collection<SourceLocation> sources) {

		TreeSet<Integer> totalVhdlLines = new TreeSet<Integer>();

		for (SourceLocation source : sources) {

			if (source != null) {

				totalVhdlLines.addAll(source.vhdlLines);

			}
		}

		return new SourceLocation(totalVhdlLines);
	}
}
