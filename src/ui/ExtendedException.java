package ui;

import java.io.FileNotFoundException;
import java.util.EmptyStackException;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 21.02.2008
 * <br>Time: 16:41:02
 */
public class ExtendedException extends Exception {
    public static final String FILE_NOT_FOUND_TEXT = "File not found";
    public static final String MISSING_FILE_TEXT = "Missing file";
    public static final String ERROR_TEXT = "Error";
    public static final String IO_ERROR_TEXT = "I/O Error";
    private final String message;
    private final String title;


	public ExtendedException(String message, String title) {
        this.message = message;
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public String getTitle() {
        return title;
    }

    public static ExtendedException create(Throwable e) {
        String message;

		if (e instanceof FileNotFoundException) {
			message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			return new ExtendedException(message, FILE_NOT_FOUND_TEXT);
		} else if (e instanceof NullPointerException || e instanceof EmptyStackException) {
			String suffix = e instanceof NullPointerException ? "NPE" : "ESE";
			message = "Critical error occurred while processing:" +
					"\nSpecified source file contains unsupported constructs or is of a wrong type (" + suffix + ").";
			return new ExtendedException(message, ERROR_TEXT);
		} else {
			message = e.getClass().getSimpleName();
			if (e.getMessage() != null) message = message + ": " + e.getMessage();
			return new ExtendedException(message, ERROR_TEXT);
		}
    }
}
