package base;

import base.vhdl.structure.nodes.TransitionNode;
import base.vhdl.structure.nodes.IfNode;
import base.vhdl.structure.nodes.AbstractNode;
import java.util.*;

import io.scan.LexemeComposer;

/**
 * Generally, the class maps (final) HLDD model structures to (initial) VHDL lines the former were generated from.
 * The mapping is performed through a set of interconnected HashMaps that, as a whole, reflect the process
 * of creating HLDD model structures from VHDL files.  
 *
 * <br><br>User: Anton Chepurov
 * <br>Date: 26.03.2009
 * <br>Time: 12:36:48
 */
public class VHDL2HLDDMapping {
    private static final char NEW_LINE_UNIX = '\n';
    private static final char NEW_LINE_MAC = '\r';
    private static final char END_OF_FILE = 65535;

    /* Singleton instance */
    private static final VHDL2HLDDMapping instance = new VHDL2HLDDMapping();
    private int currentLineCount;

    /* Fields for the line currently under processing */
    private char lastChar;
    private StringBuilder line;


    /**
     * Currently processed VHDL lines.
     * Not a single line, but a list of lines, since a single
     * {@link io.scan.VHDLToken} can span across multiple lines.
     */
    private List<VHDL2HLDDMappingItem> currentLines = new LinkedList<VHDL2HLDDMappingItem>();
    /**
     * Map of {@link base.vhdl.structure.nodes.AbstractNode AbstractNodes} to source VHDL line numbers.
     */
    private Map<? super AbstractNode, Set<Integer>> linesByNode = new HashMap<AbstractNode, Set<Integer>>();

    public static VHDL2HLDDMapping getInstance() {
        return instance;
    }

    private VHDL2HLDDMapping() {
        clear();
    }

    


    public void clear() {
        currentLineCount = 1;
        lastChar = 0;
        line = new StringBuilder();
        //todo...
    }

    public int getCurrentLineCount() {
        return currentLineCount;
    }

    public Set<VHDL2HLDDMappingItem> getCurrentLines() {
        /* Add any non-empty lines being currently under processing */
        addLineToCurrentLines();
        return new TreeSet<VHDL2HLDDMappingItem>(currentLines);
    }

    public void purgeCurrentLines() {
        //todo...
        
        currentLines = new LinkedList<VHDL2HLDDMappingItem>();
        line = new StringBuilder();
    }

    public void setLinesForNode(AbstractNode node, Set<VHDL2HLDDMappingItem> linesMappingItems) {
//        String nodeAsString = node.toString() + ">>> ";
//        for (VHDL2HLDDMappingItem item : linesMappingItems) {
//            System.out.println(nodeAsString + item.getLineNumber());
//        }
        Set<Integer> vhdlLines = VHDL2HLDDMappingItem.collectLineNumbers(linesMappingItems);
        linesByNode.put(node, vhdlLines);
    }

    public Set<Integer> getLinesForNode(AbstractNode node) {
        /* Create a real copy of set, not a shallow one.
        * The elements of the set don't need to be unique, for these are their values that matter. */
        return new TreeSet<Integer>(linesByNode.get(node));
    }

    public void append(char newChar) {
        /* Count lines */
        if (isNewLine(newChar)) {
            addLineToCurrentLines();
            currentLineCount++;
        }
        /* Append the character */
        if (newChar != END_OF_FILE) {
            line.append(newChar);
        }
        /* Save last character */
        lastChar = newChar;
    }

    private void addLineToCurrentLines() {
        String trimmedLine = line.toString().trim();
        if (trimmedLine.length() > 0) {
            /* Add all lines but comments to currentLines */
            if (!trimmedLine.startsWith(LexemeComposer.DEFAULT_COMMENT)) {
                /* Add to currentLines */
                currentLines.add(new VHDL2HLDDMappingItem(currentLineCount, trimmedLine));
            }
            /* Init new empty line */
            line = new StringBuilder();
        }
    }

    /**
     * New lines: {@link #NEW_LINE_MAC}, {@link #NEW_LINE_UNIX} or {@link #NEW_LINE_MAC} + {@link #NEW_LINE_UNIX}.<br>
     * Thus, new line is detected if either<br>
     * 1) <code>newChar</code> is {@link #NEW_LINE_MAC} or<br>
     * 2) <code>newChar</code> is {@link #NEW_LINE_UNIX} and the lastChar is not {@link #NEW_LINE_MAC}.<br><br>
     * The last condition allows avoiding counting the new line twice for {@link #NEW_LINE_MAC} + {@link #NEW_LINE_UNIX}.
     * @param newChar newly appended character
     * @return <code>true</code> if the <code>newChar</code> denotes a new line
     */
    private boolean isNewLine(char newChar) {
        return newChar == NEW_LINE_MAC || (newChar == NEW_LINE_UNIX && lastChar != NEW_LINE_MAC);
    }

}
