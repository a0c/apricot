package base.vhdl.structure;

import java.util.List;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 14.09.2010
 * <br>Time: 21:42:26
 */
public class ComponentInstantiation {
	private final String name;
	private final ComponentDeclaration declaration;
	private final PortMap portMap;

	public ComponentInstantiation(String name, ComponentDeclaration declaration, PortMap portMap) {
		this.name = name;
		this.declaration = declaration;
		this.portMap = portMap;
	}

	public String getName() {
		return name;
	}

	public ComponentDeclaration getDeclaration() {
		return declaration;
	}

	public AbstractOperand findActualMappingFor(String formalName) {

		return portMap.findActualFor(formalName);
	}

	public void renameFormalMapping(String oldFormal, String newFormal) {

		portMap.renameFormal(oldFormal, newFormal);		
	}

	public List<OperandImpl> findPartedOutputActuals() {

		return portMap.filterPartedActualsFrom(declaration.getOutputPorts());
	}

}
