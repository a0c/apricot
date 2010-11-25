package base.vhdl.structure;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Anton Chepurov
 */
public class ComponentDeclaration extends ASTObject {

	private final String name;

	private final File sourceFile;

	private final List<Port> ports;

	private final Map<String, String> genericTypesByName;

	public ComponentDeclaration(String name, File sourceFile) {
		super(null);
		this.name = name;
		this.sourceFile = sourceFile;
		ports = new LinkedList<Port>();
		genericTypesByName = new HashMap<String, String>();
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

	public void addGeneric(Constant genericConstant, String typeAsString) {
		genericTypesByName.put(genericConstant.getName(), typeAsString);
	}

	public String getGenericTypeAsString(String genericName) {
		return genericTypesByName.get(genericName);
	}

	public void resolvePositionalMap(PortMap portMap) {
		portMap.resolvePositionalMap(ports);
	}
}
