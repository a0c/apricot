package base.helpers;

import parsers.vhdl.PackageParser;

import javax.swing.*;
import java.util.Map;
import java.util.HashMap;

import io.scan.VHDLScanner;
import io.scan.LexemeComposer;

/**
 * Singleton
 *
 * @author Anton Chepurov
 */
public class ExceptionSolver {
	private static final String MAIN_MESSAGE_START = "The following exception occurred:\n\n";
	private static final String MAIN_MESSAGE_END = "\n\nYou can try to solve the exception using Exception Solver." +
			"\nChoose one of the following:";
	private static final String MAIN_TITLE = "Exception Solver";
	private static final ExceptionSolver INSTANCE = new ExceptionSolver();
	private static final Map<String, Object> SOLUTION_BY_MESSAGE = new HashMap<String, Object>();
	private JFrame frame;

	/* Deny instantiation */

	private ExceptionSolver() {
	}

	public static ExceptionSolver getInstance() {
		return INSTANCE;
	}

	public Object findSolution(String message, SolutionOptions expectedSolution) throws Exception {
		/* Check amongst available solutions */
		if (SOLUTION_BY_MESSAGE.containsKey(message)) {
			return SOLUTION_BY_MESSAGE.get(message);
		}
		/* Guide to solution */
		SolutionOptions selection = chooseSolution(message, expectedSolution);
		/* Receive and save the solution */
		SOLUTION_BY_MESSAGE.put(message, inputSolution(selection));
		/* Return the solution */
		return SOLUTION_BY_MESSAGE.get(message);
	}

	private Object inputSolution(SolutionOptions selection) throws Exception {
		switch (selection) {
			case VALUE:
				String valueAsString = JOptionPane.showInputDialog(SolutionOptions.VALUE.message, 0);
				/* If ESCAPE pressed: */
				if (valueAsString == null) {
					Thread.currentThread().interrupt();
				}
				/* Use VHDLScanner, since HEX must be transformed from "X\"10\"" into "X \"10\"" */
				return PackageParser.parseConstantValueWithLength(new VHDLScanner(new LexemeComposer(valueAsString)).next().getValue());
			case IGNORE:
				return true;
			case EXIT:
				System.exit(0);
			case CANCEL:
				return CancellingException.getInstance();
			case PATH:
//                new SingleFileSelector()
				break;
		}
		return null;
	}

	private SolutionOptions chooseSolution(String message, SolutionOptions expectedSolution) {
		int answer = JOptionPane.showOptionDialog(frame, createMessage(message), MAIN_TITLE, JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE, null, SolutionOptions.titles(), expectedSolution.title);
		switch (answer) {
			case 0:
				return SolutionOptions.VALUE;
			case 1:
				return SolutionOptions.PATH;
			case 2:
				return SolutionOptions.IGNORE;
			case 3:
				return SolutionOptions.EXIT;
			case 4:
			case -1:
				return SolutionOptions.CANCEL;
			default:
				return null;
		}
	}

	private String createMessage(String message) {
		return MAIN_MESSAGE_START + message + MAIN_MESSAGE_END;
	}

	public void setFrame(JFrame frame) {
		this.frame = frame;
	}


	public enum SolutionOptions {
		VALUE("Constant value", "Enter constant value:"),
		PATH("File path", ""),
		IGNORE("Ignore", ""),
		EXIT("Exit", ""),
		CANCEL("Cancel", "");

		private final String title;
		private final String message;

		SolutionOptions(String title, String message) {
			this.title = title;
			this.message = message;
		}

		private static String[] titles() {
			String[] messages = new String[values().length];
			int i = 0;
			for (SolutionOptions solutionOption : values()) {
				messages[i++] = solutionOption.title;
			}
			return messages;
		}
	}

	public static class CancellingException extends Exception {
		static CancellingException instance;

		public static CancellingException getInstance() {
			if (instance == null) {
				instance = new CancellingException();
			}
			return instance;
		}
	}
}
