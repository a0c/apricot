package base.psl.structure;

import io.PSLBufferedReader;
import base.helpers.RegexFactory;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 01.12.2007
 * <br>Time: 22:16:12
 */
public class Range {

    private int start;

    private int end;

    private TemporalModifier temporalModifier;

    public Range(int[] range, char temporalModifierChar) throws Exception {
        /* Set RANGE */
        initRange(range);

        /* Set TEMPORAL_MODIFIER */
        temporalModifierChar = Character.toLowerCase(temporalModifierChar);
        if (temporalModifierChar == TemporalModifier.ALWAYS.getSuffix() /*|| temporalModifierChar == 'A'*/) {

            temporalModifier = TemporalModifier.ALWAYS;

        } else if (temporalModifierChar == TemporalModifier.EXIST.getSuffix() /*|| temporalModifierChar == 'E'*/) {

            temporalModifier = TemporalModifier.EXIST;

        } else if (temporalModifierChar == ' ') {

            //todo: Temporal Fix: Simulator requires either _A or _E, no empty modifier allowed.
//            temporalModifier = TemporalModifier.NEXT;
            temporalModifier = TemporalModifier.ALWAYS;

        } else throw new Exception("Unknown temporal modifier \'" + temporalModifierChar + "\' used in final_Range");

    }

    public Range(int[] range) throws Exception {
        initRange(range);

        /*      NEXT        NEXT[5]     */
        //todo: Temporal Fix: Simulator requires either _A or _E, no empty modifier allowed.
//        temporalModifier = TemporalModifier.NEXT;
        temporalModifier = TemporalModifier.ALWAYS;
    }

    private void initRange(int[] range) throws Exception {

        if (range.length == 1) {

            start = end = range[0];

        } else if (range.length == 2) {

            start = range[0];
            end = range[1];

        } else throw new Exception("Invalid range size: " + range.length + "\nValid size is 1 or 2");

    }




    public String toString() {
        StringBuffer ret = new StringBuffer();

        ret.append("@[");

//        if (start == end) ret.append(start);
//        else ret.append(start).append("..").append(end);
        ret.append(start).append("..").append(end);

        ret.append("]");

        ret.append(temporalModifier.toString());

        return ret.toString();
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public TemporalModifier getTemporalModifier() {
        return temporalModifier;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public void setTemporalModifier(TemporalModifier temporalModifier) {
        this.temporalModifier = temporalModifier;
    }

    public void adjustWindow(Range windowToAdjustTo) {
        start += windowToAdjustTo.getStart();
        end -= windowToAdjustTo.getEnd();
    }

    public static boolean isRangeDeclaration(String expression) {

        /* Check for  '['  and  ']' */
        if (!(expression.contains("[") && expression.contains("]"))) return false;
        String potentialRange = expression.substring(expression.indexOf("[") + 1, expression.indexOf("]")).trim();

        /*  */
        if (!(potentialRange.matches("\\w+\\s+[tT][oO]\\s+\\w+") || potentialRange.matches("\\w+"))) return false;

        /* All checks passed. It's a range declaration */
        return true;
    }

    private static int[] parseRange(String rangeExpression) {
        rangeExpression = rangeExpression.replaceAll("\\[", "").replaceAll("\\]", "");

//        int dotsIndex = rangeExpression.split("\\s[tT][oO]\\s")[0].length();
        String[] rangeNumbers = rangeExpression.split("\\s[tT][oO]\\s");
        int[] range = new int[rangeNumbers.length];
        for (int i = 0; i < rangeNumbers.length; i++) {
            range[i] = Integer.parseInt(rangeNumbers[i].trim());
        }

        return range;
//        if (dotsIndex == -1) {
//            /*      4     */
//            return new int[]{
//                    Integer.parseInt(rangeExpression)
//            };
//        } else {
//            /*      4 to 9   */
//            return new int[]{
//                    Integer.parseInt(rangeExpression.substring(0, dotsIndex).trim()),
//                    Integer.parseInt(rangeExpression.substring(dotsIndex + 4).trim())
//            };
//        }

    }

    /**
     *
     * <br>
     * Currently supported:<br>
     * next, next[i], next_a[i..j], next_e[i..j]
     *
     * @param expression
     * @return
     * @throws Exception
     */
    public static Range parseRangeNEXT(String expression) throws Exception {
        String nextRegex = RegexFactory.createStringRegex("next");

        if (expression.matches(nextRegex + "\\[.*")) { //todo: before was "\\s\\[.*"
            /*      NEXT[i]      */
            return new Range(Range.parseRange(new PSLBufferedReader(expression).readBlock('[', ']', false)));

        } else if (expression.matches(nextRegex + "_.*")) {
            /*      NEXT_       */
            char temporalModifierChar = expression.charAt(expression.indexOf("_") + 1);

            int[] range = Range.parseRange(new PSLBufferedReader(expression).readBlock('[', ']', false));

            return new Range(range, temporalModifierChar);

        } else if (expression.matches(nextRegex + ".*")) {
            /*      NEXT        */
            return new Range(new int[]{1});

        } else return null;

    }

    public static String[] parseRangeDeclaration(String rangeDeclaration) {
        rangeDeclaration = rangeDeclaration.substring(rangeDeclaration.indexOf("[") + 1, rangeDeclaration.indexOf("]"));

        String[] declarations = rangeDeclaration.matches("\\w+\\s+[tT][oO]\\s+\\w+")
                ? rangeDeclaration.split("\\s[tT][oO]\\s")
                : new String[]{rangeDeclaration};
        String[] retDecl = new String[declarations.length];
        for (int i = 0; i < declarations.length; i++) {
            retDecl[i] = declarations[i].trim();
        }

        return retDecl;
    }

    /**
     *
     * @param parentOperandWindow
     * @param operatorWindowPlaceholders
     * @return      real window received after replacing placeholders.<br>
     *              <b>NB!</b> <code>END</code> is denoted with -1.
     * @throws Exception
     */
    public static Range replacePlaceholders(Range parentOperandWindow, String[] operatorWindowPlaceholders) throws Exception {

        if (parentOperandWindow == null) {
            int start;
            try {
                start = Integer.parseInt(operatorWindowPlaceholders[0]);
            } catch (NumberFormatException e) {
                throw new Exception("Error while merging windows:" +
                        "\nOperator with WINDOW_PLACEHOLDER ( "+ operatorWindowPlaceholders[0] + " ) must have its window defined within PSL property.");
            }
            int end;
            try {
                end = Integer.parseInt(operatorWindowPlaceholders[1]);
            } catch (NumberFormatException e) {
                if (operatorWindowPlaceholders[1].equals("END")) {
                    end = -1;
                } else throw new Exception("Error while merging windows:" +
                        "\nOperator with WINDOW_PLACEHOLDER ( "+ operatorWindowPlaceholders[1] + " ) must have its window defined within PSL property.");
            }

            return new Range(new int[]{ start, end });

        } else {
            int start;
            try {
                start = Integer.parseInt(operatorWindowPlaceholders[0]);
            } catch (NumberFormatException e) {
                start = parentOperandWindow.getStart();
            }
            int end;
            try {
                end = Integer.parseInt(operatorWindowPlaceholders[1]);
            } catch (NumberFormatException e) {
                end = operatorWindowPlaceholders[1].equals("END") ? -1 : parentOperandWindow.getEnd();
            }

            return new Range(new int[]{ start, end }, parentOperandWindow.getTemporalModifier().getSuffix());
        }

    }

    public enum TemporalModifier {
        ALWAYS('a'), EXIST('e'), NEXT(' ');

        private final char suffix;

        TemporalModifier(char suffix) {
            this.suffix = suffix;
        }

        public char getSuffix() {
            return suffix;
        }

        public String toString() {
            switch (this) {
                case ALWAYS:
                case EXIST:
                    return "_" + getSuffix();
                default:
                    return "";
            }
        }
    }

}

