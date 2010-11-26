package base.hldd.structure.nodes;

import base.SourceLocation;
import base.hldd.structure.variables.AbstractVariable;
import base.psl.structure.Range;
import base.Indices;

/**
 * Node with a notion of <i>temporal window</i>.
 * Occurs in THLDDs generated from PSL assertions.
 * <p/>
 * <b>THLDD</b> is a Temporally extended HLDD.
 *
 * @author Anton Chepurov
 */
public class TemporalNode extends Node {
	private Range window;
	private String[] windowPlaceholders;

	/**
	 * Main and only constructor
	 *
	 * @param builder from which to build the TemporalNode
	 */
	private TemporalNode(Builder builder) {
		super(builder);
		window = builder.window;
		windowPlaceholders = builder.windowPlaceholders;
	}

	protected String depVarName() {
		return window == null ? super.depVarName() : super.depVarName() + window.toString();
	}

	/* Getters */

	public String[] getWindowPlaceholders() {
		return windowPlaceholders;
	}

	public static class Builder extends Node.Builder {
		// Optional parameters -- initialized to default values
		private Range window = null;
		private String[] windowPlaceholders = null;

		public Builder(AbstractVariable dependentVariable) {
			super(dependentVariable);
		}

		public TemporalNode build() {
			return new TemporalNode(this);
		}

		/**
		 * Override superclass method and return this builder (not the one of the superclass)
		 */
		@Override
		public Builder createSuccessors(int conditionValuesCount) {
			super.createSuccessors(conditionValuesCount);
			return this;
		}

		/**
		 * Override superclass method and return this builder (not the one of the superclass)
		 */
		public Builder range(Indices range) {
			super.range(range);
			return this;
		}

		/**
		 * Override superclass method and return this builder (not the one of the superclass)
		 */
		public Builder source(SourceLocation source) {
			super.source(source);
			return this;
		}

		public Builder window(Range window) {
			this.window = window;
			return this;
		}

		public Builder windowPlaceholders(String[] windowPlaceholders) {
			this.windowPlaceholders = windowPlaceholders;
			return this;
		}
	}

}
