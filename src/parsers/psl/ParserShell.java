package parsers.psl;

import base.hldd.structure.models.BehModel;
import base.hldd.structure.models.utils.ModelManager;
import base.hldd.structure.models.utils.TGMModelCreatorImpl;
import base.hldd.structure.variables.AbstractVariable;
import base.hldd.structure.variables.GraphVariable;
import base.hldd.structure.variables.Variable;
import base.psl.structure.PPGLibrary;
import base.psl.structure.Property;
import io.ConsoleWriter;
import io.PPGLibraryReader;
import io.scan.PSLScanner;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * @author Anton Chepurov
 */
public class ParserShell {

	/* PPG Library file, PSL file and HLDD Base model file */
	private final File ppgLibraryFile;
	private final File pslFile;
	private final File baseModelFile;
	private final ConsoleWriter consoleWriter;
	/* PPG Library */
	PPGLibrary library;

	/* Collector for PROPERTIES */
	private Property[] newProperties;
	private base.hldd.structure.models.utils.ModelManager hlddModelManager;


	/* Final model */
	private BehModel model = null;
	private String comment = null;

	public ParserShell(File ppgLibraryFile, File pslFile, File baseModelFile, ConsoleWriter consoleWriter) throws FileNotFoundException {
		this.ppgLibraryFile = ppgLibraryFile;
		this.pslFile = pslFile;
		this.baseModelFile = baseModelFile;
		this.consoleWriter = consoleWriter;
	}

	public void parse() throws Exception {
		long startTime = System.currentTimeMillis();

		/* Read PPG Library */
		readPPGLibrary();

		/* Read (parse) PSL file */
		readPSLFile();

		/* Read base HLDD model */
		readBaseHLDDModel();

		/* Construct Graphs for properties */
		constructPropertyGraphs();

		consoleWriter.writeLn("Conversion finished in " + (System.currentTimeMillis() - startTime) + " ms.");
	}

	private void readPPGLibrary() throws Exception {
		consoleWriter.write("(1/4) Reading PPG Library...");
		library = new PPGLibraryReader(ppgLibraryFile).getPpgLibrary();
		consoleWriter.done();
	}

	private void readPSLFile() throws Exception {
		consoleWriter.write("(2/4) Parsing PSL structure...");
		StructureBuilder structureBuilder = new StructureBuilder(library);
		new StructureParser(new PSLScanner(pslFile), structureBuilder).parse();
		newProperties = structureBuilder.getProperties();
		consoleWriter.done();
	}

	private void readBaseHLDDModel() throws Exception {
		consoleWriter.write("(3/4) Parsing Base HLDD model structure...");

		/* Read base HLDD model */
		BehModel hlddModel = BehModel.parseHlddStructure(baseModelFile);

		/* Fill HLDD Model Manager with HLDD model variables */
		hlddModelManager = new ModelManager();
		for (AbstractVariable variable : hlddModel.getVariables()) {
			if (variable instanceof Variable || variable instanceof GraphVariable) {
				hlddModelManager.addVariable(variable);
			}
		}

		consoleWriter.done();
	}

	private void constructPropertyGraphs() throws Exception {
		consoleWriter.write("(4/4) Constructing THLDDs for properties...");
		long startTime = System.currentTimeMillis();
		TGMModelCreatorImpl modelCreator = new TGMModelCreatorImpl(newProperties, hlddModelManager, consoleWriter);
		model = modelCreator.getModel();
		consoleWriter.done(System.currentTimeMillis() - startTime);
		comment = modelCreator.getComment();
	}

	public BehModel getModel() {
		if (model == null) {
			try {
				parse();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return model;
	}

	public String getComment() {
		if (comment == null) {
			try {
				parse();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return comment;
	}
}
