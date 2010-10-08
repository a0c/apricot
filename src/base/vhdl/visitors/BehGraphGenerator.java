package base.vhdl.visitors;

import base.vhdl.structure.nodes.*;
import ui.ConfigurationHandler;
import ui.ConverterSettings;

import java.util.Collection;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 06.02.2008
 * <br>Time: 22:55:30
 */
public class BehGraphGenerator extends GraphGenerator {
    private Collection<String> registers;

    public BehGraphGenerator(ConfigurationHandler config, ConverterSettings settings, Collection<String> registers) {
        super(config, settings, Type.Beh);
        this.registers = registers;
    }

    public void visitProcess(base.vhdl.structure.Process process) throws Exception {

        if (modelCollector.hasPartialAssignmentsIn(process)) {
            /* At first, process partial settings, like "Parity(7) <= smth;" */
            processPartialSettings(process);
        }

        /* (Re)Traverse the root node until all the variables
        *  that are set within this process are processed. */
        while (true) {
            if (!couldProcessNextGraphVariable(null, process.getRootNode()))
                break;
            else
                processedGraphVars.add(graphVariable.getName());

        }

    }

    protected void doCheckTruePart(IfNode ifNode) throws Exception { /* do nothing */ }

    protected void doCheckFalsePart(IfNode ifNode) throws Exception { /* do nothing */ }

    protected void doCheckWhenTransitions(WhenNode whenNode) throws Exception { /* do nothing */ }

    protected boolean isDelay(String variableName) {
        return registers.contains(variableName);
    }

}
