package base.vhdl.structure;

import base.vhdl.structure.nodes.CompositeNode;
import base.vhdl.visitors.AbstractVisitor;
import base.vhdl.visitors.Visitable;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Anton Chepurov
 */
public class Architecture extends ASTObject implements Visitable {
	private final String name;
	private final String affiliation;

	private Set<Constant> constants = new HashSet<Constant>();

	private Set<Signal> signals = new HashSet<Signal>();

	private Set<Process> processes = new LinkedHashSet<Process>();

	private CompositeNode transitions;

	private Set<ComponentInstantiation> components = new HashSet<ComponentInstantiation>();

	public Architecture(String name, String affiliation, Entity parent) {
		super(parent);
		this.name = name;
		this.affiliation = affiliation;
	}

	public void addConstant(Constant newConstant) {
		if (!constants.contains(newConstant)) {
			constants.add(newConstant);
		}
	}

	public void addSignal(Signal newSignal) {
		if (!signals.contains(newSignal)) {
			signals.add(newSignal);
		}
	}

	public void addProcess(Process newProcess) {
		if (!processes.contains(newProcess)) {
			processes.add(newProcess);
		}
	}

	/* SETTERS and GETTERS */

	public Set<Constant> getConstants() {
		return constants;
	}

	public Set<Signal> getSignals() {
		return signals;
	}

	public Set<Process> getProcesses() {
		return processes;
	}

	public Set<ComponentInstantiation> getComponents() {
		return components;
	}

	/**
	 * NB! Transitions are added using this method.
	 *
	 * @return process-external transitions of the given {@link base.vhdl.structure.Architecture}
	 */
	public CompositeNode getTransitions() {
		if (transitions == null) {
			transitions = new CompositeNode();
		}
		return transitions;
	}

	public String toString() {
		return "Architecture " + name + " (" + affiliation + ")";
	}

	public void traverse(AbstractVisitor visitor) throws Exception {
		visitor.visitArchitecture(this);

		for (Process process : processes) {
			process.traverse(visitor);
		}
	}

	private Signal resolveSignal(String name) {
		for (Signal signal : signals) {
			if (signal.getName().equals(name)) {
				return signal;
			}
		}
		return null;
	}

	private Constant resolveConstantInternal(String name) {
		for (Constant constant : constants) {
			if (constant.getName().equals(name)) {
				return constant;
			}
		}
		return null;
	}

	public void addComponent(ComponentInstantiation componentInst) {
		components.add(componentInst);
	}

	public ComponentInstantiation resolveComponentInstantiation(String compInstName) {
		for (ComponentInstantiation component : components) {
			if (component.getName().equals(compInstName)) {
				return component;
			}
		}
		return null;
	}

	@Override
	public ASTObject doResolve(String name) {
		Signal signal = resolveSignal(name);
		if (signal != null) {
			return signal;
		}
		Constant constant = resolveConstantInternal(name);
		if (constant != null) {
			return constant;
		}
		return resolveComponentInstantiation(name);
	}
}
