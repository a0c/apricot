package ui.io;

import ui.base.AbstractCoverage;
import ui.base.SplittedCoverage;

import java.util.Scanner;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 29.06.2008
 * <br>Time: 23:07:14
 */
public class CoverageParser {
    private final String consoleOutput;
    private AbstractCoverage nodeCoverage = null;
    private AbstractCoverage edgeCoverage = null;
    private AbstractCoverage toggleCoverage = null;

    private static final String NODES_START = "Node Coverage =";
    private static final String EDGES_START = "Edge Coverage =";
    private static final String TOTAL_START = "Toggle Coverage =";

    private boolean checkNodes = true;
    private boolean checkEdges = true;

    public CoverageParser(String consoleOutput) {
        this.consoleOutput = consoleOutput;
    }

    private void parse() {
        Scanner scanner = new Scanner(consoleOutput);
        String line;
        try {
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();

                if (checkNodes && line.startsWith(NODES_START)) {
                    SplittedCoverageHolder covHolder = new SplittedCoverageHolder(line);
                    nodeCoverage = new SplittedCoverage(covHolder.getCovered(), covHolder.getTotal(), "Node coverage");
                    checkNodes = false;
                } else

                if (checkEdges && line.startsWith(EDGES_START)) {
                    SplittedCoverageHolder covHolder = new SplittedCoverageHolder(line);
                    edgeCoverage = new SplittedCoverage(covHolder.getCovered(), covHolder.getTotal(), "Edge coverage");
                    checkEdges = false;
                } else

                if (line.startsWith(TOTAL_START)) {
                    SplittedCoverageHolder covHolder = new SplittedCoverageHolder(line);
                    toggleCoverage = new SplittedCoverage(covHolder.getCovered(), covHolder.getTotal(), "Toggle coverage");
                    break;
                }
            }
        } finally {
            scanner.close();
        }
    }

    public AbstractCoverage getNodeCoverage() {
        if (nodeCoverage == null) {
            parse();
        }
        return nodeCoverage;
    }

    public AbstractCoverage getEdgeCoverage() {
        if (edgeCoverage == null) {
            parse();
        }
        return edgeCoverage;
    }

    public AbstractCoverage getToggleCoverage() {
        if (toggleCoverage == null) {
            parse();
        }
        return toggleCoverage;
    }

    private class SplittedCoverageHolder {
        private static final int NON_INIT_VALUE = -1;

        private final String line;
        private int covered = NON_INIT_VALUE;
        private int total = NON_INIT_VALUE;

        private SplittedCoverageHolder(String line) {
            this.line = line;
        }

        private void parse() {
            String[] words = line.substring(line.indexOf("(") + 1, line.indexOf(")")).split("/");
            covered = Integer.parseInt(words[0].trim());
            total = Integer.parseInt(words[1].trim());
        }

        public int getCovered() {
            if (isNonInitialized(covered)) {
                parse();
            }
            return covered;
        }

        public int getTotal() {
            if (isNonInitialized(total)) {
                parse();
            }
            return total;
        }

        private boolean isNonInitialized(int value) {
            return value == NON_INIT_VALUE;
        }
    }

}
