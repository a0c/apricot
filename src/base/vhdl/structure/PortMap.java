package base.vhdl.structure;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Anton Chepurov
 */
public class PortMap {

	private final List<PortMapItem> portMapItems;

	public PortMap(int size) {
		portMapItems = new ArrayList<PortMapItem>(size);
	}

	public void addMapping(OperandImpl formal, AbstractOperand actual) {
		portMapItems.add(new PortMapItem(formal, actual));
	}

	public AbstractOperand findActualFor(String formalName) {

		for (PortMapItem mapItem : portMapItems) {

			if (mapItem.getFormalName().equals(formalName)) {

				return mapItem.actual;
			}
		}
		return null;
	}

	public void renameFormal(String oldFormal, String newFormal) {

		for (int i = 0, portMappingsSize = portMapItems.size(); i < portMappingsSize; i++) {

			PortMapItem mapping = portMapItems.get(i);

			if (mapping.getFormalName().equals(oldFormal)) {

				portMapItems.set(i, new PortMapItem(new OperandImpl(newFormal), mapping.actual));

				break;
			}
		}
	}

	public List<OperandImpl> filterPartedActualsFrom(List<Port> ports) {

		LinkedList<OperandImpl> partedActuals = new LinkedList<OperandImpl>();

		for (Port port : ports) {

			AbstractOperand actual = findActualFor(port.getName());

			if (actual == null) {
				continue; /* no signal is connected to this port of the component */
			}
			if (actual.isParted() && actual instanceof OperandImpl) {

				partedActuals.add(((OperandImpl) actual));
			}

		}

		return partedActuals;

	}

	void resolvePositionalMap(List<Port> ports) {

		for (int i = 0; i < portMapItems.size(); i++) {
			PortMapItem oldPortMapItem = portMapItems.get(i);
			if (oldPortMapItem.formal == null) {
				OperandImpl resolvedFormal = new OperandImpl(ports.get(i).getName());
				portMapItems.set(i, new PortMapItem(resolvedFormal, oldPortMapItem.actual));
			}
		}
	}


	private class PortMapItem {
		private final OperandImpl formal;
		private final AbstractOperand actual;

		private PortMapItem(OperandImpl formal, AbstractOperand actual) {
			this.formal = formal;
			this.actual = actual;
		}

		private String getFormalName() {
			return formal.getName();
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append("PortMapItem");
			sb.append("{formal='").append(formal).append('\'');
			sb.append(", actual='").append(actual).append('\'');
			sb.append('}');
			return sb.toString();
		}
	}

}
