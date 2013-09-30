entity b00 is 

port(
   in1: in integer range 32767 downto -32768; -- 16 bit 
   in2: in integer range 32767 downto -32768; -- 16 bit 
   reset: in bit;
   out1 : out integer range 32767 downto -32768; -- 16 bit
   out2 : out integer range 32767 downto -32768; -- 16 bit
   clock : in bit );

end b00;

architecture BEHAV of b00 is
	constant A:integer:=0;
	constant B:integer:=1;
	constant C:integer:=2;
   --signal reg: integer range 32767 downto -32768; 
begin
  process(clock,reset)
        
	variable reg: integer range 32767 downto -32768; 
	
        -- equivalente ad 'out2: out integer;' su 16 bit
	variable state: integer range 2 downto 0;

  begin
	  if reset =  '1'  then 
			state:=A; 
			out1<=0; 
			out2<=0; 		
	  elsif clock'event and clock='1' then
		case state is 
		when A => -- 0
			if not(in1=0) then 
				reg:=in1; 
				out1<=1; 
				out2<=1; 
				state:=B; -- 1 
			else 
				reg:=in2; 
				out1<=0; 
				out2<=0; 
				state:=C; -- 2
			end if; 
		when B => -- 1 
			if not(reg=1) then
				out1<=0; 
				out2<=0; 
				state:=A; -- 0 
			else 
				out1<=reg*2; 
				out2<=reg; 
				state:=B; -- 1
			end if;
		when C => -- 2
			if not(in2=0) then 
				reg:=reg+in2; 
				out1<=1; 
				out2<=1; 
				state:=C;  -- 2
			elsif in1=0 then 
				out1<=reg; 
				out2<=reg/2; 
			else 
				out1<=reg/2; 
				out2<=reg; 
			end if; 
			state:=A; -- 0
		end case; 
	  end if; 
  end process;
end BEHAV;
