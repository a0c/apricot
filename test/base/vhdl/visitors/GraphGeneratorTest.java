package base.vhdl.visitors;

import org.junit.Test;

/**
 * @author Anton Chepurov
 */
public class GraphGeneratorTest {


	@Test
	public void someTest() {
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
