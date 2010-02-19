package io;

import javax.swing.*;
import java.util.logging.Logger;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 09.12.2008
 * <br>Time: 16:34:26
 */
public class ConsoleWriter {
    private static final String NEW_LINE = System.getProperty("line.separator");
    public static final String DONE_TEXT = "\t\tDone.";
    public static final String FAILED_TEXT = "\t\tFailed." + NEW_LINE;

	private static final Logger LOG = Logger.getLogger(ConsoleWriter.class.getName());

    private final JTextArea consoleTextArea;
    private final boolean toConsole;
	private StringBuilder logMessageBuilder = null;
    private static final String DOT_TEXT = ".";
    private static final String IN_TEXT = " in ";
    private static final String MS_TEXT = " ms.";

	public ConsoleWriter(JTextArea consoleTextArea, boolean toConsole) {
        this.consoleTextArea = consoleTextArea;
        this.toConsole = toConsole;
    }

    public void done(long ms) {
        writeLn(insertMs(ms));
    }

    public void done() {
        writeLn(DONE_TEXT);
    }

    public void failed() {
        writeLn(FAILED_TEXT);
    }

    public void writeLn(String line) {
        if (toConsole) {
            System.out.println(line);
        }
        consoleTextArea.append(line + NEW_LINE);
		logPrint(line);
		/* Auto-Scroll to bottom */
        autoscrollToBottom();
    }

	public void write(String line) {
        if (toConsole) {
            System.out.print(line);
        }
        consoleTextArea.append(line);
		logCollect(line); // collect for further print into log
		/* Auto-Scroll to bottom */
        autoscrollToBottom();
    }

	private String insertMs(long ms) {
        return DONE_TEXT.replace(DOT_TEXT, IN_TEXT + ms + MS_TEXT);
    }

	private void autoscrollToBottom() {
        /* Auto-Scroll to bottom */
        int length = consoleTextArea.getDocument().getLength();
        consoleTextArea.select(length, length);
    }

	/* ######################
	######  LOGGER #########*/
	private void logCollect(String line) {
		getLogMessageBuilder().append(line); // collect for further print into log
	}

	private void logPrint(String line) {
		StringBuilder mesBuilder = getLogMessageBuilder();
		LOG.info(mesBuilder.append(line).toString());
		logReset();
	}

	private void logReset() {
		logMessageBuilder = new StringBuilder();
	}

	public StringBuilder getLogMessageBuilder() {
		if (logMessageBuilder == null) {
			logReset();
		}
		return logMessageBuilder;
	}
}
