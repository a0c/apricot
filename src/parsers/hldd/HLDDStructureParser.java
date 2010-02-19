package parsers.hldd;

import io.scan.HLDDScanner;
import base.psl.structure.Range;
import base.hldd.structure.Flags;
import base.Indices;

import java.math.BigInteger;
import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 20.02.2008
 * <br>Time: 18:38:11
 */
public class HLDDStructureParser {
    private final HLDDScanner scanner;
    private final HLDDStructureBuilder builder;

    public HLDDStructureParser(HLDDScanner scanner, HLDDStructureBuilder structureBuilder) {
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
                Flags flags = Flags.parse(token.substring(token.indexOf("(") + 1, token.indexOf(")")).trim());  // collectFlags(token.substring(token.indexOf("(") + 1, token.indexOf(")")).trim());
                /* Extract NAME */
                String name = token.substring(token.indexOf("\"") + 1, token.lastIndexOf("\"")).trim();
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
                    builder.buildFunction(index, name, functionType, inputIndices, inputPartedIndices, length);

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
                    builder.buildGraph(index, flags, name, length, graphLength, graphIndex);
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
                        String successorsDecl = token.substring(token.indexOf("(", token.indexOf(")")) + 1, token.lastIndexOf(")")).trim();
                        int[] successors;
                        if (successorsDecl.contains("=")) {
                            String[] succDeclarations = successorsDecl.split("\\s");
							List<Integer> sucList = new LinkedList<Integer>();
                            for (String successorDeclaration : succDeclarations) {
								int condition;
								int succRelativeIndex = Integer.parseInt(successorDeclaration.substring(successorDeclaration.indexOf(">") + 1).trim());
								String[] condStrings = successorDeclaration.substring(0, successorDeclaration.indexOf("=")).trim().split("-"); // e.g. "1-2" or "3"
								if (condStrings.length == 1) { // " 1 => 3 "
									condition = Integer.parseInt(condStrings[0]);
									sucList.add(condition, succRelativeIndex);
								} else if (condStrings.length == 2) { // " 1-2 => 3 "
									int end = Integer.parseInt(condStrings[1].trim());
									for (condition = Integer.parseInt(condStrings[0].trim()); condition <= end; condition++) {
										sucList.add(condition, succRelativeIndex);
									}
								} else { // ???
									throw new Exception("HLDDStructureParser: cannot parse defective condition " + successorDeclaration.substring(0, successorDeclaration.indexOf("=")).trim());
								}
							}
							successors = new int[sucList.size()];  // successors = new int[succDeclarations.length]; //todo: (int) Math.pow(2, depVarHsb + 1)
							for (int condition = 0; condition < successors.length; condition++) {
								successors[condition] = sucList.get(condition);
							}
						} else {
                            successors = null;
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

        if (line.contains("<") && line.contains(">")) {
            int openIndex = line.lastIndexOf("<");
            int closeIndex = line.lastIndexOf(">");
            line = line.substring(openIndex + 1, closeIndex);

            if (line.contains(":")) {
                try {
                    int highestIndex = Integer.parseInt(line.substring(0, line.indexOf(":")).trim());
                    int lowestIndex = Integer.parseInt(line.substring(line.indexOf(":") + 1).trim());
                    return new Indices(highestIndex, lowestIndex);
                } catch (NumberFormatException e) {
                    throw new Exception("Could not parse the following indices to Integer: " + line);
                }

            } else {
                try {
                    int theOnlyIndex;
                    theOnlyIndex = Integer.parseInt(line.trim());
                    return new Indices(theOnlyIndex, theOnlyIndex);
                } catch (NumberFormatException e) {
                    throw new Exception("Could not parse the following indices to Integer: " + line);
                }
            }

        } else return null;

    }

    private String collectFlags(String flagsString) {
        StringBuilder strBldr = new StringBuilder();
        for (char flagChar : flagsString.toCharArray()) {
            if (flagChar != '_') strBldr.append(Character.toUpperCase(flagChar));
        }
        return strBldr.toString();
    }
}
