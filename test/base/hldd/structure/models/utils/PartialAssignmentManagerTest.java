package base.hldd.structure.models.utils;

import base.Indices;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author Anton Chepurov
 */
public class PartialAssignmentManagerTest {

	@Test
	public void correctUnsetIndicesExtracted() {
		DataHolder[] data = new DataHolder[]{
				new DataHolder(
						new boolean[5],
						new Indices[]{new Indices(4, 0)}),
				new DataHolder(
						new boolean[]{true, false, false, false, false},
						new Indices[]{new Indices(4, 1)}),
				new DataHolder(
						new boolean[]{true, true, true, false, false},
						new Indices[]{new Indices(4, 3)}),
				new DataHolder(
						new boolean[]{true, true, true, true, true},
						new Indices[]{}),
				new DataHolder(
						new boolean[]{true, false, true, false, false},
						new Indices[]{new Indices(1, 1), new Indices(4, 3)}),
				new DataHolder(
						new boolean[]{true, false, true, true, true},
						new Indices[]{new Indices(1, 1)}),
				new DataHolder(
						new boolean[]{true, false, true, false, true},
						new Indices[]{new Indices(1, 1), new Indices(3, 3)}),
				new DataHolder(
						new boolean[]{false, false, true, true, false},
						new Indices[]{new Indices(1, 0), new Indices(4, 4)}),
				new DataHolder(
						new boolean[]{false, false, false, false, true},
						new Indices[]{new Indices(3, 0)}),
		};
		for (DataHolder dataHolder : data) {
			Collection<Indices> actualIndices = PartialAssignmentManager.extractUnsetIndices(dataHolder.setBits);
			assertArrayEquals("The following set bits filled incorrect: " + java.util.Arrays.toString(dataHolder.setBits),
					dataHolder.unsetIndices,
					actualIndices.toArray(new Indices[actualIndices.size()]));
		}
	}

	/* Helper CLASSES */
	private class DataHolder {
		private final boolean[] setBits;
		private final Indices[] unsetIndices;
		public DataHolder(boolean[] setBits, Indices[] unsetIndices) {
			this.setBits = setBits;
			this.unsetIndices = unsetIndices;
		}
	}

}
