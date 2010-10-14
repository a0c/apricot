package parsers.vhdl;

import base.Type;
import base.vhdl.structure.*;
import base.vhdl.structure.Package;

import java.math.BigInteger;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 01.04.2009
 * <br>Time: 15:46:14
 */
public class DefaultPackageBuilder extends AbstractPackageBuilder {
    private Package aPackage;

    public void buildConstant(String constantName, Type type, BigInteger value) {
        if (aPackage != null) {
            Constant newConstant = new Constant(constantName, type, value);

            aPackage.addConstant(newConstant);
            registerConstant(newConstant);
        }
    }

    public Package getPackageStructure() {
        aPackage.addTypeByName(typeByName);
        return aPackage;
    }

    public void buildPackage(String packageName) {
        aPackage = new Package(packageName);
    }
}
