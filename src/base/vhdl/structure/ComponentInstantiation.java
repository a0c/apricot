package base.vhdl.structure;

import java.util.Collection;
import java.util.List;

/**
 * @author Anton Chepurov
 */
public class ComponentInstantiation extends ASTObject {
	private final String name;
	private final ComponentDeclaration declaration;
	private final PortMap portMap;
	private final Collection<Constant> generics;

	public ComponentInstantiation(String name, ComponentDeclaration declaration, Collection<Constant> generics, PortMap portMap) {
		super(null);
		this.name = name;
		this.declaration = declaration;
		this.generics = generics;
		this.portMap = portMap;
	}

	public String getName() {
		return name;
	}

	public ComponentDeclaration getDeclaration() {
		return declaration;
	}

	public Collection<Constant> getGenerics() {
		return generics;
	}

	public AbstractOperand findActualMappingFor(String formalName) {

		return portMap.findActualFor(formalName);
	}

	public void renameFormalMapping(String oldFormal, String newFormal) {

		portMap.renameFormal(oldFormal, newFormal);
	}

	public void renameGeneric(String oldName, String newName) {

		for (Constant generic : generics) {

			if (generic.getName().equalsIgnoreCase(oldName)) {

				generics.remove(generic);

				generics.add(new Constant(newName, generic.getType(), generic.getValue()));

				break;
			}
		}
	}

	public List<OperandImpl> findPartedOutputActuals() {

		return portMap.filterPartedActualsFrom(declaration.getOutputPorts());
	}

}
