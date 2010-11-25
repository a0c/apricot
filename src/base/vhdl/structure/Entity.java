package base.vhdl.structure;

import base.vhdl.visitors.AbstractVisitor;
import base.vhdl.visitors.Visitable;
import io.scan.VHDLScanner;
import parsers.vhdl.StructureBuilder;
import parsers.vhdl.StructureParser;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Anton Chepurov
 */
public class Entity extends ASTObject implements Visitable {

	private String name;

	private Set<Constant> genericConstants;

	private Set<Constant> constants;

	private Set<Port> ports;

	private Map<String, ComponentDeclaration> componentDeclarations;

	private Architecture architecture;


	public static Entity parseVhdlStructure(File vhdlFile) throws Exception {
		/* Parse VHDL structure */
		VHDLScanner vhdlScanner = new VHDLScanner(vhdlFile);
		StructureBuilder structureBuilder = new StructureBuilder();
		StructureParser structureParser = new StructureParser(vhdlScanner, structureBuilder);
		structureParser.parse();
		return structureBuilder.getVHDLStructure();
	}

	public Entity(String name) {
		super(null);
		this.name = name;
		ports = new HashSet<Port>();
		genericConstants = new HashSet<Constant>();
		constants = new HashSet<Constant>();
		componentDeclarations = new HashMap<String, ComponentDeclaration>();
	}

	public void addPort(Port newPort) {
		ports.add(newPort);
	}

	public void addComponentDeclaration(ComponentDeclaration componentDeclaration) {
		componentDeclarations.put(componentDeclaration.getName(), componentDeclaration);
	}

	public void addGenericConstant(Constant newGenericConstant) {
		genericConstants.add(newGenericConstant);
	}

	public void addConstant(Constant newConstant) {
		constants.add(newConstant);
	}

	/* GETTERS and SETTERS */

	public Architecture getArchitecture() {
		return architecture;
	}

	public Set<Constant> getGenericConstants() {
		return genericConstants;
	}

	public Set<Constant> getConstants() {
		return constants;
	}

	public Set<Port> getPorts() {
		return ports;
	}

	public void setArchitecture(Architecture architecture) {
		this.architecture = architecture;
	}

	public void traverse(AbstractVisitor visitor) throws Exception {
		visitor.visitEntity(this);

		architecture.traverse(visitor);
	}

	private Port resolvePort(String name) {
		//todo... change ports type to Map
		for (Port port : ports) {
			if (port.getName().equals(name)) {
				return port;
			}
		}
		return null;
	}

	private Constant resolveConstantInternal(String name) {
		//todo... change constants type to Map
		for (Constant constant : constants) {
			if (constant.getName().equals(name)) {
				return constant;
			}
		}
		//todo... change generic constants type to Map
		for (Constant constant : genericConstants) {
			if (constant.getName().equals(name)) {
				return constant;
			}
		}
		return null;
	}

	public ComponentDeclaration resolveComponentDeclaration(String compDeclName) {
		return componentDeclarations.get(compDeclName);
	}

	@Override
	public ASTObject doResolve(String name) {
		Port port = resolvePort(name);
		if (port != null) {
			return port;
		}
		Constant constant = resolveConstantInternal(name);
		if (constant != null) {
			return constant;
		}
		return resolveComponentDeclaration(name);
	}
}
