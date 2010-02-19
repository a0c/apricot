
-----------------------------------------------------------------
USE work.ag_lib.all ;

library IEEE;
   use IEEE.std_logic_1164.all;
   use IEEE.std_logic_misc.all;
   use IEEE.std_logic_arith.all;
   use IEEE.std_logic_components.all;
   
ENTITY FLOATPDIV_DP IS 
   PORT  (
-- control ports:
      Clock : IN std_logic ; 
      EQ_1_OUTPUT : OUT std_logic ;           
      REG_1_ENABLE : IN std_logic ;   -- jagatavS
      REG_2_ENABLE : IN std_logic ;   -- jagatavE
      REG_3_ENABLE : IN std_logic ;   -- jagatavM
      REG_4_ENABLE : IN std_logic ;   -- jagajaS
      REG_5_ENABLE : IN std_logic ;   -- jagajaE
      REG_6_ENABLE : IN std_logic ;   -- jagajaM
      REG_7_ENABLE : IN std_logic ;   -- vastusS
      REG_8_ENABLE : IN std_logic ;   -- vastusE
      REG_9_ENABLE : IN std_logic ;   -- vastusM
      REG_10_ENABLE : IN std_logic ;   -- DivideByZero
               
-- data (non-control) ports:
      jagatavS : IN std_logic_vector(0 downto 0) ; 
      jagatavE : IN std_logic_vector (7 downto 0); 
      jagatavM : IN std_logic_vector (22 downto 0); 
      jagajaS : IN std_logic_vector(0 downto 0) ; 
      jagajaE : IN std_logic_vector (7 downto 0);
      jagajaM : IN std_logic_vector (22 downto 0);
      vastusS : OUT std_logic_vector(0 downto 0) ; 
      vastusE : OUT std_logic_vector (7 downto 0) ; 
      vastusM : OUT std_logic_vector (22 downto 0) ;
      DivideByZero: OUT std_logic_vector(0 downto 0) ); 
END ; 

USE work.ag_lib.all ;


-----------------------------------------------------------------
ARCHITECTURE STRUCTURE OF FLOATPDIV_DP IS 

-- signal declarations (nets with no ports):
   SIGNAL SUBTR_1 : std_logic_vector (22 downto 0) ; 
   SIGNAL DIV_1 : std_logic_vector (7 downto 0) ;   
   SIGNAL EQ_1 : std_logic_vector (0 downto 0) ; 
   SIGNAL XOR_1 : std_logic_vector(0 downto 0);       
   SIGNAL REG_1 : std_logic_vector(0 downto 0); 
   SIGNAL REG_2 : std_logic_vector (7 downto 0) ; 
   SIGNAL REG_3 : std_logic_vector (22 downto 0) ; 
   SIGNAL REG_4 : std_logic_vector(0 downto 0); 
   SIGNAL REG_5 : std_logic_vector (7 downto 0) ; 
   SIGNAL REG_6 : std_logic_vector (22 downto 0) ;
   SIGNAL CONST0 : std_logic_vector (22 downto 0) ; 
   
    
  
-- configuration (binding of instances):
   FOR I_EQ_1 : AG_EQ  USE ENTITY WORK.AG_EQ(behave) ;
   FOR I_SUBTR_1 : AG_SUBTR  USE ENTITY WORK.AG_SUBTR(behave) ;
   FOR I_DIV_1 : AG_DIV  USE ENTITY WORK.AG_DIV(behave) ;
   FOR I_XOR_1 : AG_XOR  USE ENTITY WORK.AG_XOR(behave) ;
   FOR I_REG_1 : AG_REG  USE ENTITY WORK.AG_REG(behave) ;
   FOR I_REG_2 : AG_REG  USE ENTITY WORK.AG_REG(behave) ;
   FOR I_REG_3 : AG_REG  USE ENTITY WORK.AG_REG(behave) ;
   FOR I_REG_4 : AG_REG  USE ENTITY WORK.AG_REG(behave) ;
   FOR I_REG_5 : AG_REG  USE ENTITY WORK.AG_REG(behave) ;
   FOR I_REG_6 : AG_REG  USE ENTITY WORK.AG_REG(behave) ;
   FOR I_REG_7 : AG_REG  USE ENTITY WORK.AG_REG(behave) ;
   FOR I_REG_8 : AG_REG  USE ENTITY WORK.AG_REG(behave) ;
   FOR I_REG_9 : AG_REG  USE ENTITY WORK.AG_REG(behave) ;    
   FOR I_REG_10 : AG_REG  USE ENTITY WORK.AG_REG(behave) ;       
  
BEGIN 

   CONST0 <= "00000000000000000000000";

-- instantiations of FU's etc (AG_LIB modules):

   I_REG_1 : AG_REG
      GENERIC MAP  (1, 1) 
      PORT MAP  (jagatavS, Clock, REG_1_ENABLE, REG_1) ; 
   I_REG_2 : AG_REG
      GENERIC MAP  (8, 8) 
      PORT MAP  (jagatavE, Clock, REG_2_ENABLE, REG_2) ; 
   I_REG_3 : AG_REG
      GENERIC MAP  (23, 23) 
      PORT MAP  (jagatavM, Clock, REG_3_ENABLE, REG_3) ; 
   I_REG_4 : AG_REG
      GENERIC MAP  (1, 1) 
      PORT MAP  (jagajaS, Clock, REG_4_ENABLE, REG_4) ; 
   I_REG_5 : AG_REG
      GENERIC MAP  (8, 8) 
      PORT MAP  (jagajaE, Clock, REG_5_ENABLE, REG_5) ; 
   I_REG_6 : AG_REG
      GENERIC MAP  (23, 23) 
      PORT MAP  (jagajaM, Clock, REG_6_ENABLE, REG_6) ; 
   I_REG_7 : AG_REG
      GENERIC MAP  (1, 1) 
      PORT MAP  (XOR_1, Clock, REG_7_ENABLE, VastusS) ; 
   I_REG_8 : AG_REG
      GENERIC MAP  (8, 8) 
      PORT MAP  (DIV_1, Clock, REG_8_ENABLE, VastusE) ; 
   I_REG_9 : AG_REG
      GENERIC MAP  (23, 23) 
      PORT MAP  (SUBTR_1, Clock, REG_9_ENABLE, VastusM) ;     
   I_REG_10 : AG_REG
      GENERIC MAP  (1, 1) 
      PORT MAP  (EQ_1, Clock, REG_10_ENABLE, DivideByZero) ;                
   I_EQ_1 : AG_EQ
      GENERIC MAP  (23, 1) 
      PORT MAP  (REG_6, const0, EQ_1_OUTPUT) ; 
   I_SUBTR_1 : AG_SUBTR
      GENERIC MAP  (23, 23) 
      PORT MAP  (REG_3, REG_6, SUBTR_1) ;           
   I_DIV_1 : AG_DIV
      GENERIC MAP  (8, 8) 
      PORT MAP  (REG_2, REG_5, DIV_1) ;    
   I_XOR_1 : AG_XOR
      GENERIC MAP  (1, 1) 
      PORT MAP  (REG_1, REG_4, XOR_1) ;      
   

END ; 


library IEEE;
   use IEEE.std_logic_1164.all;
   use IEEE.std_logic_misc.all;
   use IEEE.std_logic_arith.all;
   use IEEE.std_logic_components.all;

-- Entity generated from: DIV
-----------------------------------------------------------------
USE work.ag_lib.all ;

ENTITY FLOATPDIV_FSM IS 
   PORT  (
-- control ports:
         Clock : IN std_logic ; 
         EQ_1_OUTPUT : IN std_logic ; 
         SYN_RESET : IN std_logic  ; 
         REG_1_ENABLE : OUT std_logic ; 
         REG_2_ENABLE : OUT std_logic ; 
         REG_3_ENABLE : OUT std_logic ; 
         REG_4_ENABLE : OUT std_logic ; 
         REG_5_ENABLE : OUT std_logic ; 
         REG_6_ENABLE : OUT std_logic ; 
         REG_7_ENABLE : OUT std_logic ; 
         REG_8_ENABLE : OUT std_logic ; 
         REG_9_ENABLE : OUT std_logic ; 
         REG_10_ENABLE : OUT std_logic) ;   
               
END ; 


-----------------------------------------------------------------
ARCHITECTURE BEHAVIOUR OF FLOATPDIV_FSM IS 
   TYPE states IS  (s0, s1, s2, s3) ; 
   SIGNAL pres_state, next_state : states ; 
BEGIN 
sequencing:
--   PROCESS (pres_state, SYN_RESET, REG_1_ENABLE, REG_2_ENABLE, REG_3_ENABLE, REG_4_ENABLE, REG_5_ENABLE, REG_6_ENABLE, REG_7_ENABLE, REG_8_ENABLE, REG_9_ENABLE, REG_10_ENABLE) 
   PROCESS (pres_state, SYN_RESET, EQ_1_OUTPUT) 

   BEGIN 
      REG_1_ENABLE <= '0' ; 
      REG_2_ENABLE <= '0' ; 
      REG_3_ENABLE <= '0' ; 
      REG_4_ENABLE <= '0' ; 
      REG_5_ENABLE <= '0' ; 
      REG_6_ENABLE <= '0' ;
      REG_7_ENABLE <= '0' ; 
      REG_8_ENABLE <= '0' ; 
      REG_9_ENABLE <= '0' ;   
      REG_10_ENABLE <= '0' ; 

      CASE pres_state IS 
         WHEN s0 => 
            next_state <= s1 ; 
            REG_1_ENABLE <= '1' ; 
            REG_2_ENABLE <= '1' ; 
            REG_3_ENABLE <= '1' ; 
            REG_4_ENABLE <= '1' ; 
            REG_5_ENABLE <= '1' ; 
            REG_6_ENABLE <= '1' ;     
         WHEN s1 => 
            IF (EQ_1_OUTPUT = '1') THEN 
             next_state <= s2 ;          
            ELSE 
              next_state <= s3 ;        
            END IF ;
         WHEN s2 => 
            next_state <= s0 ;     
            REG_10_ENABLE <= '1' ;          
         WHEN s3 => 
            next_state <= s0 ;          
            REG_7_ENABLE <= '1' ; 
            REG_8_ENABLE <= '1' ; 
            REG_9_ENABLE <= '1' ; 	
      END CASE ; 

      IF  (SYN_RESET = '1' ) THEN 
         next_state <= s0 ; 
      END IF ; 

   END PROCESS sequencing ; 
 
state_memory:
   PROCESS
   BEGIN 
      WAIT UNTIL Clock'EVENT AND Clock = '1' ;
      pres_state <= next_state; 
   END PROCESS state_memory ;

END ; 

 
library IEEE;
   use IEEE.std_logic_1164.all;
   use IEEE.std_logic_misc.all;
   use IEEE.std_logic_arith.all;
   use IEEE.std_logic_components.all;

-----------------------------------------------------------------

ENTITY FLOATPDIV IS 
   PORT  (
-- control ports:
      Clock : IN std_logic ; 
      SYN_RESET : IN std_logic ; 
-- data (non-control) ports:
 --      : IN std_logic ; 
      DivideByZero : OUT std_logic_vector(0 downto 0) ; 
      jagatavS : IN std_logic_vector(0 downto 0) ; 
      jagatavE : IN std_logic_vector (7 downto 0); 
      jagatavM : IN std_logic_vector (22 downto 0); 
      jagajaS : IN std_logic_vector(0 downto 0) ; 
      jagajaE : IN std_logic_vector (7 downto 0);
      jagajaM : IN std_logic_vector (22 downto 0);
      vastusS : OUT std_logic_vector(0 downto 0) ; 
      vastusE : OUT std_logic_vector (7 downto 0) ; 
      vastusM : OUT std_logic_vector (22 downto 0)) ;
END ; 

-----------------------------------------------------------------
ARCHITECTURE STRUCTURE OF FLOATPDIV IS 

-- component declarations:
   COMPONENT FLOATPDIV_FSM
      PORT  (
-- control ports:
         Clock : IN std_logic ; 
         EQ_1_OUTPUT : IN std_logic ; 
         SYN_RESET : IN std_logic  ; 
         REG_1_ENABLE  : OUT std_logic ; 
         REG_2_ENABLE  : OUT std_logic ; 
         REG_3_ENABLE  : OUT std_logic ; 
         REG_4_ENABLE  : OUT std_logic ; 
         REG_5_ENABLE  : OUT std_logic ; 
         REG_6_ENABLE  : OUT std_logic ; 
         REG_7_ENABLE  : OUT std_logic ; 
         REG_8_ENABLE  : OUT std_logic ; 
         REG_9_ENABLE  : OUT std_logic ;           
         REG_10_ENABLE : OUT std_logic );  
         
   END COMPONENT ; 
   
   
   COMPONENT FLOATPDIV_DP
      PORT  (
-- control ports:
         Clock : IN std_logic ; 
         EQ_1_OUTPUT : OUT std_logic ; 
                               
-- data (non-control) ports:
      REG_1_ENABLE  : IN std_logic ; 
      REG_2_ENABLE  : IN std_logic ;
      REG_3_ENABLE  : IN std_logic ;
      REG_4_ENABLE  : IN std_logic ;
      REG_5_ENABLE  : IN std_logic ;
      REG_6_ENABLE  : IN std_logic ;
      REG_7_ENABLE  : IN std_logic ; 
      REG_8_ENABLE  : IN std_logic ;
      REG_9_ENABLE  : IN std_logic ;  
      REG_10_ENABLE :  std_logic ;
      jagatavS : IN std_logic_vector(0 downto 0) ; 
      jagatavE : IN std_logic_vector (7 downto 0); 
      jagatavM : IN std_logic_vector (22 downto 0); 
      jagajaS : IN std_logic_vector(0 downto 0) ; 
      jagajaE : IN std_logic_vector (7 downto 0);
      jagajaM : IN std_logic_vector (22 downto 0);
      vastusS : OUT std_logic_vector(0 downto 0) ; 
      vastusE : OUT std_logic_vector (7 downto 0) ; 
      vastusM : OUT std_logic_vector (22 downto 0) ;
      DivideByZero: OUT std_logic_vector(0 downto 0) );     
   END COMPONENT ; 

-- signal declarations (nets with no ports):
   SIGNAL EQ_1_OUTPUT : std_logic; 
   SIGNAL REG_1_ENABLE, REG_2_ENABLE, REG_3_ENABLE, REG_4_ENABLE, REG_5_ENABLE, REG_6_ENABLE, REG_7_ENABLE, REG_8_ENABLE, REG_9_ENABLE, REG_10_ENABLE: std_logic;

-- configuration (binding of instances):

BEGIN 
   ctrl_inst : FLOATPDIV_FSM
      PORT MAP  (
        Clock, 
        EQ_1_OUTPUT, 
        SYN_RESET, 
        REG_1_ENABLE, 
        REG_2_ENABLE, 
        REG_3_ENABLE, 
        REG_4_ENABLE, 
        REG_5_ENABLE, 
        REG_6_ENABLE, 
        REG_7_ENABLE, 
        REG_8_ENABLE, 
        REG_9_ENABLE, 
        REG_10_ENABLE); 
         
   dp_inst : FLOATPDIV_DP
       PORT MAP  (
        Clock, 
        EQ_1_OUTPUT, 
        REG_1_ENABLE, 
        REG_2_ENABLE, 
        REG_3_ENABLE, 
        REG_4_ENABLE,
        REG_5_ENABLE, 
        REG_6_ENABLE, 
        REG_7_ENABLE, 
        REG_8_ENABLE, 
        REG_9_ENABLE, 
        REG_10_ENABLE, 
        jagatavS, 
        jagatavE, 
        jagatavM, 
        jagajaS, 
        jagajaE,
        jagajaM,
        vastusS, 
        vastusE,
        vastusM,
        DivideByZero  
        
        ); 
    
END ; 










