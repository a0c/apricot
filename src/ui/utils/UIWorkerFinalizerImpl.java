package ui.utils;

import base.hldd.structure.models.BehModel;
import io.ConsoleWriter;
import ui.ApplicationForm;
import ui.BusinessLogic;

/**
 * @author Anton Chepurov
 */
public class UIWorkerFinalizerImpl extends AbstractWorkerFinalizer {

	private final BusinessLogic businessLogic;
	private final ConsoleWriter consoleWriter;

	public UIWorkerFinalizerImpl(BusinessLogic businessLogic, ConsoleWriter consoleWriter) {
		this.businessLogic = businessLogic;
		this.consoleWriter = consoleWriter;
	}

	@Override
	public void doBeforeWorker() {
		/* Disable UI */
		businessLogic.getApplicationForm().enableUI(false);
	}

	@Override
	public void doAfterWorker(ConvertingWorker convertingWorker) {
		/* Save comment from PSL converter */
		businessLogic.addComment(convertingWorker.getComment());
		/* Enable UI */
		businessLogic.getApplicationForm().enableUI(true);
	}

	@Override
	public void doWhenDone(BehModel model) {

		businessLogic.setModel(model);

		ApplicationForm applicationForm = businessLogic.getApplicationForm();

		applicationForm.doAskForComment();

		applicationForm.doSaveConvertedModel();

		applicationForm.paintCreatedFileGreen();

		consoleWriter.writeLn("");

		if (applicationForm.getSelectedParserId() == BusinessLogic.ParserID.VhdlBeh2HlddBeh) {
			businessLogic.doLoadHlddGraph();
		}
	}
}
