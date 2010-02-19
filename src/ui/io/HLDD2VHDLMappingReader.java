package ui.io;

import ui.base.HLDD2VHDLMapping;
import ui.base.NodeItem;
import io.QuietCloser;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 18.12.2008
 * <br>Time: 1:21:59
 */
public class HLDD2VHDLMappingReader {
    private final static int OFFSET = 1;

    private HLDD2VHDLMapping mapping = null;
    private final File mappingFile;

    public HLDD2VHDLMappingReader(File mappingFile) {
        this.mappingFile = mappingFile;
    }

    public void read() throws IOException {
        mapping = new HLDD2VHDLMapping();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(mappingFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] lineParts = line.split(":");
                if (lineParts.length != 2) {
                    throw new IOException("Mapping file is malformed");
                }

                /* Parse line numbers */
                String[] lineNumbersAsStrings = lineParts[1].split(",");
                Collection<Integer> lines = new HashSet<Integer>();
                for (String lineNumberAsString : lineNumbersAsStrings) {
                    lines.add(Integer.parseInt(lineNumberAsString.trim()) - OFFSET);
                }

                /* Parse indices */
                String[] indices = lineParts[0].split("\\s");
                if (indices.length == 2) {
                    int graphIndex = Integer.parseInt(indices[0].trim());
                    int nodeIndex = Integer.parseInt(indices[1].trim());
                    mapping.addMapping(new NodeItem(graphIndex, nodeIndex), lines);
                } //todo: EdgeMappingItem, etc...
            }
        } finally {
            QuietCloser.closeQuietly(reader);
        }
    }

    public HLDD2VHDLMapping getMapping() {
        if (mapping == null) {
            try {
                read();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return mapping;
    }
}
