library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;

package crc_pkg is

  constant CRC_CNTX_MAX_NUM  : integer := 16;  
  constant MOD_EN_BITS_MAX   : integer := 16;  

  type type_reg32     is array (natural range <>) of std_logic_vector(31 downto 0);
  type type_reg3      is array (natural range <>) of std_logic_vector(2 downto 0);
  type type_reg4      is array (natural range <>) of std_logic_vector(3 downto 0);
  type type_int2_32   is array (natural range <>) of integer range 2 to 32;


  
  constant C_ADDR_OFFS_CRC_CFG        : natural := 16#00#;          -- CRC config register
  constant C_ADDR_OFFS_CRC_INP        : natural := 16#01#;          -- CRC input register
  constant C_ADDR_OFFS_CRC_CSTAT      : natural := 16#02#;          -- CRC current status register
  constant C_ADDR_OFFS_CRC_OUTP       : natural := 16#03#;          -- CRC output register

  constant C_POLY32_G                 : std_logic_vector(31 downto 0):=  X"82608EDB";
  constant C_POLY16_G                 : std_logic_vector(31 downto 0):=  X"00008810";



function f_ComputeCrc16  ( constant PreviousCrcCheckSum   : std_logic_vector(31 downto 0);
                           constant NewData               : std_logic_vector)
                         return std_logic_vector;

function f_ComputeCrc32  ( constant PreviousCrcCheckSum   : std_logic_vector(31 downto 0);
                           constant NewData               : std_logic_vector)
                         return std_logic_vector;			 
			 
function f_swap_inv_vect ( constant vector          : std_logic_vector(31 downto 0);
                           constant size            : std_logic;
		           constant swap            : std_logic;
		           constant inv             : std_logic)
                         return std_logic_vector;

end crc_pkg ;

package body crc_pkg is



function f_swap_inv_vect ( constant vector          : std_logic_vector(31 downto 0);
                           constant size            : std_logic;
		           constant swap            : std_logic;
		           constant inv             : std_logic)
                          return std_logic_vector is

variable swap_inv_vect  : std_logic_vector(31 downto 0);
variable inv_vect       : std_logic_vector(31 downto 0);

begin    
   inv_vect := vector;
   if (inv = '1') then 
      if (size = '0') then  -- 16 bits
         inv_vect(15 downto 0) := not vector(15 downto 0); 
      else                  -- 32 bits
         inv_vect(31 downto 0) := not vector(31 downto 0); 
      end if;     	 
   end if;
   swap_inv_vect := inv_vect;
   if (swap = '1') then
      if (size = '0') then  -- 16 bits
         for i in 15 downto 0 loop
            swap_inv_vect(i) := inv_vect(15-i);
         end loop;
      else                  -- 32 bits
         for i in 31 downto 0 loop
            swap_inv_vect(i) := inv_vect(31-i);
         end loop;      
      end if;     	 
   end if;
   
   return swap_inv_vect;

end f_swap_inv_vect;


function f_ComputeCrc16  ( constant PreviousCrcCheckSum   : std_logic_vector(31 downto 0);
                           constant NewData               : std_logic_vector)
                         return std_logic_vector is
                                    
variable v_Syndrome    : std_logic_vector(31 downto 0);
variable v_NewSyndrome : std_logic_vector(31 downto 0);
variable v_TmpBit      : std_logic;
variable v_gnd         : std_logic_vector(31 downto 0);  
        
begin
   
   v_gnd := (others => '0');
   v_Syndrome := PreviousCrcCheckSum;
   for i in 0 to NewData'high loop
      v_TmpBit := v_Syndrome(15) xor NewData(i); 
      case v_TmpBit is
	 when '1' =>
	    v_NewSyndrome := v_gnd(31 downto 16) & (v_Syndrome(14 downto 0) xor C_POLY16_G(14 downto 0)) & '1';
	 when others => -- '0'
            v_NewSyndrome := v_gnd(31 downto 16) & v_Syndrome(14 downto 0) & '0';
      end case;
      v_Syndrome := v_NewSyndrome;	 
   end loop;
   return v_Syndrome;
end f_ComputeCrc16;


function f_ComputeCrc32  ( constant PreviousCrcCheckSum   : std_logic_vector(31 downto 0);
                           constant NewData               : std_logic_vector)
                         return std_logic_vector is
                                    
variable v_Syndrome    : std_logic_vector(31 downto 0);
variable v_NewSyndrome : std_logic_vector(31 downto 0);
variable v_TmpBit      : std_logic;
variable v_gnd         : std_logic_vector(31 downto 0);  
        
begin
   
   v_gnd := (others => '0');
   v_Syndrome := PreviousCrcCheckSum;
   for i in 0 to NewData'high loop
      v_TmpBit := v_Syndrome(31) xor NewData(i); 
      case v_TmpBit is
	 when '1' =>
	    v_NewSyndrome := (v_Syndrome(30 downto 0) xor C_POLY32_G(30 downto 0)) & '1';
	 when others => -- '0'
            v_NewSyndrome := v_Syndrome(30 downto 0) & '0';
      end case;
      v_Syndrome := v_NewSyndrome;	 
   end loop;
   return v_Syndrome;
end f_ComputeCrc32;

end crc_pkg ;
