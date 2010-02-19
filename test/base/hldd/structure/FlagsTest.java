package base.hldd.structure;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 31.10.2009
 * <br>Time: 23:46:27
 */
public class FlagsTest {

    @Test
    public void testFields() {

        // Empty flags created
        Flags flags = new Flags();
        assertFalse(flags.isCout());
        assertFalse(flags.isDelay());
        assertFalse(flags.isFunction());
        assertFalse(flags.isFSM());
        assertFalse(flags.isInput());
        assertFalse(flags.isOutput());
        assertFalse(flags.isReset());
        assertFalse(flags.isState());

        //  All flags set
        flags = new Flags();
        flags.setCout(true);
        flags.setDelay(true);
        flags.setFunction(true);
        flags.setFSM(true);
        flags.setInput(true);
        flags.setOutput(true);
        flags.setReset(true);
        flags.setState(true);
        assertTrue(flags.isCout());
        assertTrue(flags.isDelay());
        assertTrue(flags.isFunction());
        assertTrue(flags.isFSM());
        assertTrue(flags.isInput());
        assertTrue(flags.isOutput());
        assertTrue(flags.isReset());
        assertTrue(flags.isState());

        // Several flags set
        flags = new Flags();
        flags.setFSM(true);
        flags.setReset(true);
        assertFalse(flags.isCout());
        assertFalse(flags.isDelay());
        assertFalse(flags.isFunction());
        assertTrue(flags.isFSM());
        assertFalse(flags.isInput());
        assertFalse(flags.isOutput());
        assertTrue(flags.isReset());
        assertFalse(flags.isState());

    }

    @Test
    public void testToString() {

        Flags flags = new Flags();
        assertEquals("__________", flags.toString());

        flags.setDelay(true);
        assertEquals("________d_", flags.toString());

        flags.setOutput(true);
        assertEquals("____o___d_", flags.toString());

        flags.setDelay(false);
        assertEquals("____o_____", flags.toString());

        flags.setOutput(false);
        flags.setState(true);
        assertEquals("__s_______", flags.toString());

        flags.setDelay(true);
        assertEquals("__s_____d_", flags.toString());

        flags.setState(false);
        flags.setDelay(false);
        flags.setFunction(true);
        assertEquals("____f_____", flags.toString());

        flags.setFunction(false);
        flags.setConstant(true);
        assertEquals("c_________", flags.toString());

        flags.setConstant(false);
        flags.setInput(true);
        assertEquals("i_________", flags.toString());

        flags.setReset(true);
        assertEquals("i_r_______", flags.toString());

        flags.setInput(false);
        flags.setReset(false);
        flags.setFSM(true);
        assertEquals("________F_", flags.toString());

        flags.setFSM(false);
        flags.setCout(true);
        assertEquals("__n_______", flags.toString());

    }

    @Test
    public void testMerge() {
        Flags flags1 = new Flags();
        Flags flags2 = new Flags();

        flags1.setDelay(true);
        flags2.setOutput(true);

        Flags flags3 = flags1.merge(flags2);
        assertNotSame(flags3, flags1);
        assertNotSame(flags3, flags2);

        assertTrue(flags3.isDelay());
        assertTrue(flags3.isOutput());

        // check object independence/uniqueness
        flags1.setDelay(false);
        assertFalse(flags1.isDelay());
        assertTrue(flags3.isDelay());
        flags3.setOutput(false);
        assertFalse(flags3.isOutput());
        assertTrue(flags2.isOutput());

    }

    @Test
    public void testParse() {
        assertEquals("__________", Flags.parse("__________").toString());
        assertEquals("________d_", Flags.parse("________d_").toString());
        assertEquals("____o___d_", Flags.parse("____o___d_").toString());
        assertEquals("____o_____", Flags.parse("____o_____").toString());
        assertEquals("__s_____d_", Flags.parse("__s_____d_").toString());
        assertEquals("__s_______", Flags.parse("__s_______").toString());
        assertEquals("____f_____", Flags.parse("____f_____").toString());
        assertEquals("c_________", Flags.parse("c_________").toString());
        assertEquals("i_________", Flags.parse("i_________").toString());
        assertEquals("i_r_______", Flags.parse("i_r_______").toString());
        assertEquals("________F_", Flags.parse("________F_").toString());
        assertEquals("__n_______", Flags.parse("__n_______").toString());
    }

}
