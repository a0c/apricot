package parsers.hldd;

import base.hldd.structure.nodes.utils.Condition;
import io.scan.HLDDScanner;
import base.psl.structure.Range;
import base.hldd.structure.Flags;
import base.Indices;

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
				/* Extract NAME and PARTED INDICES */
				NameAndPartedIndicesHolder holder = parseNameAndIndices(token.substring(token.indexOf("\"") + 1, token.lastIndexOf("\"")).trim());
				String name = holder.name;
				Indices partedIndices = holder.partedIndices;
				/* Extract HIGHEST SIGNIFICANT BIT and build LENGTH */
				int highestSB = Integer.parseInt(token.substring(token.indexOf("<") + 1, token.lastIndexOf(":")).trim());
				Indices length = new Indices(highestSB, 0);
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
					Indices[] inputPartedIndices = new Indices[inputDeclarations.length];
					for (int i = 0; i < inputDeclarations.length; i++) {
						String inputDeclaration = inputDeclarations[i].trim();
						/* Extract input INDEX and PARTED INDICES, if any */
						int inputIndex = Integer.parseInt(inputDeclaration.substring(inputDeclaration.indexOf("=") + 1, inputDeclaration.lastIndexOf("<")).trim());
						inputIndices[i] = inputIndex;
						inputPartedIndices[i] = parsePartedIndices(inputDeclaration.substring(inputDeclaration.indexOf("=")));
					}
					int nameIdx = Integer.parseInt(name.substring(name.lastIndexOf("_") + 1));
					builder.buildFunction(index, nameIdx, functionType, inputIndices, inputPartedIndices, length);

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
					builder.buildGraph(index, flags, name, partedIndices, length, graphLength, graphIndex);
					/* Read NODES */
					for (int nodeIndex = 0; nodeIndex < graphLength; nodeIndex++) {
						token = scanner.next();
						/* Extract RELATIVE NODE INDEX */
						int relativeNodeIndex = Integer.parseInt(token.substring(0, token.indexOf(":")).trim().split("\\s")[1].trim());
						/* Extract DEPENDENT VARIABLE INDEX */
						int depVarIndex = Integer.parseInt(token.substring(token.lastIndexOf("=") + 1, token.indexOf("\"")).trim());
						/* Extract PARTED INDICES if any */
						Indices depVarPartedIndices = parsePartedIndices(token.substring(token.indexOf("\"") + 1, token.lastIndexOf("\"")).trim());
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
						if (Range.isRangeDeclaration(token)) {
							windowPlaceholders = Range.parseRangeDeclaration(token);
						}
						builder.buildNode(relativeNodeIndex, depVarIndex, depVarPartedIndices, successors, windowPlaceholders);
					}
				}

			}
		}
	}

	private Indices parsePartedIndices(String line) throws Exception {

		return BracketType.ANGULAR.parse(line).partedIndices; /* allowed to be null */
	}

	private NameAndPartedIndicesHolder parseNameAndIndices(String nameAndIndices) throws Exception {

		return BracketType.ROUND.parse(nameAndIndices);
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

		public boolean isPresentIn(String nameAndIndices) {
			return nameAndIndices.contains(open) && nameAndIndices.contains(close);
		}

		public NameAndPartedIndicesHolder parse(String nameAndIndices) throws Exception {
			if (isPresentIn(nameAndIndices)) {
				int openIndex = nameAndIndices.lastIndexOf(open);
				int closeIndex = nameAndIndices.lastIndexOf(close);
				String name = nameAndIndices.substring(0, openIndex);
				String line = nameAndIndices.substring(openIndex + 1, closeIndex);
				Indices partedIndices;

				if (line.contains(delim)) {
					try {
						int highestIndex = Integer.parseInt(line.substring(0, line.indexOf(delim)).trim());
						int lowestIndex = Integer.parseInt(line.substring(line.indexOf(delim) + delim.length()).trim());
						partedIndices = new Indices(highestIndex, lowestIndex);
					} catch (NumberFormatException e) {
						throw new Exception("Could not parse the following indices to Integer: " + line);
					}

				} else {
					try {
						int theOnlyIndex;
						theOnlyIndex = Integer.parseInt(line.trim());
						partedIndices = new Indices(theOnlyIndex, theOnlyIndex);
					} catch (NumberFormatException e) {
						throw new Exception("Could not parse the following indices to Integer: " + line);
					}
				}

				return new NameAndPartedIndicesHolder(name, partedIndices);

			} else {
				return new NameAndPartedIndicesHolder(nameAndIndices, null);
			}
		}

	}
}
