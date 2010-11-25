package base.vhdl.structure;

import base.vhdl.structure.nodes.CompositeNode;
import base.vhdl.visitors.AbstractVisitor;
import base.vhdl.visitors.Visitable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Anton Chepurov
 */
public class Process extends ASTObject implements Visitable {

	private final String name;

	private final Collection<String> sensitivityList;

	private Set<Variable> variables = new HashSet<Variable>();

	private Set<Constant> constants = new HashSet<Constant>();

	private CompositeNode rootNode;

	public Process(String name, Collection<String> sensitivityList, Architecture parent) {
		super(parent);
		this.name = name;
		this.sensitivityList = sensitivityList;
	}

	public void addVariable(Variable newVariable) {
		variables.add(newVariable);
	}

	public void addConstant(Constant newConstant) {
		constants.add(newConstant);
	}

	/* GETTERS and SETTERS */

	public CompositeNode getRootNode() {
		if (rootNode == null) {
			rootNode = new CompositeNode();
		}
		return rootNode;
	}

	public String getName() {
		return name;
	}

	public Collection<String> getSensitivityList() {
		return sensitivityList;
	}

	public Set<Variable> getVariables() {
		return variables;
	}

	public Set<Constant> getConstants() {
		return constants;
	}

	public String toString() {

		StringBuilder builder = new StringBuilder();
		if (name != null) {
			builder.append(name).append(": ");
		}
		builder.append("PROCESS");
		if (!sensitivityList.isEmpty()) {
			builder.append(" (");
			for (String sensitiveSignal : sensitivityList) {
				builder.append(sensitiveSignal).append(", ");
			}
			builder.delete(builder.length() - 2, builder.length());
			builder.append(")");
		}

		return builder.toString().trim();
	}

	public void traverse(AbstractVisitor visitor) throws Exception {
		visitor.visitProcess(this);
	}

	private Variable resolveVariable(String name) {
		//todo: for storing Variables, use Map instead of set
		for (Variable variable : variables) {
			if (variable.getName().equals(name)) {
				return variable;
			}
		}
		return null;
	}

	private Constant resolveConstantInternal(String name) {
		//todo: for storing Constants, use Map instead of set
		for (Constant constant : constants) {
			if (constant.getName().equals(name)) {
				return constant;
			}
		}
		return null;
	}

	@Override
	public ASTObject doResolve(String name) {
		Variable variable = resolveVariable(name);
		if (variable != null) {
			return variable;
		}
		return resolveConstantInternal(name);
	}
}
