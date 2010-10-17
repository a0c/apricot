package ui.utils;

import base.hldd.structure.models.BehModel;

/**
 * @author Anton Chepurov
 */
public abstract class AbstractWorkerFinalizer {

	public abstract void doBeforeWorker();

	public abstract void doAfterWorker(ConvertingWorker convertingWorker);

	public abstract void doWhenDone(BehModel model);

	public static AbstractWorkerFinalizer getStub() {
		return new AbstractWorkerFinalizer() {
			@Override
			public void doBeforeWorker() {
			}

			@Override
			public void doAfterWorker(ConvertingWorker convertingWorker) {
			}

			@Override
			public void doWhenDone(BehModel model) {
			}
		};
	}
}
