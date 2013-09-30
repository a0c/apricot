library IEEE;
use IEEE.std_logic_1164.all;

entity b00_behav_HLDD is
   port(
      reset : in bit;
      clock : in bit;
      in1   : in integer range 32767 downto -32768;
      in2   : in integer range 32767 downto -32768;
      out1  : out integer range 32767 downto -32768;
      out2  : out integer range 32767 downto -32768
      --out_state : out integer range 2 downto 0
      );
end b00_behav_HLDD;

architecture b00_behav_HLDD of b00_behav_HLDD is

   constant A  : integer := 0;
   constant B  : integer := 1;
   constant C  : integer := 2;

   signal reg  : integer range 32767 downto -32768;
   signal state: integer range 2 downto 0;

begin

   -- 'state' vartiable
   b00_behav_HLDD_state: process(clock,reset)
   begin
      if reset =  '1'  then
	      state <= A;
      elsif clock'event and clock='1' then
	      case state is
	      when A =>
		      if not(in1=0) then
			      state <= B;
		      else
			      state <= C;
		      end if;
	      when B =>
		      if not(reg=1) then
			      state <= A;
		      else
			      state <= B;
		      end if;
	      when C =>
		      state <= A;
	      end case;
      end if;
   end process b00_behav_HLDD_state;
   --

   -- 'reg' variable
   b00_behav_HLDD_reg: process(clock,reset,state)
   begin
      if reset =  '1'  then
	      reg <= 0;
      elsif clock'event and clock='1' then
	      case state is
	      when A =>
		      if not(in1=0) then
			      reg <= in1;
		      else
			      reg <= in2;
		      end if;
	      when B =>
		      -- reg <= reg; -- optional
		      null;
	      when C =>
		      if not(in2=0) then
			      reg <= reg + in2;
		      else
			      -- reg <= reg; -- optional
			      null;
		      end if;
	      end case;
      end if;
   end process b00_behav_HLDD_reg;
   --

   -- 'out1' primary output
   b00_behav_HLDD_out1: process(clock,reset,state,reg)
   begin
      if reset =  '1'  then
	      out1 <= 0;
      elsif clock'event and clock='1' then
	      case state is
	      when A =>
		      if not(in1=0) then
			      out1 <= 1;
		      else
			      out1 <= 0;
		      end if;
	      when B =>
		      if not(reg=1) then
			      out1 <= 0;
		      else
			      out1 <= reg*2;
		      end if;
	      when C =>
		      if not(in2=0) then
			      out1 <= 1;
		      elsif in1=0 then
			      out1 <= reg;
		      else
			      out1 <= reg/2;
		      end if;
	      end case;
      end if;
   end process b00_behav_HLDD_out1;
   --

   -- 'out2' primary output
   b00_behav_HLDD_out2: process(clock,reset,state,reg)
   begin
      if reset =  '1'  then
	      out2 <= 0;
      elsif clock'event and clock='1' then
	      case state is
	      when A =>
		      if not(in1=0) then
			      out2 <= 1;
		      else
			      out2 <= 0;
		      end if;
	      when B =>
		      if not(reg=1) then
			      out2 <= 0;
		      else
			      out2 <= reg;
		      end if;
	      when C =>
		      if not(in2=0) then
			      out2 <= 1;
		      elsif in1=0 then
			      out2 <= reg/2;
		      else
			      out2 <= reg;
		      end if;
	      end case;
      end if;
   end process b00_behav_HLDD_out2;
   --

end b00_behav_HLDD;