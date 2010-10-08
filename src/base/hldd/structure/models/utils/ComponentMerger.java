package base.hldd.structure.models.utils;

import base.hldd.structure.models.BehModel;
import base.hldd.structure.variables.AbstractVariable;
import base.vhdl.structure.AbstractOperand;
import base.vhdl.structure.ComponentInstantiation;
import ui.ExtendedException;

import java.util.Collection;


/**
 * @author Anton Chepurov
 */
public class ComponentMerger {

	private final ComponentInstantiation component;

	private final BehModel componentModel;

	public ComponentMerger(ComponentInstantiation component, BehModel componentModel) {
		this.component = component;
		this.componentModel = componentModel;
	}

	public void mergeTo(ModelManager modelCollector) throws ExtendedException {
		/* merge to modelCollector */
		merge(modelCollector);
		/* link ports */
		linkPorts(modelCollector);
	}

	private void merge(ModelManager modelCollector) {

		for (AbstractVariable absVar : componentModel.getVariables()) {

			if (!absVar.isInput()) {

				modelCollector.addVariable(absVar);
			}

		}
	}

	private void linkPorts(ModelManager modelCollector) throws ExtendedException {

		/* inputs */
		link(componentModel.getInputPorts(), modelCollector);

		/* outputs */
		link(componentModel.getOutputPorts(), modelCollector);
	}

	private void link(Collection<? extends AbstractVariable> ports, ModelManager modelCollector) throws ExtendedException {

		for (AbstractVariable port : ports) {

			PartedVariableHolder outerSignalHolder = resolveMappingFor(port, modelCollector);

			if (port.isOutput()) {

				modelCollector.rebase(port, outerSignalHolder.getVariable());

			} else {

				modelCollector.replaceWithIndices(port, outerSignalHolder);

			}
		}
	}

	private PartedVariableHolder resolveMappingFor(AbstractVariable port, ModelManager modelCollector) throws ExtendedException {

		String portName = port.getName();

		AbstractOperand outerOperand = component.findActualMappingFor(portName);

		if (outerOperand == null) {
			throw new ExtendedException("ComponentMerger: failed to link port " + portName +
					"\nReason: port does not seem to be mapped",
					ExtendedException.ERROR_TEXT);
		}

		AbstractVariable outerSignal;

		if (port.isOutput()) {

			outerSignal = modelCollector.getVariable(outerOperand.toString());

		} else {

			try {

				outerSignal = modelCollector.convertOperandToVariable(outerOperand, null, true);

			} catch (Exception e) {
				throw new ExtendedException("ComponentMerger: failed to link INPUT port " + portName +
						"\nReason: failed to resolve outer mapping signal " + outerOperand +
						"\nERROR: " + e.getMessage(),
						ExtendedException.ERROR_TEXT);
			}
		}

		if (outerSignal == null) {
			throw new ExtendedException("ComponentMerger: failed to link port " + portName +
					"\nReason: outer mapping signal " + outerOperand + " could not be resolved",
					ExtendedException.ERROR_TEXT);
		}

		return new PartedVariableHolder(outerSignal, outerOperand.getPartedIndices());
	}

}
