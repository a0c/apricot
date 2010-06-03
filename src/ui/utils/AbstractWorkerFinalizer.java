package ui.utils;

import base.hldd.structure.models.BehModel;
import ui.ExtendedException;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 30.05.2010
 * <br>Time: 23:09:15
 */
public abstract class AbstractWorkerFinalizer {

	public abstract void doBeforeWorker();
	public abstract void doAfterWorker(ConvertingWorker convertingWorker);
	public abstract void doWhenDone(BehModel model);

	public abstract void doReactOnConfigError(ExtendedException e, ConvertingWorker convertingWorker);

}
