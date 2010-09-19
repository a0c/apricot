package ui.utils;

import base.hldd.structure.models.BehModel;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 30.05.2010
 * <br>Time: 23:09:15
 */
public abstract class AbstractWorkerFinalizer {

	public abstract void doBeforeWorker();
	public abstract void doAfterWorker(ConvertingWorker convertingWorker);
	public abstract void doWhenDone(BehModel model);

	public static AbstractWorkerFinalizer getStub() {
		return new AbstractWorkerFinalizer() {
			@Override
			public void doBeforeWorker() {}
			@Override
			public void doAfterWorker(ConvertingWorker convertingWorker) {}
			@Override
			public void doWhenDone(BehModel model) {}
		};
	}
}
