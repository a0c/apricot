package base.hldd.structure.models;

import base.hldd.structure.variables.AbstractVariable;

import java.util.Collection;

/**
 * Class representing AGM RTL DD.
 *
 * @author Anton Chepurov
 */
public class Model extends BehModel {

	private int coutCount;

	public Model(Collection<AbstractVariable> variables) {
		super(variables);
	}

	protected String composeFileString(String comment) {

		StringBuilder sb = new StringBuilder();

		// add COMMENT if exists
		if (comment != null && !comment.equals("")) {
			String[] lines = comment.split("\n");
			for (String line : lines) {
				sb.append(";").append(line).append("\n");
			}
		}

		sb.append("\nSTAT#\t").append(nodeCount).append(" Nods,  ");
		sb.append(varCount).append(" Vars,  ");
		sb.append(graphCount).append(" Grps,  ");
		sb.append(inpCount).append(" Inps,  ");
		sb.append(outpCount).append(" Outs,  ");
		sb.append(constCount).append(" Cons,  ");
		sb.append(funcCount).append(" Funs,  ");
		sb.append(coutCount).append(" C_outs\n");

		sb.append("COUT#\t");

		for (int i = 0; i < coutCount; i++) {
			sb.append(i + inpCount + constCount + funcCount).append(", ");
		}
		sb.delete(sb.length() - 2, sb.length());

		sb.append("\n\nMODE#\tRTL\n\n");

		for (int i = 0; i < varCount; i++) {

			if (i == 0)
				sb.append(";inputs\n");
			if (i == inpCount)
				sb.append("\n\n;constants\n");
			if (i == inpCount + constCount)
				sb.append("\n\n;functions\n");
			if (i == inpCount + constCount + funcCount)
				sb.append("\n\n;state variable\n");
			if (i == inpCount + constCount + funcCount + 1)
				sb.append("\n\n;control part outputs\n");
			if (i == inpCount + constCount + funcCount + coutCount)
				sb.append("\n\n;control graph\n");
			if (i == inpCount + constCount + funcCount + coutCount + 1)
				sb.append("\n\n;datapath graphs\n");

			sb.append(getVariableByIndex(i)).append("\n");

		}

		return sb.toString();
	}

	protected void addStat(AbstractVariable variable) {
		super.addStat(variable);
		if (variable.isCout() || variable.isState()) coutCount++;
	}

	public int coutOffset() {
		return funcOffset() + funcCount;
	}

	public int graphOffset() {
		return coutOffset() + coutCount;
	}

	/* Getters START */

	public int getCoutCount() {
		return coutCount;
	}

	/* Getters END */

	/* Setters START*/

	/* Setters END*/

}
