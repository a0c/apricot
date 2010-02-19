package parsers.hldd;

import io.scan.HLDDScanner;

import java.io.File;

import base.hldd.structure.models.BehModel;
import base.hldd.structure.variables.utils.DefaultGraphVariableCreator;
import parsers.hldd.HLDDStructureBuilder;
import parsers.hldd.HLDDStructureParser;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 20.02.2008
 * <br>Time: 19:07:11
 */
public class TestHLDDStructureParser {

    public static void main(String[] args) throws Exception {


        File directory = new File("D:\\Documents and Settings\\Randy\\My Documents\\Anton\\TTU\\RAIK\\3levels");
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            for (File file : files) {
                if (file.isFile()) {
                    if (file.getName().endsWith("agm")) {
                        testFile(file, new File(file.getAbsolutePath().replace(".agm", "_COPY.agm")));
                        System.in.read();
                    }
                }
            }

        }

    }

    private static void testFile(File sourceFile, File destFile) throws Exception {

//        File hlddFile = new File("C:\\Documents and Settings\\Randy\\Desktop\\ITC\\ITC_ORIG\\b04_SCANNER_2_4MegaMIN.agm");
//        File hlddFile = new File("C:\\Documents and Settings\\Randy\\Desktop\\ITC\\ITC_ORIG\\b02_SCANNER_2_4MegaMIN.agm");
        System.out.print(sourceFile + " in progress...");

        HLDDScanner scanner = new HLDDScanner(sourceFile);

        HLDDStructureBuilder structureBuilder = new HLDDStructureBuilder(new DefaultGraphVariableCreator());

        HLDDStructureParser parser = new HLDDStructureParser(scanner, structureBuilder);
        parser.parse();

        BehModel behModel = structureBuilder.getModel();

        behModel.toFile(destFile, "");

        System.out.println("  Done!");
    }
}
