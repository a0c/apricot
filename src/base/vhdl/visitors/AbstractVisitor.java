package base.vhdl.visitors;

import base.vhdl.structure.Architecture;
import base.vhdl.structure.Entity;
import base.vhdl.structure.Process;
import base.vhdl.structure.nodes.CaseNode;
import base.vhdl.structure.nodes.IfNode;
import base.vhdl.structure.nodes.TransitionNode;
import base.vhdl.structure.nodes.WhenNode;

/**
 * @author Anton Chepurov
 */
public abstract class AbstractVisitor {

	//todo: make all methods non-abstract, but throw Exceptions from them. This way Impl classes will not be obliged to override methods with empty bodies, when the methods are not actually needed 
	public abstract void visitEntity(Entity entity) throws Exception;

	public abstract void visitArchitecture(Architecture architecture) throws Exception;

	public abstract void visitProcess(Process process) throws Exception;

	public abstract void visitIfNode(IfNode ifNode) throws Exception;

	public abstract void visitTransitionNode(TransitionNode transitionNode) throws Exception;

	public abstract void visitCaseNode(CaseNode caseNode) throws Exception;

	public abstract void visitWhenNode(WhenNode whenNode) throws Exception;

}
