package base.vhdl.visitors;

import org.junit.Test;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.*;
import static junit.framework.Assert.*;
import base.vhdl.structure.Process;
import ui.ConfigurationHandler;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 04.11.2009
 * <br>Time: 10:49:23
 */
public class VariableNameReplacerImplTest {

    @Test
    public void prefix() {
		ConfigurationHandler config = createMock(ConfigurationHandler.class);
		expect(config.getStateVarName()).andReturn("");
		replay(config);

		VariableNameReplacerImpl nameReplacer = new VariableNameReplacerImpl(config);

        Process mockProcess = createMock(Process.class);
        expect(mockProcess.getName()).andReturn("REG_SET");
        expect(mockProcess.getName()).andReturn("ANOTHER");
        expect(mockProcess.getName()).andReturn("");
        expect(mockProcess.getName()).andReturn(null);
        expect(mockProcess.getName()).andReturn(null);
        expect(mockProcess.getName()).andReturn(null);
        replay(mockProcess);

        assertEquals("#REG_SET#__", nameReplacer.createPrefix(mockProcess));
        assertEquals("#ANOTHER#__", nameReplacer.createPrefix(mockProcess));
        assertEquals("#PROCESS_1#__", nameReplacer.createPrefix(mockProcess));
        assertEquals("#PROCESS_2#__", nameReplacer.createPrefix(mockProcess));
        assertEquals("#PROCESS_3#__", nameReplacer.createPrefix(mockProcess));
        assertEquals("#PROCESS_4#__", nameReplacer.createPrefix(mockProcess));

    }
}
