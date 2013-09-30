library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_misc.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;

library crc_lib;
use work.crc_pkg.all;

entity crc is
   generic (
      CRC_CNTX_NUM : integer range 1 to CRC_CNTX_MAX_NUM := 1;
      MOD_EN_BITS  : integer range 8 to MOD_EN_BITS_MAX := 14);
   port (
      ipg_clk                  : in std_logic;                                   -- clock
      ipg_hard_async_reset_b   : in std_logic;                                   -- resetb
      ips_module_en            : in std_logic;                                   -- module enable
      ips_addr                 : in std_logic_vector(MOD_EN_BITS-3 downto 0);    -- address 
      ips_seq_access           : in std_logic;                                   -- sequential access  
      ips_spv_access           : in std_logic;                                   -- supervisor access  
      ips_test_access          : in std_logic;                                   -- test access          
      ips_rwb                  : in std_logic;                                   -- read/writeb enable 
      ips_byte_en              : in std_logic_vector(3 downto 0);                -- byte enable          
      ips_xfr_wait             : out std_logic;                                  -- transfer wait      
      ips_xfr_err              : out std_logic;                                  -- transfer error     
      ips_wdata                : in std_logic_vector(31 downto 0);               -- write data           
      ips_rdata                : out std_logic_vector(31 downto 0)               -- read data           
   );
end crc ;

architecture rtl of crc is

-- register arrays
signal crc_cfg_1      : std_logic_vector(2 downto 0);
signal crc_inp_1      : std_logic_vector(31 downto 0);
signal crc_cstat_1    : std_logic_vector(31 downto 0);
signal crc_cstat_comb_1    : std_logic_vector(31 downto 0);
signal crc_outp_1     : std_logic_vector(31 downto 0);

-- address signals
signal reg_addr_offs                        :  integer range 0 to 3;                              -- address reg offset
signal err_addr                             :  std_logic;                                  -- out of range address
signal err_byte_en                          :  std_logic;                                  -- byte enable error
signal reg_addr_strobe                      :  std_logic_vector(3 downto 0);                      -- address reg strobe


-- register read/write strobes
signal cs_read                 :  std_logic;                               -- read strobe
signal cs_write                :  std_logic;                               -- write strobe
signal crc_stat_wen_1          : std_logic_vector(3 downto 0);

-- CRC starting strobes
signal crc_d8_start_1          : std_logic;
signal crc_d16_start_1         : std_logic;
signal crc_d32_start_1         : std_logic;

-- inv/strobe vector
signal inv_1        : std_logic;
signal swap_1       : std_logic;
signal poly_1       : std_logic;

-- data input
signal crc_din_word        : std_logic_vector(31 downto 0);                    -- CRC data input (word)
signal crc_dstat_word      : std_logic_vector(31 downto 0);                    -- CRC data input (word)

--misc
signal ips_xfr_wait_c_1 :  std_logic;
signal ips_rd_done_1    :  std_logic;
signal wait_1           :  std_logic;


signal gnd                 :  std_logic_vector(31 downto 0);                   -- gnd 

begin


gnd               <= (others => '0');

reg_addr_offs     <= conv_integer(ips_addr(1 downto 0));                                                                     -- address reg offset
err_addr          <= '0' when (conv_integer(ips_addr(MOD_EN_BITS -3 downto 2)) < 1) else '1';                         -- error address indication
err_byte_en       <= '0' when (ips_byte_en = "0001" or ips_byte_en = "0010" or ips_byte_en = "0100" or  ips_byte_en = "1000" or
                               ips_byte_en = "0011" or ips_byte_en = "1100" or ips_byte_en = "1111") else '1';                   -- error address indication




-- ---------------------------------------------------------------------------------------------
--  wait generation
-- ---------------------------------------------------------------------------------------------

p_wait_1 : process (crc_stat_wen_1, crc_d8_start_1, crc_d16_start_1,
crc_d32_start_1)
begin
      if (crc_stat_wen_1 /= "0000" or crc_d8_start_1 = '1' or crc_d16_start_1 =
	  '1' or crc_d32_start_1 = '1') then
         wait_1 <= '1';
      else
         wait_1 <= '0';
      end if;
end process p_wait_1;


p_wait_gen_1 : process (cs_read,reg_addr_strobe,wait_1)
begin
      if (cs_read='1' and (reg_addr_strobe(3) = '1' or reg_addr_strobe(2) = '1')  and wait_1='1') then
         ips_xfr_wait_c_1  <= '1';   -- C_ADDR_OFFS_CRC_CSTAT or C_ADDR_OFFS_CRC_OUTP
      else
         ips_xfr_wait_c_1  <= '0';   
      end if; 
end process p_wait_gen_1;

ips_xfr_wait  <= ips_xfr_wait_c_1 and not ips_rd_done_1; 


-- ---------------------------------------------------------------------------------------------
--  IPS reg address decoder
-- ---------------------------------------------------------------------------------------------

p_reg_decoder : process (reg_addr_offs,ips_module_en,err_addr,err_byte_en, ips_rwb)
  begin
    if (reg_addr_offs = C_ADDR_OFFS_CRC_CFG and ips_module_en = '1' and err_addr = '0' and err_byte_en = '0') then
       reg_addr_strobe <= "0001";
       ips_xfr_err     <= '0';
       cs_read         <= ips_rwb;                           
       cs_write        <= not ips_rwb;                           
    elsif (reg_addr_offs = C_ADDR_OFFS_CRC_INP and ips_module_en = '1' and err_addr = '0' and err_byte_en = '0') then
       reg_addr_strobe <= "0010";
       ips_xfr_err     <= '0';
       cs_read         <= ips_rwb;                           
       cs_write        <= not ips_rwb;                           
    elsif (reg_addr_offs = C_ADDR_OFFS_CRC_CSTAT and ips_module_en = '1' and err_addr = '0' and err_byte_en = '0') then
       reg_addr_strobe <= "0100";
       ips_xfr_err     <= '0';
       cs_read         <= ips_rwb;                           
       cs_write        <= not ips_rwb;                           
    elsif (reg_addr_offs = C_ADDR_OFFS_CRC_OUTP and ips_module_en = '1' and err_addr = '0' and err_byte_en = '0') then
       reg_addr_strobe <= "1000";
       ips_xfr_err     <= '0';
       cs_read         <= ips_rwb;                           
       cs_write        <= not ips_rwb;                           
    elsif (ips_module_en = '1' and (err_addr = '1' or err_byte_en = '1')) then
       reg_addr_strobe <= "0000";
       ips_xfr_err     <= '1';
       cs_read         <= '0';                           
       cs_write        <= '0';                           
    else
       reg_addr_strobe <= "0000";
       ips_xfr_err     <= '0';
       cs_read         <= '0';                           
       cs_write        <= '0';                           
    end if;
end process p_reg_decoder;




-- ---------------------------------------------------------------------------------------------
--  IPS bus read operation
-- ---------------------------------------------------------------------------------------------

p_ips_read1_1 : process (ipg_hard_async_reset_b, ipg_clk)
begin
   if (ipg_hard_async_reset_b = '0') then
         ips_rd_done_1 <= '0';
   elsif (ipg_clk'event and ipg_clk = '1') then
         ips_rd_done_1 <= '0';
         if (cs_read = '1' and ips_rd_done_1 = '0' and
          (reg_addr_strobe(2) = '1' or reg_addr_strobe(3) = '1') ) then             -- C_ADDR_OFFS_CRC_CSTAT or C_ADDR_OFFS_CRC_OUTP
            ips_rd_done_1 <= '1';
         end if;
   end if;
end process p_ips_read1_1;




p_ips_read2_1 : process (cs_read, reg_addr_strobe,crc_cfg_1,crc_inp_1,crc_cstat_1,crc_outp_1,gnd)
begin
      if (cs_read = '1') then
         if (reg_addr_strobe(0) = '1') then                         -- C_ADDR_OFFS_CRC_CFG
            ips_rdata <= gnd(31 downto 3) & crc_cfg_1;
         elsif (reg_addr_strobe(1) = '1' ) then                      -- C_ADDR_OFFS_CRC_INP
            ips_rdata <= crc_inp_1;
         elsif (reg_addr_strobe(2) = '1' ) then                      -- C_ADDR_OFFS_CRC_CSTAT
            ips_rdata <= crc_cstat_1;
        
         elsif (reg_addr_strobe(3) = '1') then                       -- C_ADDR_OFFS_CRC_OUTP
            ips_rdata <= crc_outp_1;
        
         else
            ips_rdata <= (others => '0');
         end if;
      else
         ips_rdata  <= (others => '0');
      end if;
end process p_ips_read2_1;


-- ---------------------------------------------------------------------------------------------
--  IPS bus write operation
-- ---------------------------------------------------------------------------------------------

p_ips_write : process (ipg_hard_async_reset_b, ipg_clk)
begin
   if (ipg_hard_async_reset_b = '0') then
      crc_din_word   <= (others => '1');
      
         crc_inp_1        <= (others => '0');
         crc_d8_start_1        <= '0';
         crc_d16_start_1       <= '0'; 
         crc_d32_start_1       <= '0'; 
         crc_stat_wen_1   <= (others => '0');
         crc_cfg_1        <= (others =>  '0');

   elsif (ipg_clk'event and ipg_clk = '1') then
      -- initialization
         crc_d8_start_1        <= '0';
         crc_d16_start_1       <= '0'; 
         crc_d32_start_1       <= '0';       
         crc_stat_wen_1   <= (others => '0');
      --  write decoder
         if (cs_write = '1') then
              if (reg_addr_strobe(0) = '1') then                      -- C_ADDR_OFFS_CRC_CFG
              if (ips_byte_en(0) = '1') then
                  crc_cfg_1  <= ips_wdata(2 downto 0);
              end if;
              elsif (reg_addr_strobe(1) = '1') then                      -- C_ADDR_OFFS_CRC_INP
                 if (ips_byte_en = "0001") then    -- write CRC input byte_0         
                    crc_inp_1(7 downto 0)    <= ips_wdata(7 downto 0);     
                    crc_d8_start_1         <= '1';       
                    crc_din_word  <= X"FFFFFF" & ips_wdata(7 downto 0); 
                 elsif (ips_byte_en = "0010") then    -- write CRC input byte_1         
                    crc_inp_1(15 downto 8)   <= ips_wdata(15 downto 8);   
                    crc_d8_start_1         <= '1';       
                    crc_din_word  <= X"FFFFFF" & ips_wdata(15 downto 8); 
                 elsif (ips_byte_en = "0100") then    -- write CRC input byte_2         
                   crc_inp_1(23 downto 16)  <= ips_wdata(23 downto 16);  
                    crc_d8_start_1         <= '1';       
                    crc_din_word  <= X"FFFFFF" & ips_wdata(23 downto 16); 
                 elsif (ips_byte_en = "1000") then    -- write CRC input byte_3         
                    crc_inp_1(31 downto 24)  <= ips_wdata(31 downto 24);  
                    crc_d8_start_1         <= '1';       
                    crc_din_word  <=  X"FFFFFF" & ips_wdata(31 downto 24) ; 
                 elsif (ips_byte_en = "0011") then       -- write CRC input byte_1_0         
                    crc_inp_1(15 downto 0)   <= ips_wdata(15 downto 0);   
                    crc_d16_start_1        <= '1';
                    crc_din_word  <=  X"FFFF" & ips_wdata(15 downto 0) ; 
                 elsif (ips_byte_en = "1100") then    -- write CRC input byte_3_2         
                    crc_inp_1(31 downto 16)  <= ips_wdata(31 downto 16);  
                    crc_d16_start_1        <= '1';
                    crc_din_word  <=  X"FFFF" & ips_wdata(31 downto 16) ; 
                 elsif (ips_byte_en = "1111") then    -- write CRC input byte_3_2_1_0      
                    crc_inp_1(31 downto 0)   <= ips_wdata(31 downto 0);   
                    crc_d32_start_1            <= '1';
                    crc_din_word  <=  ips_wdata; 
              end if;                                 
              elsif (reg_addr_strobe(2) = '1') then                       -- C_ADDR_OFFS_CRC_CSTAT          
                 crc_stat_wen_1 <= ips_byte_en;
              end if;
         end if;
   end if;
   
end process p_ips_write;



-- ---------------------------------------------------------------------------------------------
--  inv/strobe vector
-- ---------------------------------------------------------------------------------------------

p_inv_swap_gen : process (crc_cfg_1)
begin
      inv_1   <= crc_cfg_1(0);
      swap_1  <= crc_cfg_1(1);
      poly_1  <= crc_cfg_1(2);
end process p_inv_swap_gen;





-- ---------------------------------------------------------------------------------------------
--  CRC comb
-- ---------------------------------------------------------------------------------------------

 p_crc_comb_1 : process (crc_d8_start_1,crc_d16_start_1, crc_d32_start_1, crc_cstat_1, crc_din_word, poly_1)
 begin
         -- CRC computation      
     if (crc_d8_start_1 = '1') then 
         if (poly_1 = '0') then
                crc_cstat_comb_1 <= f_ComputeCrc16_8(crc_cstat_1,crc_din_word(7 downto 0));
         else
                crc_cstat_comb_1 <= f_ComputeCrc32_8(crc_cstat_1,crc_din_word(7 downto 0));
         end if;                            
     elsif (crc_d16_start_1 = '1') then 
         if (poly_1 = '0') then
                crc_cstat_comb_1 <= f_ComputeCrc16_16(crc_cstat_1,crc_din_word(15 downto 0));
         else
                crc_cstat_comb_1 <= f_ComputeCrc32_16(crc_cstat_1,crc_din_word(15 downto 0));
         end if;                                 
     elsif (crc_d32_start_1 = '1') then
         if (poly_1 = '0') then
                crc_cstat_comb_1 <= f_ComputeCrc16_32(crc_cstat_1,crc_din_word);
         else
                crc_cstat_comb_1 <= f_ComputeCrc32_32(crc_cstat_1,crc_din_word);
         end if;
     else
             crc_cstat_comb_1 <= crc_cstat_1;    
     end if;
 end process p_crc_comb_1;


-- ---------------------------------------------------------------------------------------------
--  CRC computation
-- ---------------------------------------------------------------------------------------------

p_crc_eng : process (ipg_hard_async_reset_b, ipg_clk)
begin
   if (ipg_hard_async_reset_b = '0') then
      crc_dstat_word <= (others => '0'); 
         crc_cstat_1      <= (others => '1');
         crc_outp_1       <= (others => '1');      
   elsif (ipg_clk'event and ipg_clk = '1') then
      crc_dstat_word <= ips_wdata; 
         -- CRC updating 
         crc_cstat_1 <= crc_cstat_comb_1;
         -- CRC seed initialization
         if (crc_stat_wen_1(0) = '1') then
            crc_cstat_1(7 downto 0)   <= crc_dstat_word(7 downto 0);
         end if;
         if (crc_stat_wen_1(1) = '1') then
            crc_cstat_1(15 downto 8)  <= crc_dstat_word(15 downto 8);
         end if;
         if (crc_stat_wen_1(2) = '1') then
            crc_cstat_1(23 downto 16) <= crc_dstat_word(23 downto 16);
         end if;
         if (crc_stat_wen_1(3) = '1') then
            crc_cstat_1(31 downto 24) <= crc_dstat_word(31 downto 24);
         end if;
         if (inv_1 = '1' and swap_1 = '1') then
            crc_outp_1 <=  f_swap_inv_vect(crc_cstat_comb_1,poly_1,'1','1'); 
         elsif (inv_1 = '1' and swap_1 = '0') then
            crc_outp_1 <=  f_swap_inv_vect(crc_cstat_comb_1,poly_1,'0','1'); 
         elsif (inv_1 = '0' and swap_1 = '1') then
            crc_outp_1 <=  f_swap_inv_vect(crc_cstat_comb_1,poly_1,'1','0'); 
         else
            crc_outp_1 <=  crc_cstat_comb_1; 
         end if;

   end if;
end process p_crc_eng;

end rtl;
