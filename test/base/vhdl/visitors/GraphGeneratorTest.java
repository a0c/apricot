package base.vhdl.visitors;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 02.10.2008
 * <br>Time: 11:57:18
 */
public class GraphGeneratorTest {
    private static final String[] correctResetNames = {
            "RESET",
            "reset",
            "Reset",
            "ReSeT",
            "ResEt",
            "SRESET",
            "O_THAT_IS_RESET_YES",
            "is_reset"
    };
    private static final String[] incorrectResetNames = {
            "bla",
            "Rese",
            "RST"
    };
    private static final String[] correctClockNames = {
            "CLOCK",
            "clock",
            "Clock",
            "ClOcK",
            "SCLOCK",
            "O_THAT_IS_CLOCK_YES",
            "is_clock",
            "CLK",
            "clk",
            "Clk",
            "ClK",
            "SCLK",
            "O_THAT_IS_CLK_YES",
            "is_clk",            
    };


    @Test public void correctResetNameAccepted() {
        for (String validRstName : correctResetNames) {
            assertTrue(GraphGenerator.isResetName(validRstName));
        }
    }

    @Test public void incorrectResetDenied() {
        for (String invalidRstName : incorrectResetNames) {
            assertFalse(GraphGenerator.isResetName(invalidRstName));
        }
    }

    @Test public void correctClockNameAccepted() {
        for (String validClkName : correctClockNames) {
            assertTrue("\"" + validClkName + "\" is not recognized as CLOCK name", 
                    GraphGenerator.isClockName(validClkName));
        }
    }
    
    @Test public void incorrectClockNameDenied() {
        String validClkName = "CLOOK";
        assertFalse("\"" + validClkName + "\" is recognized as CLOCK name",
                GraphGenerator.isClockName(validClkName));
    }

    @Test public void someTest() {
/*
        ### /// ### /// ### /// ### /// ### /// ###
        Here remember that ":=" is a Blocking assignment,
        and "<=" is a Non-Blocking assignment.
        The result of the Blocking one is observable AT ONCE.
        The result of the Non-Blocking one is observable only AT THE END OF THE PROCESS.
        ### /// ### /// ### /// ### /// ### /// ###

        ########### 1 ###########
        conta_tmp := conta_tmp+1;
		if conta_tmp = 8 then
			conta_tmp := 0;
        end if;

        ########### 2 ###########
        conta_tmp := conta_tmp+1;
		if conta_tmp = 8 then
			conta_tmp(2) := 0;
		end if;

        ########### 3 ###########
        cts   <= '1'; //init

        if rtr = '1' then
            cts   <= '1';
        end if ;
        if rtr = '0' then
            cts <= '0' ;
        end if ;

        cts   <= '0'; //smth else

        ########### 4 ###########
        IF ((tre = '0') OR (dsr = '0')) THEN
            error <= '1';
            error <= '1';
        ELSIF (NOT ((tre = '0') OR (dsr = '0'))) THEN
            error <= '1';
            error <= '0';
        END IF;

        ########### 5 ###########
        crc_d8_start_1        <= '0';
        

*/
    }
}
