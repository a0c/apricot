package base.vhdl.structure;

import base.vhdl.visitors.Visitable;
import base.vhdl.visitors.AbstractVisitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.io.File;

import io.scan.VHDLScanner;
import parsers.vhdl.StructureBuilder;
import parsers.vhdl.StructureParser;

/**
 * @author Anton Chepurov
 */
public class Entity implements Visitable {

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

	public Port resolvePort(String portName) {
		//todo... change ports type to Map
		for (Port port : ports) {
			if (port.getName().equals(portName)) {
				return port;
			}
		}
		return null;
	}

	public Constant resolveConstant(String constantName) {
		//todo... change constants type to Map
		for (Constant constant : constants) {
			if (constant.getName().equals(constantName)) {
				return constant;
			}
		}
		//todo... change generic constants type to Map
		for (Constant constant : genericConstants) {
			if (constant.getName().equals(constantName)) {
				return constant;
			}
		}
		return null;
	}

	public ComponentDeclaration resolveComponentDeclaration(String compDeclName) {
		return componentDeclarations.get(compDeclName);
	}
}
