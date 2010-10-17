package base.vhdl.structure;

import base.vhdl.visitors.Visitable;
import base.vhdl.visitors.AbstractVisitor;
import base.vhdl.structure.nodes.CompositeNode;

import java.util.*;

/**
 * @author Anton Chepurov
 */
public class Architecture implements Visitable {
	private final String name;
	private final String affiliation;

	private Set<Constant> constants = new HashSet<Constant>();

	private Set<Signal> signals = new HashSet<Signal>();

	private Set<Process> processes = new LinkedHashSet<Process>();

	private CompositeNode transitions;

	private Set<ComponentInstantiation> components = new HashSet<ComponentInstantiation>();

	public Architecture(String name, String affiliation) {
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

	public Signal resolveSignal(String signalName) {
		for (Signal signal : signals) {
			if (signal.getName().equals(signalName)) {
				return signal;
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

	public void addComponent(ComponentInstantiation componentInst) {
		components.add(componentInst);
	}
}
