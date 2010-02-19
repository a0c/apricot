package parsers.tgm;

import io.scan.HLDDScanner;

import java.util.List;
import java.util.LinkedList;
import java.util.Collection;
import java.util.HashSet;
import java.io.File;

import ui.BusinessLogic;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 26.01.2009
 * <br>Time: 19:06:45
 */
public class ModelDataLoader {
    private final File patternFile;
    private HLDDScanner scanner = null;
    private FileType sourceFileType = null;
    private List<String> variableNames = null;
    private int graphCount = -1;
    private Collection<Integer> graphIndices = null;
    private Collection<Integer> inputIndices = null;

    public ModelDataLoader(File patternFile) {
        this.patternFile = patternFile;
        File modelFile = null;
        try {
            sourceFileType = FileType.parseFileType(patternFile);
            modelFile = sourceFileType.deriveModelFile(patternFile);
            if (modelFile == null || !modelFile.exists()) {
                scanner = null;
            } else {
                scanner = new HLDDScanner(modelFile);
            }
        } catch (Exception e) {
            if (modelFile != null) {
                throw new RuntimeException("Unexpected bug: cannot create " + HLDDScanner.class.getSimpleName() +
                        " from modelFile " + modelFile.getAbsolutePath());
            }
        }
    }

    public void collect() {
        if (isModelFileMissing()) {
            variableNames = null;
            return;
        }
        variableNames = new LinkedList<String>();
        graphIndices = new HashSet<Integer>();
        inputIndices = new HashSet<Integer>();
        try {
            String token;
            int varIndex = -1;
            while ((token = scanner.next()) != null) {
                if (token.startsWith("VAR#") && isValidVariable(++varIndex)) {
                    String varName = token.substring(token.indexOf("\"") + 1, token.lastIndexOf("\"")).trim();
                    if (varName.contains("@")) {
                        varName = varName.substring(0, varName.indexOf("@"));
                    }
                    variableNames.add(varName);
                } else if (token.startsWith("STAT#")) {
                    int inputCount = 0, constCount = 0, funcCount = 0;
                    /* Parse statistics */
                    String[] stats = token.split(",");
                    for (String stat : stats) {
                        if (stat.contains("GRPS")) {
                            graphCount = Integer.parseInt(stat.trim().split("\\s")[0].trim());
                        } else if (stat.contains("INPS")) {
                            inputCount = Integer.parseInt(stat.trim().split("\\s")[0].trim());
                        } else if (stat.contains("CONS")) {
                            constCount = Integer.parseInt(stat.trim().split("\\s")[0].trim());
                        } else if (stat.contains("FUNS")) {
                            funcCount = Integer.parseInt(stat.trim().split("\\s")[0].trim());
                        }
                    }

                    /* Collect variable indices*/
                    addIndices(0, inputCount, inputIndices);
                    addIndices(inputCount + constCount + funcCount, graphCount, graphIndices);
                }
            }
        } finally {
            scanner.close();
        }

    }

    public boolean isModelFileMissing() {
        return scanner == null;
    }

    public String getMissingFileMessage() {
        return sourceFileType.getMissingFileMessage(patternFile); 
    }

    private boolean isValidVariable(int index) {
        switch (sourceFileType) {
            case CHKfile:
            case SIMfile:
                return graphIndices.contains(index) || inputIndices.contains(index);
            case TSTFile:
                return inputIndices.contains(index);
            default:
                return false;
        }
    }

    private void addIndices(int start, int length, Collection<Integer> destCollection) {
        int end = start + length;
        for (int i = start; i < end; i++) {
            destCollection.add(i);
        }
    }

    public String[] getVariableNames() {
        if (variableNames == null) {
            collect();
        }
        return variableNames == null ? null : variableNames.toArray(new String[variableNames.size()]);
    }

    public int getGraphCount() {
        if (graphCount == -1) {
            collect();
        }
        return graphCount;
    }

    public Collection<Integer> getInputIndices() {
        if (inputIndices == null) {
            collect();
        }
        return inputIndices;
    }

    public Collection<Integer> getGraphIndices() {
        if (graphIndices == null) {
            collect();
        }
        return graphIndices;
    }

    public FileType getSourceFileType() {
        return sourceFileType;
    }

    public enum FileType {
        CHKfile, SIMfile, TSTFile;
        private static final String CHK_EXTENSION = ".chk";
        private static final String SIM_EXTENSION = ".sim";
        private static final String AGM_EXTENSION = ".agm";
        private static final String TGM_EXTENSION = ".tgm";
        private static final String TST_EXTENSION = ".tst";


        public File deriveModelFile(File patternFile) {
            switch (this) {
                case CHKfile:
                    return BusinessLogic.deriveFileFrom(patternFile, CHK_EXTENSION, TGM_EXTENSION);
                case SIMfile:
                    return BusinessLogic.deriveFileFrom(patternFile, SIM_EXTENSION, AGM_EXTENSION);
                case TSTFile:
                    return BusinessLogic.deriveFileFrom(patternFile, TST_EXTENSION, AGM_EXTENSION);
                default:
                    return null;
            }
        }

        public String getMissingFileMessage(File patternFile) {
            switch (this) {
                case CHKfile:
                    return "To map CHK stimuli with signal names, please, place TGM file (" +
                            patternFile.getName().replace(CHK_EXTENSION, TGM_EXTENSION) + ") into the " +
                            "following directory:\n" + patternFile.getParentFile().getAbsolutePath();
                case SIMfile:
                    return "To map SIM stimuli with signal names, please, place AGM file (" +
                            patternFile.getName().replace(SIM_EXTENSION, AGM_EXTENSION) + ") into the " +
                            "following directory:\n" + patternFile.getParentFile().getAbsolutePath();
                case TSTFile:
                    return "To map TST stimuli with signal names, please, place AGM file (" +
                            patternFile.getName().replace(TST_EXTENSION, AGM_EXTENSION) + ") into the " +
                            "following directory:\n" + patternFile.getParentFile().getAbsolutePath();
                default:
                    return "";
            }
        }

        public static FileType parseFileType(File patternFile) {
            String patternFileName = patternFile.getName().toLowerCase();
            if (patternFileName.endsWith(CHK_EXTENSION)) {
                return CHKfile;
            } else if (patternFileName.endsWith(SIM_EXTENSION)) {
                return SIMfile;
            } else if (patternFileName.endsWith(TST_EXTENSION)) {
                return TSTFile;
            } else return null;
        }
    }
}
