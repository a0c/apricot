package base.vhdl.structure.utils;

import base.Range;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author Anton Chepurov
 */
public class SplittableOperandStorageTest {

	@Test
	public void correctUnsetRangesExtracted() {
		DataHolder[] data = new DataHolder[]{
				new DataHolder(
						new boolean[5],
						new Range[]{new Range(4, 0)}),
				new DataHolder(
						new boolean[]{true, false, false, false, false},
						new Range[]{new Range(4, 1)}),
				new DataHolder(
						new boolean[]{true, true, true, false, false},
						new Range[]{new Range(4, 3)}),
				new DataHolder(
						new boolean[]{true, true, true, true, true},
						new Range[]{}),
				new DataHolder(
						new boolean[]{true, false, true, false, false},
						new Range[]{new Range(1, 1), new Range(4, 3)}),
				new DataHolder(
						new boolean[]{true, false, true, true, true},
						new Range[]{new Range(1, 1)}),
				new DataHolder(
						new boolean[]{true, false, true, false, true},
						new Range[]{new Range(1, 1), new Range(3, 3)}),
				new DataHolder(
						new boolean[]{false, false, true, true, false},
						new Range[]{new Range(1, 0), new Range(4, 4)}),
				new DataHolder(
						new boolean[]{false, false, false, false, true},
						new Range[]{new Range(3, 0)}),
		};
		for (DataHolder dataHolder : data) {
			Collection<Range> actualRanges = SplittableOperandStorage.extractUnsetRanges(dataHolder.setBits);
			assertArrayEquals("The following set bits filled incorrect: " + java.util.Arrays.toString(dataHolder.setBits),
					dataHolder.unsetRanges,
					actualRanges.toArray(new Range[actualRanges.size()]));
		}
	}

	/* Helper CLASSES */

	private class DataHolder {
		private final boolean[] setBits;
		private final Range[] unsetRanges;

		public DataHolder(boolean[] setBits, Range[] unsetRanges) {
			this.setBits = setBits;
			this.unsetRanges = unsetRanges;
		}
	}

}
