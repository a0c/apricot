package io.scan;

import io.scan.VHDLToken;
import io.scan.VHDLScanner;

import java.io.File;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 30.01.2008
 * <br>Time: 19:48:37
 */
public class TestVhdlScanner {

    public static void main(String[] args) throws Exception {

//        scanner.VhdlScanner scanner = new scanner.VhdlScanner(new File("D:\\Documents and Settings\\Randy\\My Documents\\Anton\\TTU\\RAIK\\VERTIGO docs\\Designs\\antonile\\ITC99\\b10\\syn\\b10.vhd"));
//        scanner.VhdlScanner scanner = new scanner.VhdlScanner(new File("D:\\plot\\VHDL\\Designs\\VHDL\\RTL\\diffeq.rtl.vhdl"));
//        scanner.VhdlScanner scanner = new scanner.VhdlScanner(new File("D:\\Documents and Settings\\Randy\\My Documents\\Anton\\TTU\\RAIK\\VERTIGO docs\\Designs\\VHDL\\RTL Beh\\b09.vhd"));
//       scanner.VhdlScanner scanner = new scanner.VhdlScanner(new File("D:\\Documents and Settings\\Randy\\My Documents\\Anton\\TTU\\RAIK\\VERTIGO docs\\Designs\\VHDL\\RTL Beh\\b04.vhd"));

        String[] files = {"D:\\Documents and Settings\\Randy\\My Documents\\Anton\\TTU\\RAIK\\VERTIGO docs\\Designs\\VHDL\\Beh\\b00.vhd",
                "D:\\Documents and Settings\\Randy\\My Documents\\Anton\\TTU\\RAIK\\VERTIGO docs\\Designs\\VHDL\\Beh\\b04.vhd",
                "D:\\Documents and Settings\\Randy\\My Documents\\Anton\\TTU\\RAIK\\VERTIGO docs\\Designs\\VHDL\\Beh\\b09.vhd",
                "D:\\Documents and Settings\\Randy\\My Documents\\Anton\\TTU\\RAIK\\VERTIGO docs\\Designs\\VHDL\\Beh\\b10.vhd",
                "D:\\Documents and Settings\\Randy\\My Documents\\Anton\\TTU\\RAIK\\VERTIGO docs\\Designs\\VHDL\\Beh DD\\b04_hldd_behav_tree_OPTIMIZED.vhd",
                "D:\\Documents and Settings\\Randy\\My Documents\\Anton\\TTU\\RAIK\\VERTIGO docs\\Designs\\VHDL\\Beh DD\\b00_hldd_behav.vhd",
                "D:\\Documents and Settings\\Randy\\My Documents\\Anton\\TTU\\RAIK\\VERTIGO docs\\Designs\\VHDL\\Beh DD\\b00z_behav_HLDD_ORIGINAL.vhd",
                "D:\\Documents and Settings\\Randy\\My Documents\\Anton\\TTU\\RAIK\\VERTIGO docs\\Designs\\VHDL\\Beh DD\\b10_behav_HLDD.vhd",
                "C:\\Documents and Settings\\Randy\\Desktop\\ITC\\ITC_ORIG\\b08.vhd"
        };
        for (int i = 0; i < 9; i++) {
            VHDLScanner scanner;
            scanner = new VHDLScanner(new File(files[i]));
//            if (i < 10) {
//                scanner = new VhdlScanner(new File("D:\\plot\\VHDL\\Designs\\_Temp\\VhdlScanner\\b0" + i + ".vhd"));
//            } else {
//                scanner = new VhdlScanner(new File("D:\\plot\\VHDL\\Designs\\_Temp\\VhdlScanner\\b" + i + ".vhd"));
//            }
            VHDLToken token;
            while ((token = scanner.next()) != null) {
                System.out.println("|" + token.getValue() + "|  " + token.getType());
//                if (token.getType() == Token.Type.UNKNOWN) {
//                    System.out.println("File " + i + ":");
//                    System.out.println("|" + token.getValue() + "|  " + token.getType());
//                    System.out.println("-----------------------------------------------------------------------------------------------------------");
//                }
            }

        }


//        VhdlScanner scanner = new VhdlScanner(new File("D:\\Documents and Settings\\Randy\\My Documents\\Anton\\TTU\\RAIK\\VERTIGO docs\\Designs\\antonile\\ITC99\\b10\\syn\\b10.vhd"));
//
//        int i = 0;
//        while (scanner.hasNext()) {
//            System.out.println(++i + ":" + scanner.next() + ".");
//        }
    }
}
