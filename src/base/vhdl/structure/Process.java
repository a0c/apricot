package base.vhdl.structure;

import base.vhdl.structure.nodes.CompositeNode;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

import base.vhdl.visitors.Visitable;
import base.vhdl.visitors.AbstractVisitor;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 06.02.2008
 * <br>Time: 21:31:50
 */
public class Process implements Visitable {

    private String name;
    private final Collection<String> sensitivityList;

    private Set<Variable> variables = new HashSet<Variable>();

    private Set<Constant> constants = new HashSet<Constant>();

    private CompositeNode rootNode;

    public Process(String name, Collection<String> sensitivityList) {
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

    public Variable resolveVariable(String variableName) {
        for (Variable variable : variables) {
            if (variable.getName().equals(variableName)) {
                return variable;
            }
        }
        return null;
    }

    public Constant resolveConstant(String constantName) {
        for (Constant constant : constants) {
            if (constant.getName().equals(constantName)) {
                return constant;
            }
        }
        return null;
    }
}
