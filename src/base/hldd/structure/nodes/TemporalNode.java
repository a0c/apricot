package base.hldd.structure.nodes;

import base.hldd.structure.variables.AbstractVariable;
import base.psl.structure.Range;
import base.Indices;

import java.util.Set;

/**
 * Node with a notion of <i>temporal window</i>.
 * Occurs in THLDDs generated from PSL assertions.
 * <p>
 * <b>THLDD</b> is a Temporally extended HLDD.
 *
 * <br><br>User: Anton Chepurov
 * <br>Date: 11.12.2007
 * <br>Time: 21:20:28
 */
public class TemporalNode extends Node {
    private Range window;
    private String[] windowPlaceholders;

    /**
     * Main and only constructor
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
        public Builder partedIndices(Indices partedIndices) {
            super.partedIndices(partedIndices);
            return this;
        }
        /**
         * Override superclass method and return this builder (not the one of the superclass)
         */
        public Builder vhdlLines(Set<Integer> vhdlLines) {
            super.vhdlLines(vhdlLines);
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
