package ui.io;

import io.QuietCloser;
import ui.base.NodeItem;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 18.12.2008
 * <br>Time: 2:00:27
 */
public class CoverageReader {
    private static final String NODE_START_TEXT = ".NODE_COVERAGE";
    private static final String EDGE_START_TEXT = ".EDGE_COVERAGE";
    private static final String DEFAULT_COMMENT = ";";
    
    private final File covFile;
    private Collection<NodeItem> uncoveredNodeItems;

    public CoverageReader(File covFile) {
        this.covFile = covFile;
    }

    public void read() throws IOException {
        uncoveredNodeItems = new HashSet<NodeItem>();
        BufferedReader reader = null;
        boolean readingNodes = true;
        try {
            reader = new BufferedReader(new FileReader(covFile));
            String line;
            while ((line = reader.readLine().trim()) != null && readingNodes) {
                /* Skip comments and empty lines */
                if (line.startsWith(DEFAULT_COMMENT) || line.length() == 0) continue;

                if (readingNodes && line.startsWith(NODE_START_TEXT)) {
                    readingNodes = false;
                    while ((line = reader.readLine().trim()) != null && !line.startsWith(EDGE_START_TEXT)) {
                        /* Skip comments and empty lines */
                        if (line.startsWith(DEFAULT_COMMENT) || line.length() == 0) continue;
                        
                        /* Parse indices */
                        String[] indicesAsStrings = line.split("\\s");
                        if (indicesAsStrings.length != 2) {
                            throw new IOException("Coverage file is malformed");
                        }
                        int graphIndex = Integer.parseInt(indicesAsStrings[0].trim());
                        int nodeIndex = Integer.parseInt(indicesAsStrings[1].trim());
                        uncoveredNodeItems.add(new NodeItem(graphIndex, nodeIndex));
                    }
                }

            }

        } finally {
            QuietCloser.closeQuietly(reader);
        }
    }

    public Collection<NodeItem> getUncoveredNodeItems() {
        if (uncoveredNodeItems == null) {
            try {
                read();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return uncoveredNodeItems;
    }
}
