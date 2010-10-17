package base.hldd.structure.variables.utils;

import parsers.hldd.Collector;

/**
 * @author Anton Chepurov
 */
public interface GraphVariableCreator {
	/**
	 * Creates instances of {@link base.hldd.structure.variables.GraphVariable}
	 * from parsed data.
	 *
	 * @param collector where to take the parsed data from and place created variables to
	 * @throws Exception if an error occurs while creating
	 *                   {@link base.hldd.structure.variables.GraphVariable} instance
	 */
	void createGraphVariables(Collector collector) throws Exception;
}
