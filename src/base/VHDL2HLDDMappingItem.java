package base;

import java.util.Set;
import java.util.TreeSet;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 10.04.2009
 * <br>Time: 11:14:15
 */
public class VHDL2HLDDMappingItem implements Comparable<VHDL2HLDDMappingItem> {
    private final int lineNumber;
    private final String line;

    public VHDL2HLDDMappingItem(int lineNumber, String line) {
        this.lineNumber = lineNumber;
        this.line = line;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getLine() {
        return line;
    }

    public String toString() {
        return lineNumber + ": " + line;
    }

    public static Set<Integer> collectLineNumbers(Set<VHDL2HLDDMappingItem> mappingItems) {
        Set<Integer> linesSet = new TreeSet<Integer>();
        for (VHDL2HLDDMappingItem mappingItem : mappingItems) {
            linesSet.add(mappingItem.lineNumber);
        }
        return linesSet;
    }

    public int compareTo(VHDL2HLDDMappingItem o) {
        return  new Integer(this.lineNumber).compareTo(o.lineNumber);
    }
}
