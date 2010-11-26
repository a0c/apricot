package parsers.vhdl;

import base.Type;
import base.Indices;
import base.vhdl.structure.Constant;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.HashSet;

import parsers.OperandValueCalculator;
import parsers.ExpressionBuilder;

/**
 * @author Anton Chepurov
 */
public abstract class AbstractPackageBuilder implements PackageBuilder {

	protected final Map<String, Type> typeByName = new HashMap<String, Type>();

	protected OperandValueCalculator valueCalculator = new OperandValueCalculator();

	protected Collection<String> variableNames = new HashSet<String>();

	protected ExpressionBuilder expressionBuilder = new ExpressionBuilder(valueCalculator, variableNames);


	public void registerConstant(Constant newConstant) {
		variableNames.add(newConstant.getName());
		valueCalculator.addConstant(newConstant);
	}

	public void registerType(String typeName, Type type) {
		typeByName.put(typeName, type);
	}

	public boolean containsType(String typeName) {
		return typeByName.containsKey(typeName);
	}

	public Type getType(String typeName) {
		return typeByName.get(typeName);
	}

	public Indices buildIndices(String rangeAsString) throws Exception {
		return expressionBuilder.buildRange(rangeAsString);
	}

}
