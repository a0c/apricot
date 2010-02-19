use settings_pkg.all;

entity ex1 is
  port( V1_T1, V1_T2		: in integer range 127 downto -128;
        V2_T1, V2_T2, V2_T3	: in integer range 127 downto -128;
        cS1_C1, cS1_C2		: in bit;
        cS2_C				: in cS2_type;
		cS3_C1, cS3_C2, cS3_C3 : in bit;
        V1, V2	 			: out integer range 127 downto -128;
        CLOCK    			: in bit
                           );
end ex1;

architecture BEHAV of ex1 is

  constant cS2_C_W1 : cS2_type := 1;
  constant cS2_C_W2 : cS2_type := 2;
  constant cS2_C_W3 : cS2_type := 3;

  begin
  process(CLOCK)
 begin 
	if (cS1_C1 and cS1_C2)
	then 
		V1 <= V1_T1;
	else 
		V1 <= V1_T2;
	end if;
	
	case cS2_C is 
	when cS2_C_W1 => 
		V2 <= V2_T1;
	when cS2_C_W2 => 
		V2 <= V2_T2;
	when cS2_C_W3 => 
		V1 <= V1_T2;
		if(cS3_C1 and ((not cS3_C2) or cS3_C3))
		then 
			V2 <= V2_T2;
		else 
			V2 <= V2_T3; 
	end if;
	end case;
  end process;
End BEHAV;
