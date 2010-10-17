package ui;

/**
 * @author Anton Chepurov
 */
public class DefaultUncaughtExceptionHandler extends ThreadGroup {

	private static final String SOME_NAME = "SomeName";
	private final ApplicationForm applicationForm;

	public DefaultUncaughtExceptionHandler(ApplicationForm applicationForm) {
		super(SOME_NAME);
		this.applicationForm = applicationForm;
	}

	public void uncaughtException(Thread t, Throwable e) {
		if (applicationForm != null) {
			if (e instanceof ExtendedException) {
				applicationForm.showErrorMessage((ExtendedException) e);
			} else if (e instanceof RuntimeException && e.getCause() instanceof ExtendedException) {
				applicationForm.showErrorMessage((ExtendedException) e.getCause());
			} else {
				//noinspection ThrowableResultOfMethodCallIgnored
				applicationForm.showErrorMessage(ExtendedException.create(e));
			}
		}
	}

}
