package ee.ttu.pld.apricot.cli;

import ui.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Anton Chepurov
 */
public class TaskRunner {

	public TaskRunner(Request request) {
		if (request.isBroken()) {
			request.printBroken();
			return;
		}
		processRequest(request);
	}

	private void processRequest(Request request) {

		List<String> cmd = new ArrayList<String>(10);

		cmd.add(ui.ApplicationForm.LIB_DIR + (Platform.isWindows() ? "hlddsim.exe" : "hlddsim"));
		request.buildCommand(cmd);

		run(cmd, request);
	}

	@SuppressWarnings({"StaticMethodNamingConvention"})
	private static void run(List<String> cmd, Request request) {

		try {
			Process process = Runtime.getRuntime().exec(cmd.toArray(new String[cmd.size()]));

			boolean success = waitForProcessToComplete(process, cmd.toString());

			if (success) {
				request.markSuccessful();
			}

		} catch (IOException e) {
			System.out.println("[APRICOT] ### ERROR ###: " + e.getMessage());
		}
	}

	@SuppressWarnings({"BooleanMethodNameMustStartWithQuestion"})
	private static boolean waitForProcessToComplete(Process process, String cmd) {
		InputStream inputStream = process.getInputStream();
		InputStream errorStream = process.getErrorStream();
		boolean isProcessFinished = false;
		boolean success = false;
		try {
			while (!isProcessFinished && !Thread.interrupted()) {

				readInputAndErrorStreams(inputStream, errorStream);

				try {
					int exitValue = process.exitValue();
					isProcessFinished = true;
					success = exitValue == 0;
					if (!success) {
						System.out.println("[APRICOT] ### ERROR ###: Task failed with error " + exitValue + ". Task: " + cmd);
					}
				} catch (IllegalThreadStateException e) {
					// indicates that process.exitValue() cannot return any value yet
					try {
						Thread.sleep(200);
					} catch (InterruptedException e1) {
						break;
					}
				}
			}

			readInputAndErrorStreams(inputStream, errorStream);

		} catch (IOException e) {
			System.out.println("[APRICOT] ### ERROR ###: " + e.getMessage());
		}
		return success;
	}

	private static void readInputAndErrorStreams(InputStream inputStream, InputStream errorStream) throws IOException {
		/* Read OUTPUT */
		readFromStream(inputStream, System.out);
		/* Read ERROR */
		readFromStream(errorStream, System.err);
	}

	@SuppressWarnings({"ResultOfMethodCallIgnored"})
	private static void readFromStream(InputStream inputStream, PrintStream printStream) throws IOException {
		int byteCount = inputStream.available();
		if (byteCount > 0) {
			byte[] bytes = new byte[byteCount];
			inputStream.read(bytes);
			printStream.write(bytes);
			printStream.flush();
		}
	}

}
