package io.scan;

import java.io.File;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 20.02.2008
 * <br>Time: 18:48:34
 */
public class TestHlddScanner {

    public static void main(String[] args) throws Exception {

        File hlddFile = new File("C:\\Documents and Settings\\Randy\\Desktop\\ITC\\ITC_ORIG\\b04_SCANNER_2_4MegaMIN.agm");
        HLDDScanner scanner = new HLDDScanner(hlddFile);

        String token;
        while ((token = scanner.next()) != null) {
            System.out.println(token);
        }

        System.out.println("STOP.");
        
    }
}
