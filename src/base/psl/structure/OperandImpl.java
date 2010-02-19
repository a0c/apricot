package base.psl.structure;

import base.vhdl.structure.AbstractOperand;

/**
 * Class represents boolean expression in HLD.
 * Models boolean layer of PSL.
 *
 * Hierarchically, the class is equivalent to the {@link base.vhdl.structure.OperandImpl} 
 * and is introduced solely for delegation purporses, in order not to
 * change the existing hierarchy of {@link base.vhdl.structure.AbstractOperand}.
 * So, the class only wraps instances of {@link base.vhdl.structure.OperandImpl}
 * into the PSL hierarchy class {@link base.psl.structure.OperandImpl}.
 *
 * <br><br>User: Anton Chepurov
 * <br>Date: 24.09.2008
 * <br>Time: 13:27:37
 */
public class OperandImpl extends AbstractExpression {
    private final AbstractOperand baseOperand;

    public OperandImpl(AbstractOperand baseOperand) {
        this.baseOperand = baseOperand;
    }

    public AbstractOperand getBaseOperand() {
        return baseOperand;
    }

}
