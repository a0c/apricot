package ui.utils;

import io.ConsoleWriter;

import base.hldd.structure.models.BehModel;
import base.hldd.structure.models.Model;
import base.hldd.structure.models.utils.BehModelCreatorImpl;
import base.hldd.structure.models.utils.ModelManager;
import base.hldd.structure.models.utils.ModelCreator;
import base.vhdl.structure.Entity;
import base.vhdl.visitors.*;

import java.io.*;
import java.util.concurrent.ExecutionException;
import java.util.Date;
import java.util.logging.Logger;
import java.text.DateFormat;

import parsers.Beh2RtlTransformer;
import parsers.psl.ParserShell;

import javax.swing.*;

import ui.*;
import ui.BusinessLogic.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 09.12.2008
 * <br>Time: 20:02:35
 */
public class ConvertingWorker extends SwingWorker<BehModel, Void> {
	private static final Logger LOG = Logger.getLogger(ConvertingWorker.class.getName());

    private static final DateFormat DATE_TIME_FORMAT = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
    private static final String SEPARATOR = "#### ";

    private final ParserID parserId;
    private final ConsoleWriter consoleWriter;
	private String comment = "";

    private final File sourceFile;
    private File pslFile;
    private final File baseModelFile;
	private final HLDDRepresentationType hlddType;
    private final boolean shouldSimplify;
	private final AbstractWorkerFinalizer workerFinalizer;
	private ConverterSettings settings;
	private ConfigurationHandler config;

	public ConvertingWorker(AbstractWorkerFinalizer workerFinalizer, ConsoleWriter consoleWriter, ConverterSettings settings) {
		this.workerFinalizer = workerFinalizer;
		this.settings = settings;
        this.parserId = settings.getParserId();
        this.consoleWriter = consoleWriter;
        this.sourceFile = settings.getSourceFile();
        this.pslFile = settings.getPslFile();
        this.baseModelFile = settings.getBaseModelFile();
		this.hlddType = settings.getHlddType();
        this.shouldSimplify = settings.isDoSimplify();
		/* Load Configuration */
		if (parserId == ParserID.VhdlBeh2HlddBeh || parserId == ParserID.VhdlBehDd2HlddBeh) {
			config = ConfigurationHandler.loadConfiguration(sourceFile);
		}

    }


    protected BehModel doInBackground() throws ExtendedException {
		if (isCancelled()) return null;
		workerFinalizer.doBeforeWorker();

        BehModel model = null;
        Entity entity;
        GraphGenerator graphCreatingVisitor;
        ModelManager modelCollector;
        ModelCreator modelCreator;
		OutputStream mapFileStream = settings.getMapFileStream();
        int current = 1, total;
        long startTime;

        consoleWriter.writeLn(getTimeAsString() + "\tRunning " + parserId.getTitle() + " converter:");

        try {
            switch (parserId) {
                case VhdlBeh2HlddBeh:

                    total = 4;
                    total += hlddType == HLDDRepresentationType.REDUCED ? 1 :
                            hlddType == HLDDRepresentationType.MINIMIZED ? 2 : 0;
                    /* Parse VHDL structure */
                    consoleWriter.write(stat(current++, total) + "Parsing VHDL structure...");
                    entity = Entity.parseVhdlStructure(sourceFile);
                    consoleWriter.done();

                    startTime = System.currentTimeMillis();
                    /* Process received VHDL structure */
                    consoleWriter.write(stat(current++, total) + "Preprocessing VHDL structure...");
                    entity.traverse(new VariableNameReplacerImpl(config)); // todo: varNR.getStateName()
                    DelayFlagCollector delayCollector = new DelayFlagCollector();
                    entity.traverse(delayCollector);
                    entity.traverse(new ClockEventRemover());
                    consoleWriter.done();

                    /* Generate Graphs (GraphVariables) and collect all variables */
                    consoleWriter.write(stat(current++, total) + "Generating HLDDs...");
                    graphCreatingVisitor = new BehGraphGenerator(config, settings, delayCollector.getDFlagNames());
                    entity.traverse(graphCreatingVisitor);
                    modelCollector = graphCreatingVisitor.getModelCollector();
                    consoleWriter.done();

                    /* Create HLDD model */
                    consoleWriter.write(stat(current++, total) + "Creating HLDD model...");
                    modelCreator = new BehModelCreatorImpl(modelCollector.getConstants(), modelCollector.getVariables());
                    model = modelCreator.getModel();
                    consoleWriter.done();

                    /* TRIM */
                    if (hlddType == HLDDRepresentationType.REDUCED) {
                        applyReductionRule1(model, current, total);
                    } else if (hlddType == HLDDRepresentationType.MINIMIZED) {
                        applyReductionRule1(model, current++, total);
                        applyReductionRule2(model, current, total);
                    }
                    startTime = System.currentTimeMillis() - startTime;
                    consoleWriter.writeLn("Conversion finished in " + startTime + " ms.");
                    consoleWriter.writeLn("Created model with " + model.getVarCount() + " Variables, " +
                            model.getGraphCount() + " Graphs and " + model.getNodeCount() + " Nodes.");
                    /* Print VHDL_2_HLDD_Mapping to file */
                    consoleWriter.write("Writing VHDL2HLDD map file...");
                    model.printMapFile(mapFileStream);
                    consoleWriter.done();

                    break;

                case VhdlBehDd2HlddBeh:

                    total = 4;
                    total += hlddType == HLDDRepresentationType.REDUCED ? 1 :
                            hlddType == HLDDRepresentationType.MINIMIZED ? 2 : 0;
                    total += shouldSimplify ? 1 : 0;

                    /* Parse VHDL structure */
                    consoleWriter.write(stat(current++, total) + "Parsing HIF structure...");
                    entity = Entity.parseVhdlStructure(sourceFile);
                    consoleWriter.done();

                    startTime = System.currentTimeMillis();
                    /* Process received VHDL structure */
                    consoleWriter.write(stat(current++, total) + "Preprocessing HIF structure...");
                    entity.traverse(new ClockEventRemover());
                    consoleWriter.done();
                    if (shouldSimplify) {
                        consoleWriter.write(stat(current++, total) + "Pre-simplifying HIF structure...");
                        entity.traverse(new IfNodeSimplifier(true, false));    // Simplifies VHDL Structure.
                        consoleWriter.done();
                    }

                    /* Generate Graphs (GraphVariables) and collect all variables */
                    consoleWriter.write(stat(current++, total) + "Generating HLDDs...");
                    graphCreatingVisitor = new BehDDGraphGenerator(config, settings);
                    entity.traverse(graphCreatingVisitor);
                    modelCollector = graphCreatingVisitor.getModelCollector();
                    consoleWriter.done();

                    /* Create HLDD model */
                    consoleWriter.write(stat(current++, total) + "Creating HLDD model...");
                    modelCreator = new BehModelCreatorImpl(modelCollector.getConstants(), modelCollector.getVariables());
                    model = modelCreator.getModel();
                    consoleWriter.done();

                    /* TRIM */
                    if (hlddType == HLDDRepresentationType.REDUCED) {
                        applyReductionRule1(model, current, total);
                    } else if (hlddType == HLDDRepresentationType.MINIMIZED) {
                        applyReductionRule1(model, current++, total);
                        applyReductionRule2(model, current, total);
                    }
                    startTime = System.currentTimeMillis() - startTime;
                    consoleWriter.writeLn("Conversion finished in " + startTime + " ms.");
                    consoleWriter.writeLn("Created model with " + model.getVarCount() + " Variables, " +
                            model.getGraphCount() + " Graphs and " + model.getNodeCount() + " Nodes.");
					/* Print VHDL_2_HLDD_Mapping to file */
					consoleWriter.write("Writing VHDL2HLDD map file...");
					model.printMapFile(mapFileStream);
					consoleWriter.done();

                    break;

                case HlddBeh2HlddRtl:

                    /* Parse HLDD structure */
                    consoleWriter.write("(1/3) Parsing Beh HLDD structure...");
                    BehModel behModel = BehModel.parseHlddStructure(sourceFile);
                    consoleWriter.done();

                    startTime = System.currentTimeMillis();
                    /* Transform HLDD structure from Beh to RTL level*/
                    consoleWriter.write("(2/3) Converting Beh to RTL...");
                    Beh2RtlTransformer transformer = new Beh2RtlTransformer(behModel);
                    model = transformer.getRtlModel();
                    consoleWriter.done();

                    //                System.out.println("CONTROL size (UNINDEXED): " + ((base.hldd.structure.variables.GraphVariable) model.getVarByName("CONTROL")).getGraph().getRootNode().getUnindexedSize());
                    //                System.out.println("CONTROL size (INDEXED): " + ((base.hldd.structure.variables.GraphVariable) model.getVarByName("CONTROL")).getGraph().getRootNode().getSize());
                    /* TRIM */
                    applyReductionRule2(model, 3, 3);
                    //                System.out.println("CONTROL size (UNINDEXED): " + ((base.hldd.structure.variables.GraphVariable) model.getVarByName("CONTROL")).getGraph().getRootNode().getUnindexedSize());
                    //                System.out.println("CONTROL size (INDEXED): " + ((base.hldd.structure.variables.GraphVariable) model.getVarByName("CONTROL")).getGraph().getRootNode().getSize());
                    startTime = System.currentTimeMillis() - startTime;
                    consoleWriter.writeLn("Conversion finished in " + startTime + " ms.");

                    consoleWriter.writeLn("Created model with " + model.getVarCount() + " Variables, " +
                            model.getGraphCount() + " Graphs, " + model.getNodeCount() + " Nodes and " +
                            ((Model) model).getCoutCount() + " Control Part Signals.");

                    break;

                case PSL2THLDD:
                    ParserShell pslParserShell = new ParserShell(sourceFile, pslFile, baseModelFile, consoleWriter);
                    pslParserShell.run();

					comment = pslParserShell.getComment();
                    model = pslParserShell.getModel();
                    break;

                default:
                    /* do nothing. Model = null. */
            }

        } catch (Exception e) {
			StringWriter stringWriter = new StringWriter();
			e.printStackTrace(new PrintWriter(stringWriter));
			LOG.fine(stringWriter.toString());
			e.printStackTrace();
			consoleWriter.failed();
			throw ExtendedException.create(e);
		} catch (Throwable throwable) {
            consoleWriter.failed();
            throw ExtendedException.create(throwable);
        } finally {
			workerFinalizer.doAfterWorker(this);			
		}

        return model;
    }

    private String stat(int current, int total) {
        return "(" + current + "/" + total + ") ";
    }

    private void applyReductionRule1(BehModel model, int currentStepIndex, int totalSteps) {
        consoleWriter.write(stat(currentStepIndex, totalSteps) + "Reducing model...");
        model.reduce();
        consoleWriter.done();
    }

    private void applyReductionRule2(BehModel model, int currentStepIndex, int totalSteps) {
        consoleWriter.write(stat(currentStepIndex, totalSteps) + "Minimizing model...");
        model.minimize();
        consoleWriter.done();
    }

    private String getTimeAsString() {
        return SEPARATOR + DATE_TIME_FORMAT.format(new Date(System.currentTimeMillis()));
    }

    protected void done() {

		if (isCancelled()) return;
		
		BehModel model;
		try {
			model = get();
		} catch (InterruptedException e) {return;} catch (ExecutionException e) {
			/* If error occurred while parsing, terminate process. */
			throw new RuntimeException(e.getCause());
		}

		workerFinalizer.doWhenDone(model);

        super.done();
    }

	public String getComment() {
		return comment;
	}

	public static BehModel convertAndWait(ConverterSettings settings) throws InterruptedException, ExecutionException {

		ConvertingWorker converter = new ConvertingWorker(
				AbstractWorkerFinalizer.getStub(),
				ConsoleWriter.getStub(),
				settings);

		converter.execute();

		while (!converter.isDone()) {

			Thread.sleep(50);

		}

		return converter.get();
	}

	public static BehModel convertInSeparateThreadAndWait(ConverterSettings settings) throws InterruptedException, ExecutionException {

		ConvertingWorker converter = new ConvertingWorker(
				AbstractWorkerFinalizer.getStub(),
				ConsoleWriter.getStub(),
				settings);

		Thread thread = new Thread(converter);

		thread.start();

		thread.join();

		return converter.get();
	}
}
