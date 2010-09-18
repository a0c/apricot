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

import java.util.*;
import java.math.BigInteger;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 06.02.2008
 * <br>Time: 21:13:25
 */
public class VHDLStructureBuilder extends AbstractPackageBuilder {

    private Entity entity;
    private Set<Package> packages = new HashSet<Package>();
    /* Context Stack */
    private Stack<Object> contextStack = new Stack<Object>();

//    private OperandValueCalculator valueCalculator = new OperandValueCalculator();
//
//    private Collection<String> variableNames = new HashSet<String>();
//
//    private ExpressionBuilder expressionBuilder = new ExpressionBuilder(valueCalculator, variableNames);

	public void buildEntity(String entityName) {
        if (entity == null) {
            entity = new Entity(entityName);
            contextStack.push(entity);
        }
        /* Copy internals of the available packages to the Entity */
        for (Package aPackage : packages) {
            /* Copy constants */
            for (Constant constant : aPackage.getConstants()) {
                buildGeneric(constant.getName(), constant.getValue());
            }
            /* Copy typeByName */
            for (Map.Entry<String, Type> hsbByTNameEntry : aPackage.getTypeByName().entrySet()) {
                registerType(hsbByTNameEntry.getKey(), hsbByTNameEntry.getValue());
            }
        }
    }

    public void buildGeneric(String genericConstantName, BigInteger value) {
        if (entity != null) {
            Constant newConstant = new Constant(genericConstantName, value);
            registerConstant(newConstant);
            entity.addGenericConstant(newConstant);
        }
    }

    public void buildPort(String portName, boolean isInput, Type type) {
        if (entity != null) {
            Port newPort = new Port(portName, isInput, type);
            variableNames.add(portName);
            entity.addPort(newPort);
        }
    }

    public void buildArchitecture(String name, String affiliation) {
        if (entity != null) {
            entity.setArchitecture(new Architecture(name, affiliation));
            contextStack.push(entity.getArchitecture());
        }
    }

    public void buildConstant(String constantName, BigInteger value) {
        if (entity != null) {
            Constant newConstant = new Constant(constantName, value);
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
            //todo: EXPERIMENTAL WHEN
        } else if (currentContext instanceof WhenNode/*isCompositeWhenNode(currentContext)*/) {
            /* Remove CaseNode */
            contextStack.pop();
        } else if (currentContext instanceof Process) {
            //todo?
        } else if (currentContext instanceof Architecture) {
            // do nothing
        } else if (currentContext instanceof Entity) {
            //todo?
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
        Object objectUnderConstr = contextStack.peek();
        if (objectUnderConstr instanceof Process) {
            Process process = (Process) objectUnderConstr;
            process.addVariable(new Variable(variableName, type));
            variableNames.add(variableName);
        }
    }

    public void buildSignal(String signalName, Type type) {
        Object objectUnderConstr = contextStack.peek();
        if (objectUnderConstr instanceof Architecture) {
            Architecture architecture = (Architecture) objectUnderConstr;
            architecture.addSignal(new Signal(signalName, type));
            variableNames.add(signalName);
        }
    }


    /**
     *
     * @param conditionString a string representation of a condition
     * @param source corresponding lines in source VHDL file
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

            //todo: EXPERIMENTAL WHEN
            /* Add newNode to the current CaseNode
             * only if newNode is a Composite WhenNode */
            if (newNode instanceof WhenNode/*isCompositeWhenNode(newNode)*/) {

                newNode.setParent((AbstractNode) currentContext);
                ((CaseNode) currentContext).addCondition((WhenNode) newNode);

                //todo: EXPERIMENTAL WHEN
//                /* Flatten composite structure here.
//                *  Composite structure will still be preserved in contextStack. */
//                List<AbstractNode> whenNodes = ((CompositeNode) newNode).getChildren();
//                for (AbstractNode whenNode : whenNodes) {
//                    whenNode.setParent((AbstractNode) currentContext);
//                    ((CaseNode) currentContext).addCondition((WhenNode) whenNode);
//                }
            }

            //todo: EXPERIMENTAL WHEN
        } else if (currentContext instanceof WhenNode/*isCompositeWhenNode(currentContext)*/) {

            newNode.setParent(((WhenNode) currentContext));
            ((WhenNode) currentContext).addTransition(newNode);

            //todo: EXPERIMENTAL WHEN
//            /* Add newNode to the current WhenNode-s */
//            for (AbstractNode whenNode : ((CompositeNode) currentContext).getChildren()) {
//                newNode.setParent(whenNode);
//                ((WhenNode) whenNode).addTransition(newNode);
//            }
            
        }
    }

    /**
     * Composite WhenNode is a {@link CompositeNode} with all its children being  {@link WhenNode}-s.
     * @param newNode node to check
     * @return if the specified node is Composite WhenNode
     */
    private boolean isCompositeWhenNode(Object newNode) {
        if (newNode instanceof CompositeNode) {
            CompositeNode compositeNode = (CompositeNode) newNode;
            for (AbstractNode child : compositeNode.getChildren()) {
                if (!(child instanceof WhenNode)) return false;
            }
            return true;
        } else return false;
    }

    /**
     *
     * @param variableName left part of the transition
     * @param variableValue right part of the transition
     * @param source corresponding lines in source VHDL file
	 * @throws Exception if and error occurs duting the creation of a transition
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
        } else  {
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
     *
     * @param conditionString a string representation of a condition
     * @param source corresponding lines in source VHDL file
	 * @throws  Exception if and error occurs during the creation of a condition
     *          or a preceding IF statement was not found for the ELSIF statement
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
        } else throw new Exception("Preceding IF statement is not found for the following ELSIF statement: " + conditionString);
    }

    /**
     *
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
        //todo: EXPERIMENTAL WHEN
//        if (isCompositeWhenNode(contextStack.peek())) {
//            contextStack.pop();
//        }
        if (contextStack.peek() instanceof WhenNode) {
            contextStack.pop();
        }
        //todo: EXPERIMENTAL WHEN
//        CompositeNode compositeWhenNode = new CompositeNode();
//        for (String condition : conditions) {
//            /* Create WhenNode */
//            WhenNode whenNode = new WhenNode(condition);
//            /* Add it to composite node */
//            compositeWhenNode.addNode(whenNode);
//        }
        WhenNode whenNode = new WhenNode(conditions);
        /* Add CompositeWhenNode to Current Context */
        addNodeToCurrentContext(whenNode/*compositeWhenNode*/);
        /* Add CompositeWhenNode to Context Stack
        * (everywhere in contextStack are now composite nodes, instead of WhenNodes) */
        //todo: EXPERIMENTAL WHEN
        contextStack.push(whenNode/*compositeWhenNode*/);
    }

    public Entity getVHDLStructure() {
        return entity;
    }

    public void addPackage(Package aPackage) {
        packages.add(aPackage);
    }

}
