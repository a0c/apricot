package ee.ttu.pld.apricot.verifier;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;

/**
 * @author Anton Chepurov
 */
public class Statistics {
	static final String SKIPPING_NON_EXISTENT_FILE = "Skipping non-existent file: ";
	static final String SKIPPING_WITHOUT_VHDL = "Skipping file without VHDL: ";
	static final String CHANGED_FILE = "Changed file: ";

	private PrintStream printStream;
	private OutputStream outputStream;
	private int failedCount = 0;
	private int skippedCount = 0;
	private int passCount = 0;
	private int mapPassCount = 0;
	private int mapFailedCount = 0;

	private Statistics(OutputStream outputStream) {
		if (outputStream != null) {
			this.outputStream = outputStream;
			this.printStream = new PrintStream(new BufferedOutputStream(outputStream), true);
		} else {
			this.outputStream = System.out;
			this.printStream = System.out;
		}
	}

	public static Statistics createConsoleStatistics() {
		return new Statistics(null);
	}

	public static Statistics createByteArrayStatistics() {
		return new Statistics(new ByteArrayOutputStream());
	}

	public int getFailedCount() {
		return failedCount;
	}

	public int getSkippedCount() {
		return skippedCount;
	}

	public int getPassCount() {
		return passCount;
	}

	public int getMapPassCount() {
		return mapPassCount;
	}

	public int getMapFailedCount() {
		return mapFailedCount;
	}

	public String getMessage() {
		return outputStream.toString().trim();
	}

	private void print(String message) {
		printStream.println(message);
	}

	/* INFORMATIONAL ACTIONS */

	public void info(String message) {
		print(message);
	}

	/* STATISTICAL ACTIONS */

	public void skippedNonExistent(String filePath) {
		print(SKIPPING_NON_EXISTENT_FILE + filePath);
		skip();
	}

	public void skippedWithoutVHDL(String filePath) {
		print(SKIPPING_WITHOUT_VHDL + filePath);
		skip();
	}

	public void skipped(String message) {
		print(message);
		skip();
	}

	public void fail(String designFilePath) {
		print(CHANGED_FILE + designFilePath);
		failedCount++;
	}

	private void skip() {
		skippedCount++;
	}

	public void pass() {
		passCount++;
	}

	public void passMap() {
		mapPassCount++;
	}

	public void failMap(String mapFilePath) {
		print(CHANGED_FILE + mapFilePath);
		mapFailedCount++;
	}

	@Override
	public String toString() {
		if (failedCount == 0) { // none failed
			if (skippedCount == 0) { // no changes, none skipped --- all passed
				return MessageFormat.format("All files PASSED. Total: {0} {0,choice,1#file|1<files}.", passCount);
			} else { // no changes, some skipped
				if (passCount == 0) {
					return MessageFormat.format("All files SKIPPED. Total: {0} {0,choice,1#file|1<files}.", skippedCount);
				} else {
					return MessageFormat.format("{0} {0,choice,1#file|1<files} PASSED.{1}", passCount, skipped());
				}
			}
		} else { // some failed
			if (failedCount == total()) {
				return MessageFormat.format("All files CHANGED. Total: {0} {0,choice,1#file|1<files}.", failedCount);
			} else {
				return MessageFormat.format("{0} CHANGED {0,choice,1#file|1<files} (out of {1}).{2}", failedCount, total(), skipped());
			}
		}
	}

	public String mapToString() {
		if (mapFailedCount == 0) {
			if (mapPassCount == 0) {
				return "No MAP files.";
			} else {
				return MessageFormat.format("All MAP files PASSED. Total: {0} {0,choice,1#file|1<files}.", mapPassCount);
			}
		} else {
			if (mapPassCount == 0) {
				return MessageFormat.format("All MAP files CHANGED. Total: {0} {0,choice,1#file|1<files}.", mapFailedCount);
			} else {
				return MessageFormat.format("{0} CHANGED MAP {0,choice,1#file|1<files} (out of {1}).", mapFailedCount, totalMap());
			}
		}
	}

	private String skipped() {
		return skippedCount == 0 ? "" : " " + skippedCount + " SKIPPED.";
	}

	private int total() {
		return failedCount + passCount;
	}

	private int totalMap() {
		return mapFailedCount + mapPassCount;
	}
}
