package base.hldd.structure.models.utils;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 02.11.2008
 * <br>Time: 12:00:20
 */
public class ModelCollectorTest {
    /* Correct values for desired HSB, to use when creating constants: */
    /* 1) 10 => previouslyDesired HSB (X)
    * 2) "10" => typed HSB (2)
    * 3) 10 & "10" & X"FF"
    *    10 => typed + value converted (4);  "10" => typed (2);  X"FF" => typed (8);
    * 4) 10 + "1000" + X"F"
    *    10 => operator imposed (4);  "1000" => typed (4);  X"F" => typed (4);
    * 5) VAR SHL "100"    /  10
    *    "100" => typed (3)  /  typed + value converted (4)
    * 6) 10 & (user_def_func(var1, var2) + 203)
    *    10 => typed + value converted (4);  203 => Operator imposed (HSB of user_def_func);
    * 7) 10 + (user_def_func(var1, var2) + 203)
    *    10 => Operator imposed(HSB of 2nd operand (ADDER with user_def_func));
    *    203 => Operator imposed (HSB of user_def_func);
    * 8) 10 & (user_def_func(var1, var2) & 203)
    *    10 => typed + value converted (4);  203 => typed + converted (8);
    *
    *
    * */



    private final static String[] functionDeclarations = {
            "10 & (user_def_func(var1, var2) + 203)",
            "10 + (user_def_func(var1, var2) + 203)",
            "10 & \"100101\""   //---> For every ConstantVariable operand desiredHighestSB must be calculated separately!!!
    };

}
