package base.vhdl.structure;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 13.09.2010
 * <br>Time: 18:14:50
 */
public class ComponentDeclaration {
	
	private final String name;

	private final File sourceFile;

	private final List<Port> ports;

	public ComponentDeclaration(String name, File sourceFile) {
		this.name = name;
		this.sourceFile = sourceFile;
		ports = new LinkedList<Port>();
	}

	public void addPort(Port port) {
		ports.add(port);
	}

	public String getName() {
		return name;
	}

	public File getSourceFile() {
		return sourceFile;
	}

	public List<Port> getOutputPorts() {

		List<Port> outputPorts = new LinkedList<Port>();

		for (Port port : ports) {

			if (port.isOutput()) {

				outputPorts.add(port);
			}
		}

		return outputPorts;
	}
}
