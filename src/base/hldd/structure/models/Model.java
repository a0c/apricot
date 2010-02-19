package base.hldd.structure.models;

import base.hldd.structure.variables.AbstractVariable;
import java.util.HashMap;

/**
 * Class representing AGM RTL DD.
 *
 * <p>User: Anton Chepurov
 * <br>Date: 27.06.2007
 * <br>Time: 20:24:35
 */
public class Model extends BehModel {

    private int coutCount;

    @Deprecated
    public Model() {
        super();
    }

    public Model(HashMap<Integer, AbstractVariable> indexVariableHash) {
        super(indexVariableHash);
    }

    protected String composeFileString(String comment) {

        StringBuffer str = new StringBuffer();

        // add COMMENT if exists
        if (comment != null && !comment.equals("")) {
            String[] lines = comment.split("\n");
            for (String line : lines) {
                str.append(";").append(line).append("\n");
            }
        }

        str.append("\nSTAT#\t").append(nodeCount).append(" Nods,  ");
        str.append(varCount).append(" Vars,  ");
        str.append(graphCount).append(" Grps,  ");
        str.append(inpCount).append(" Inps,  ");
        str.append(outpCount).append(" Outs,  ");
        str.append(constCount).append(" Cons,  ");
        str.append(funcCount).append(" Funs,  ");
        str.append(coutCount).append(" C_outs\n");

        str.append("COUT#\t");

        for (int i = 0; i < coutCount; i++) {
            str.append(i + inpCount + constCount + funcCount).append(", ");
        }
        str.delete(str.length() - 2, str.length());

        str.append("\n\nMODE#\tRTL\n\n");

        for (int i = 0; i < varCount; i++) {

            if (i == 0)
                str.append(";inputs\n");
            if (i == inpCount)
                str.append("\n\n;constants\n");
            if (i == inpCount + constCount)
                str.append("\n\n;functions\n");
            if (i == inpCount + constCount + funcCount)
                str.append("\n\n;state variable\n");
            if (i == inpCount + constCount + funcCount + 1)
                str.append("\n\n;control part outputs\n");
            if (i == inpCount + constCount + funcCount + coutCount)
                str.append("\n\n;control graph\n");
            if (i == inpCount + constCount + funcCount + coutCount + 1)
                str.append("\n\n;datapath graphs\n");

            str.append(vars.containsKey(varNameByIndex.get(i)) ? vars.get(varNameByIndex.get(i)) : consts.get(varNameByIndex.get(i))).append("\n");

        }

        return str.toString();
    }

    protected void addStat(AbstractVariable variable) {
        super.addStat(variable);
        if (variable.isCout() || variable.isState()) coutCount++;
    }

    public int coutOffset(){ return funcOffset() + funcCount; }

    public int graphOffset() { return coutOffset() + coutCount; }

    /* Getters START */

    public int getCoutCount() {
        return coutCount;
    }

    /* Getters END */

    /* Setters START*/

    public void setCoutCount(int coutCount) {
        this.coutCount = coutCount;
    }

    /* Setters END*/

}
