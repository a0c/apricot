package base.vhdl.structure;

import parsers.vhdl.PackageParser;

import java.io.File;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import io.scan.VHDLScanner;
import base.Type;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 06.04.2009
 * <br>Time: 23:04:47
 */
public class Package {
    private final String name;
    private final Set<Constant> constants = new HashSet<Constant>();
    private final Map<String, Type> typeByName = new HashMap<String, Type>();

    public static Package parsePackageStructure(File vhdlPackageFile) throws Exception {
        PackageParser packageParser = new PackageParser(new VHDLScanner(vhdlPackageFile));
        packageParser.parse();
        return packageParser.getPackageStructure();
    }

    public Package(String name) {
        this.name = name;
    }

    public void addConstant(Constant constant) {
        constants.add(constant);
    }

    public Set<Constant> getConstants() {
        return constants;
    }

    public void addTypeByName(Map<String, Type> typeByName) {
        this.typeByName.putAll(typeByName);
    }

    public Map<String, Type> getTypeByName() {
        return typeByName;
    }
}
