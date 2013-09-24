package parsers.hldd;

import base.Range;
import base.hldd.structure.nodes.utils.Condition;
import io.scan.HLDDScanner;
import base.hldd.structure.Flags;

import java.math.BigInteger;
import java.util.TreeMap;

/**
 * @author Anton Chepurov
 */
public class StructureParser {
	private final HLDDScanner scanner;
	private final StructureBuilder builder;

	public StructureParser(HLDDScanner scanner, StructureBuilder structureBuilder) {
		this.scanner = scanner;
		this.builder = structureBuilder;
	}

	public void parse() throws Exception {
		String token;

		while ((token = scanner.next()) != null) {
			if (token.startsWith("STAT#")) {
				token = token.substring(token.indexOf("#") + 1).trim();
				/* Collect Stat array */
				String[] stats = token.split(",");
				String[] statArray = new String[stats.length * 2];
				int index = 0;
				for (String stat : stats) {
					String[] pair = stat.trim().split("\\s");
					if (pair.length != 2) {
						throw new Exception("Line of statistics is malformed. A value-identifier pair doesn't consist of 2 elements: " + stat.trim());
					}
					statArray[index++] = pair[1].trim();
					statArray[index++] = pair[0].trim();
				}

				builder.buildStat(statArray);
			} else if (token.startsWith("VAR#")) {
				/* Extract INDEX */
				int index = Integer.parseInt(token.substring(token.indexOf("#") + 1, token.indexOf(":")).trim());
				/* Extract FLAGS */
				Flags flags = Flags.parse(token.substring(token.indexOf("(") + 1, token.indexOf(")")).trim());
				/* Extract NAME and RANGE */
				NameAndRange holder = parseNameAndRange(token.substring(token.indexOf("\"") + 1, token.lastIndexOf("\"")).trim());
				String name = holder.name;
				Range range = holder.range;
				/* Extract HIGHEST SIGNIFICANT BIT and build LENGTH */
				int highestSB = Integer.parseInt(token.substring(token.indexOf("<") + 1, token.lastIndexOf(":")).trim());
				Range length = new Range(highestSB, 0);
				/* Analyze FLAGS */
				if (flags.isConstant()) {
					/* CONSTANT */
					/* Extract CONSTANT VALUE */
					BigInteger constValue = new BigInteger(token.substring(token.indexOf("=") + 1).trim());
					builder.buildConstant(index, name, length, constValue);

				} else if (flags.isFunction()) {
					/* FUNCTION */
					/* Read next token */
					token = scanner.next();
					if (!token.startsWith("FUN#")) {
						throw new Exception("Function declaration is expected but not found on line: " + token);
					}
					/* Extract FUNCTION TYPE */
					String functionType = token.substring(token.indexOf("#") + 1, token.indexOf("(")).trim();
					/* Extract INPUTS */
					String[] inputDeclarations = token.substring(token.indexOf("(") + 1, token.indexOf(")")).trim().split(",");
					int[] inputIndices = new int[inputDeclarations.length];
					Range[] inputRanges = new Range[inputDeclarations.length];
					for (int i = 0; i < inputDeclarations.length; i++) {
						String inputDeclaration = inputDeclarations[i].trim();
						/* Extract input INDEX and RANGE, if any */
						int inputIndex = Integer.parseInt(inputDeclaration.substring(inputDeclaration.indexOf("=") + 1, inputDeclaration.lastIndexOf("<")).trim());
						inputIndices[i] = inputIndex;
						inputRanges[i] = parseRange(inputDeclaration.substring(inputDeclaration.indexOf("=")));
					}
					int nameIdx = name.contains("____") ? Integer.parseInt(name.substring(name.lastIndexOf("_") + 1)) : -1;
					builder.buildFunction(index, name, nameIdx, functionType, inputIndices, inputRanges, length);

				} else if (flags.isInput()) {
					/* VARIABLE (INPUT PORTS) */
					builder.buildVariable(index, flags, name, length);
				} else {
					/* GRAPH */
					/* Read next token */
					token = scanner.next();
					if (!token.startsWith("GRP#")) {
						throw new Exception("Graph declaration is expected but not found on line: " + token);
					}
					/* Extract GRAPH INDEX */
					int graphIndex = Integer.parseInt(token.substring(token.indexOf("#") + 1, token.indexOf(":")).trim());
					/* Extract GRAPHS LENGTH */
					int graphLength = Integer.parseInt(token.substring(token.lastIndexOf("=") + 1, token.indexOf("-")).trim());
					builder.buildGraph(index, flags, name, range, length, graphLength, graphIndex);
					/* Read NODES */
					for (int nodeIndex = 0; nodeIndex < graphLength; nodeIndex++) {
						token = scanner.next();
						/* Extract RELATIVE NODE INDEX */
						int relativeNodeIndex = Integer.parseInt(token.substring(0, token.indexOf(":")).trim().split("\\s")[1].trim());
						/* Extract DEPENDENT VARIABLE INDEX */
						int depVarIndex = Integer.parseInt(token.substring(token.lastIndexOf("=") + 1, token.indexOf("\"")).trim());
						/* Extract RANGE if any */
						Range depVarRange = parseRange(token.substring(token.indexOf("\"") + 1, token.lastIndexOf("\"")).trim());
						/* Extract SUCCESSORS */
						String successorsDecl = token.substring(token.indexOf("(", token.indexOf(")")) + 1, token.indexOf(")", token.indexOf(")") + 1)).trim();
						TreeMap<Condition, Integer> successors = null;
						if (successorsDecl.contains("=")) {
							successors = new TreeMap<Condition, Integer>();
							String[] sucDeclarations = successorsDecl.split("\\s");
							for (String successorDeclaration : sucDeclarations) {
								int sucRelativeIndex = Integer.parseInt(successorDeclaration.substring(successorDeclaration.indexOf(">") + 1).trim());
								Condition sucCondition = Condition.parse(successorDeclaration.substring(0, successorDeclaration.indexOf("=")).trim());
								successors.put(sucCondition, sucRelativeIndex);
							}
						}
						/* Extract WINDOW */
						String[] windowPlaceholders = null;
						if (base.psl.structure.Range.isRangeDeclaration(token)) {
							windowPlaceholders = base.psl.structure.Range.parseRangeDeclaration(token);
						}
						builder.buildNode(relativeNodeIndex, depVarIndex, depVarRange, successors, windowPlaceholders);
					}
				}

			}
		}
	}

	private Range parseRange(String line) throws Exception {

		return BracketType.ANGULAR.parse(line).range; /* allowed to be null */
	}

	private NameAndRange parseNameAndRange(String nameAndRange) throws Exception {

		return BracketType.ROUND.parse(nameAndRange);
	}

	private enum BracketType {
		ROUND("(", " DOWNTO ", ")"), ANGULAR("<", ":", ">");

		private final String open;
		private final String close;
		private final String delim;

		BracketType(String open, String delim, String close) {
			this.open = open;
			this.close = close;
			this.delim = delim;
		}

		public boolean isPresentIn(String nameAndRange) {
			return nameAndRange.contains(open) && nameAndRange.contains(close);
		}

		public NameAndRange parse(String nameAndRange) throws Exception {
			if (isPresentIn(nameAndRange)) {
				int openIndex = nameAndRange.lastIndexOf(open);
				int closeIndex = nameAndRange.lastIndexOf(close);
				String name = nameAndRange.substring(0, openIndex);
				String line = nameAndRange.substring(openIndex + 1, closeIndex);
				Range range;

				if (line.contains(delim)) {
					try {
						int highestIndex = Integer.parseInt(line.substring(0, line.indexOf(delim)).trim());
						int lowestIndex = Integer.parseInt(line.substring(line.indexOf(delim) + delim.length()).trim());
						range = new Range(highestIndex, lowestIndex);
					} catch (NumberFormatException e) {
						throw new Exception("Could not parse Integers in the following range: " + line);
					}

				} else {
					try {
						int theOnlyIndex;
						theOnlyIndex = Integer.parseInt(line.trim());
						range = new Range(theOnlyIndex, theOnlyIndex);
					} catch (NumberFormatException e) {
						return new NameAndRange(nameAndRange, null);
//						throw new Exception("Could not parse Integers in the following range: " + line);
					}
				}

				return new NameAndRange(name, range);

			} else {
				return new NameAndRange(nameAndRange, null);
			}
		}

	}

	static class NameAndRange {
		String name;
		Range range;

		NameAndRange(String name, Range range) {
			this.name = name;
			this.range = range;
		}
	}

}
