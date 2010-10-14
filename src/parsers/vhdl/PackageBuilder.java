package parsers.vhdl;

import base.Type;

import java.math.BigInteger;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 01.04.2009
 * <br>Time: 16:15:42
 */
public interface PackageBuilder {

    /**
     * Builds constants from {@link io.scan.VHDLToken.Type#CONSTANT_DECL CONSTANT_DECL} and
     * {@link io.scan.VHDLToken.Type#TYPE_ENUM_DECL TYPE_DECL}.
     *
     * @param constantName name of the constant being built
	 * @param type type of the constant being built
     * @param value value of the new constant being built
     */
    void buildConstant(String constantName, Type type, BigInteger value);

    /**
     * Maps type of a {@link io.scan.VHDLToken.Type#TYPE_ENUM_DECL TYPE_ENUM_DECL} with the type's name.
     * The mapping is used when calculating type for a signal of type {@link io.scan.VHDLToken.Type# TYPE_ENUM_DECL
     * TYPE_ENUM_DECL}.
     *
     * @param typeName name of the type to register
     * @param type to register
     */
    void registerType(String typeName, Type type);

    /**
     * Checks whether builder contains the specified {@link io.scan.VHDLToken.Type#TYPE_ENUM_DECL TYPE_ENUM_DECL}.
     *
     * @param typeName name of the type to check
     * @return whether builder contains the specified type
     */
    boolean containsType(String typeName);

    /**
     * @param typeName name of the type to obtain
     * @return type specified by typeName 
     */
    Type getType(String typeName);
}
