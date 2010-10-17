import parsers.psl.ParserShell;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author Anton Chepurov
 */
public class PSLMain {

	public static void main(String[] args) throws Exception {

		//todo: catch all exceptions...
//        pslParserShell pslParserShell = new pslParserShell("D:\\Documents and Settings\\Randy\\My Documents\\Anton\\TTU\\RAIK\\VERTIGO docs\\Designs\\antonile\\TLM_MADNESS\\lpc_and_vci__rtl.psl");
		ParserShell pslParserShell = new ParserShell(
				new File("D:\\Documents and Settings\\Randy\\My Documents\\Anton\\TTU\\RAIK\\VERTIGO docs\\PSL\\ppg.lib"),
				new File("D:\\Documents and Settings\\Randy\\My Documents\\Anton\\TTU\\RAIK\\VERTIGO docs\\PSL\\latw_dummy_ETS08.psl"),
				new File("D:\\Documents and Settings\\Randy\\My Documents\\Anton\\TTU\\RAIK\\VERTIGO docs\\PSL\\latw_dummy_ETS08.agm"),
				null);
		pslParserShell.parse();

		pslParserShell.getModel().toFile(new FileOutputStream(new File("D:\\Documents and Settings\\Randy\\My Documents\\Anton\\TTU\\RAIK\\VERTIGO docs\\PSL\\latw_dummy_ETS08.agm")), pslParserShell.getComment());

	}

}
