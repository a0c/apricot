package parsers.vhdl;

import base.SourceLocation;
import base.vhdl.structure.Process;
import base.vhdl.structure.nodes.*;
import base.vhdl.structure.Transition;
import base.vhdl.structure.Expression;
import base.vhdl.structure.*;
import base.vhdl.structure.Package;
import base.Indices;
import base.Type;
import io.QuietCloser;

import java.io.*;
import java.util.*;
import java.math.BigInteger;
import java.util.regex.Pattern;

/**
 * @author Anton Chepurov
 */
public class StructureBuilder extends AbstractPackageBuilder {

	private Entity entity;

	private Set<Package> packages = new HashSet<Package>();
	/* Context Stack */
	private Stack<Object> contextStack = new Stack<Object>();

	public void buildEntity(String entityName) {
		if (entity == null) {
			entity = new Entity(entityName);
			contextStack.push(entity);
		}
		/* Copy internals of the available packages to the Entity */
		for (Package aPackage : packages) {
			/* Copy constants */
			for (Constant constant : aPackage.getConstants()) {
				buildGeneric(constant.getName(), constant.getType(), constant.getValue());
			}
			/* Copy typeByName */
			for (Map.Entry<String, Type> hsbByTNameEntry : aPackage.getTypeByName().entrySet()) {
				registerType(hsbByTNameEntry.getKey(), hsbByTNameEntry.getValue());
			}
		}
	}

	public void buildComponentDeclaration(String componentName, File sourceFile) throws Exception {
		if (entity != null) {
			File compArchFile = new ArchitectureFileFinder(sourceFile).findArchitectureFileForEntity(componentName);
			ComponentDeclaration comp = new ComponentDeclaration(componentName, compArchFile);
			entity.addComponentDeclaration(comp);
			contextStack.push(comp);
		}
	}

	public void buildGeneric(String genericConstantName, Type type, BigInteger value) {
		if (entity != null) {
			Constant newConstant = new Constant(genericConstantName, type, value);
			registerConstant(newConstant);
			entity.addGenericConstant(newConstant);
		}
	}

	public void buildPort(String portName, boolean isInput, Type type) {
		if (entity != null) {
			Port newPort = new Port(portName, isInput, type);
			Object currentContext = contextStack.peek();
			if (currentContext instanceof Entity) {
				variableNames.add(portName);
				entity.addPort(newPort);
			} else if (currentContext instanceof ComponentDeclaration) {
				((ComponentDeclaration) currentContext).addPort(newPort);
			}
		}
	}

	public void buildComponentInstantiation(String componentName, String componentUnitName, List<Map.Entry<String, String>> portMappingEntries) throws Exception {
		PortMap portMap = new PortMap(portMappingEntries.size());
		for (Map.Entry<String, String> portMappingEntry : portMappingEntries) {
			AbstractOperand formal = expressionBuilder.buildExpression(portMappingEntry.getKey());
			AbstractOperand actual = expressionBuilder.buildExpression(portMappingEntry.getValue());
			if (!(formal instanceof OperandImpl)) {
				throw new Exception("Simple operand expected as a Formal in port map " + portMappingEntry);
			}
			portMap.addMapping((OperandImpl) formal, actual);
		}
		ComponentDeclaration componentDeclaration = entity.resolveComponentDeclaration(componentUnitName);
		ComponentInstantiation compInst = new ComponentInstantiation(componentName, componentDeclaration, portMap);

		Object currentContext = contextStack.peek();
		if (currentContext instanceof Architecture) {
			((Architecture) currentContext).addComponent(compInst);
		}
	}

	public void buildArchitecture(String name, String affiliation) {
		if (entity != null) {
			entity.setArchitecture(new Architecture(name, affiliation));
			contextStack.push(entity.getArchitecture());
		}
	}

	public void buildConstant(String constantName, Type type, BigInteger value) {
		if (entity != null) {
			Constant newConstant = new Constant(constantName, type, value);
			registerConstant(newConstant);
			Object currentContext = contextStack.peek();
			if (currentContext == null) {
				/* Do nothing */
			} else if (currentContext instanceof Entity) {
				((Entity) currentContext).addConstant(newConstant);
			} else if (currentContext instanceof Architecture) {
				((Architecture) currentContext).addConstant(newConstant);
			} else if (currentContext instanceof Process) {
				((Process) currentContext).addConstant(newConstant);
			}
		}
	}

	public void buildCloseDeclaration() {
		/* Remove Current Context */
		Object currentContext = contextStack.pop();
		if (currentContext instanceof IfNode) {
			// do nothing
		} else if (currentContext instanceof WhenNode) {
			/* Remove CaseNode */
			contextStack.pop();
		} else if (currentContext instanceof Process) {
			// do nothing
		} else if (currentContext instanceof Architecture) {
			// do nothing
		} else if (currentContext instanceof Entity) {
			// do nothing
		}
	}

	public void buildProcess(String processName, Collection<String> sensitivityList) {
		if (entity != null) {
			Architecture architecture = entity.getArchitecture();
			if (architecture != null) {
				Process process = new Process(processName, sensitivityList);
				architecture.addProcess(process);
				/* Add process to Context Stack */
				contextStack.push(process);
			}
		}
	}

	public void buildVariable(String variableName, Type type) {
		Object objectUnderConstruction = contextStack.peek();
		if (objectUnderConstruction instanceof Process) {
			Process process = (Process) objectUnderConstruction;
			process.addVariable(new Variable(variableName, type));
			variableNames.add(variableName);
		}
	}

	public void buildSignal(String signalName, Type type, BigInteger defaultValue) {
		Object objectUnderConstruction = contextStack.peek();
		if (objectUnderConstruction instanceof Architecture) {
			Architecture architecture = (Architecture) objectUnderConstruction;
			architecture.addSignal(new Signal(signalName, type, defaultValue));
			variableNames.add(signalName);
		}
	}

	public void buildAlias(String name, Type type, String actual) throws Exception {

		AbstractOperand actualOperand = expressionBuilder.buildExpression(actual);
		if (!(actualOperand instanceof OperandImpl)) {
			throw new UnsupportedConstructException("Simple operand expected as alias' actual, received: "
					+ actual, actual);
		}

		expressionBuilder.addAlias(new Alias(name, type, (OperandImpl) actualOperand));
	}

	/**
	 * @param conditionString a string representation of a condition
	 * @param source		  corresponding lines in source VHDL file
	 * @throws Exception if and error occurs during the creation of a condition
	 */
	public void buildIfStatement(String conditionString, SourceLocation source) throws Exception {
		/* Create IfNode */
		AbstractOperand conditionExpression = expressionBuilder.buildExpression(conditionString);
		if (conditionExpression instanceof OperandImpl) {
			conditionExpression = expressionBuilder.unfoldBoolean((OperandImpl) conditionExpression);
		}
		if (!(conditionExpression instanceof Expression)) {
			throw new Exception("Boolean conditions are not supported as IfNode conditions. Replace with Expression condition.");
		}
		IfNode ifNode = new IfNode((Expression) conditionExpression);
		/* Map created IfNode with VHDL lines being processed currently */
		ifNode.setSource(source);
		/* Add IfNode to Current Context */
		addNodeToCurrentContext(ifNode);
		/* Add IfNode to Context Stack */
		contextStack.push(ifNode);
	}

	private void addNodeToCurrentContext(AbstractNode newNode) { //todo: move setting parent into addNode(), if possible (look where else is setParent() used and if always before addNode() then may be remove setParent() method at all...)
		/* Get Current Context from stack */
		Object currentContext = contextStack.peek();
		if (currentContext instanceof Architecture) {

			CompositeNode transitionsNode = ((Architecture) currentContext).getTransitions();
//            newNode.setParent(transitionsNode); // todo: does nothing, since is overriden in the statement that follows (in transitionsNode.addNode();)
			transitionsNode.addNode(newNode);

		} else if (currentContext instanceof Process) {

			/* Add newNode to the rootNode of the current process */
			CompositeNode rootNode = ((Process) currentContext).getRootNode();
//            newNode.setParent(rootNode); // todo: does nothing, since is overriden in the statement that follows (in rootNode.addNode();)
			rootNode.addNode(newNode);

		} else if (currentContext instanceof IfNode) {

			/* Add newNode to the current IfNode */
			newNode.setParent((IfNode) currentContext);
			((IfNode) currentContext).addTransition(newNode);

		} else if (currentContext instanceof CaseNode) {

			/* Add newNode to the current CaseNode */
			if (newNode instanceof WhenNode) {

				newNode.setParent((AbstractNode) currentContext);
				((CaseNode) currentContext).addCondition((WhenNode) newNode);

			}

		} else if (currentContext instanceof WhenNode) {

			newNode.setParent((WhenNode) currentContext);
			((WhenNode) currentContext).addTransition(newNode);

		}
	}

	/**
	 * @param variableName  left part of the transition
	 * @param variableValue right part of the transition
	 * @param source		corresponding lines in source VHDL file
	 * @throws Exception if and error occurs during the creation of a transition
	 */
	public void buildTransition(String variableName, String variableValue, SourceLocation source) throws Exception {
		/* Create TransitionNode */
		Transition transition;
		if (variableValue.equals("NULL")) {
			/* ##### NULL #####*/
			transition = new Transition();
		} else if (variableValue.startsWith("(") && variableValue.endsWith(")") && variableValue.contains("OTHERS =>")) {
			/* ##### OTHERS #####*/
			variableValue = variableValue.substring(variableValue.indexOf("=>") + 2, variableValue.indexOf(")")).trim();
			/* Check that 0 or 1 is set. Otherwise say that implementation is missing. */
			doCheckOthers(variableValue);
			variableValue = replaceOthersValue(getLengthFor(variableName), variableValue);
			transition = new Transition((OperandImpl) expressionBuilder.buildExpression(variableName), expressionBuilder.buildExpression(variableValue));
		} else {
			/* ##### NORMAL #####*/
			transition = new Transition((OperandImpl) expressionBuilder.buildExpression(variableName), expressionBuilder.buildExpression(variableValue));
		}
		TransitionNode transitionNode = new TransitionNode(transition);
		/* Map created TransitionNode with VHDL lines being processed currently */
		transitionNode.setSource(source);
		/* Add TransitionNode to Current Context */
		addNodeToCurrentContext(transitionNode);
	}

	private String replaceOthersValue(Indices length, String variableValue) {
		/* Cut off Single quotes */
		variableValue = variableValue.substring(1, 2);
		/* Create a new String and fill it with 0 or 1 */
		StringBuilder sb = new StringBuilder(length.length());
		sb.append("\"");
		for (int i = 0; i < length.length(); i++) { /*length + 1*/
			sb.append(variableValue);
		}
		sb.append("\"");
		return sb.toString();
	}

	private Indices getLengthFor(String variableName) {
		//todo: consider implementing resolve(String) method in Entity, Architecture, Process etc...
		/* Search in PORTS */
		for (Port port : entity.getPorts()) {
			if (port.getName().equals(variableName)) {
				return port.getType().getLength();
			}
		}
		/* Search in SIGNALS */
		for (Signal signal : entity.getArchitecture().getSignals()) {
			if (signal.getName().equals(variableName)) {
				return signal.getType().getLength();
			}
		}
		/* Search in VARIABLES */
		for (Process process : entity.getArchitecture().getProcesses()) {
			for (Variable variable : process.getVariables()) {
				if (variable.getName().equals(variableName)) {
					return variable.getType().getLength();
				}
			}
		}
		return null;
	}

	private void doCheckOthers(String valueString) throws Exception {
		if (!(valueString.equals("\'0\'") || valueString.equals("\'1\'")))
			throw new Exception("Unsupported OTHERS construct is met: " + valueString +
					"\nCurrent implementation only supports filling signals with constant values 0 and 1.");
	}

	/**
	 * @param conditionString a string representation of a condition
	 * @param source		  corresponding lines in source VHDL file
	 * @throws Exception if and error occurs during the creation of a condition
	 * 					 or a preceding IF statement was not found for the ELSIF statement
	 */
	public void buildElsifStatement(String conditionString, SourceLocation source) throws Exception {
		/* Mark falsePart for current IfNode on top of Context Stack */
		Object currentContext = contextStack.peek();
		if (currentContext instanceof IfNode) {
			IfNode ifNode = (IfNode) currentContext;
			ifNode.markFalsePart();
			/* 1) Build new IfNode,
			*  2) add it to the falsePart of the Current Context,
			*  3) put newly built IfNode on top of Context Stack */
			buildIfStatement(conditionString, source);
			/* Remove Preceding IF statement from Context Stack */
			contextStack.remove(ifNode); //todo: check to work properly. Use remove(int) otherwise.
		} else
			throw new Exception("Preceding IF statement is not found for the following ELSIF statement: " + conditionString);
	}

	/**
	 * @throws Exception if a preceding IF statement was not found for the ELSE statement
	 */
	public void buildElseStatement() throws Exception {
		/* Mark falsePart for current IfNode on top of Context Stack */
		Object currentContext = contextStack.peek();
		if (currentContext instanceof IfNode) {
			((IfNode) currentContext).markFalsePart();
		} else throw new Exception("Preceding IF statement is not found for ELSE statement");

	}

	public void buildCaseStatement(String variableName, SourceLocation source) throws Exception {
		/* Create CaseNode */
		CaseNode caseNode = new CaseNode(expressionBuilder.buildExpression(variableName));
		/* Map created CaseNode with VHDL lines being processed currently */
		caseNode.setSource(source);
		/* Add CaseNode to Current Context */
		addNodeToCurrentContext(caseNode);
		/* Add CaseNode to Context Stack */
		contextStack.push(caseNode);
	}

	public void buildWhenStatement(String... conditions) throws Exception {
		/* Remove previous WhenNode from Context Stack */
		if (contextStack.peek() instanceof WhenNode) {
			contextStack.pop();
		}
		WhenNode whenNode = new WhenNode(conditions);
		/* Add CompositeWhenNode to Current Context */
		addNodeToCurrentContext(whenNode);
		/* Add CompositeWhenNode to Context Stack */
		contextStack.push(whenNode);
	}

	public Entity getVHDLStructure() {
		return entity;
	}

	public void addPackage(Package aPackage) {
		packages.add(aPackage);
	}

	static class ArchitectureFileFinder {
		private final File sourceFile;

		public ArchitectureFileFinder(File sourceFile) {
			this.sourceFile = sourceFile;
		}

		public File findArchitectureFileForEntity(String entityName) {

			List<File> files = getListOfFiles();

			for (File file : files) {

				FileInputStream fis = null;
				try {

					fis = new FileInputStream(file);

					if (new Detector(fis, entityName).isDetected()) {
						return file;
					}

				} catch (FileNotFoundException e) {
					throw new RuntimeException(e); // should never happen
				} finally {
					QuietCloser.closeQuietly(fis);
				}
			}
			return null;
		}

		List<File> getListOfFiles() {
			return Arrays.asList(sourceFile.getParentFile().listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					if (pathname.equals(sourceFile)) {
						return false;
					}
					String fileName = pathname.getName().toLowerCase();
					return fileName.endsWith(".vhd") || fileName.endsWith(".vhdl");
				}
			}));
		}

		public File getSourceFile() {
			return sourceFile;
		}

		public static class Detector {
			// basic_identifier ::= letter { [ underline ] letter_or_digit }
			private static final Pattern IDENTIFIER = Pattern.compile("([a-z]\\w*)", Pattern.CASE_INSENSITIVE);
			private final Scanner scanner;
			private final String entityName;

			public Detector(InputStream inputStream, String entityName) {
				this.entityName = entityName;
				scanner = new Scanner(new BufferedInputStream(inputStream));
			}

			public boolean isDetected() {
				String token;
				while ((token = nextSignificant()) != null) {
					if (token.equalsIgnoreCase("architecture")
							&& IDENTIFIER.matcher(nextSignificant()).matches()
							&& "of".equalsIgnoreCase(nextSignificant())
							&& entityName.equalsIgnoreCase(nextSignificant())
							&& "is".equalsIgnoreCase(nextSignificant())) {
						return true;
					}
				}
				return false;
			}

			private String nextSignificant() {
				while (scanner.hasNext()) {
					String token = scanner.next();
					if (token.startsWith("--")) {
						/* Skip commented line */
						if (scanner.hasNextLine()) {
							scanner.nextLine();
							continue;
						} else {
							return null;
						}
					}
					return token;
				}
				return null;
			}
		}
	}
}
