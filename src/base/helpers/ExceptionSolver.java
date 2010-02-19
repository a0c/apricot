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
 * <br><br>User: Anton Chepurov
 * <br>Date: 29.10.2008
 * <br>Time: 21:03:54
 */
public class ExceptionSolver {
    private static final String MAIN_MESSAGE_START = "The following exception occurred:\n\n";
    private static final String MAIN_MESSAGE_END = "\n\nYou can try to solve the exception using Exception Solver." +
            "\nChoose one of the following:";
    private static final String MAIN_TITLE = "Exception Solver";
    private static final ExceptionSolver instance = new ExceptionSolver();
    private static final Map<String, Object> solutionByMessage = new HashMap<String, Object>();
    private JFrame frame;

    /* Deny instantiation */
    private ExceptionSolver() {}

    public static ExceptionSolver getInstance() {
        return instance;
    }

    public Object findSolution(String message, SolutionOptions expectedSolution) throws Exception {
        /* Check amongst available solutions */
        if (solutionByMessage.containsKey(message)) {
            return solutionByMessage.get(message);
        }
        /* Guide to solution */
        SolutionOptions selection = chooseSolution(message, expectedSolution);
        /* Receive and save the solution */
        solutionByMessage.put(message, inputSolution(selection));
        /* Return the solution */
        return solutionByMessage.get(message);
    }

    private Object inputSolution(SolutionOptions selection) throws Exception {
        switch (selection) {
            case CONSTANT_VALUE:
                String valueAsString = JOptionPane.showInputDialog(SolutionOptions.CONSTANT_VALUE.message, 0);
                /* If ESCAPE pressed: */
                if (valueAsString == null) {
                    Thread.currentThread().interrupt();
                }
                /* Use VHDLScanner, since HEX must be transformed from "X\"10\"" into "X \"10\"" */
                return PackageParser.parseConstantValueWithLength(new VHDLScanner(new LexemeComposer(valueAsString)).next().getValue());
            case IGNORE:
                return true;
            case TERMINATE:
                System.exit(0);
            case FILE_PATH:
//                new SingleFileSelector()
                break;
        }
        return null;
    }

    private SolutionOptions chooseSolution(String message, SolutionOptions expectedSolution) {
        int answer = JOptionPane.showOptionDialog(frame, createMessage(message), MAIN_TITLE, JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, SolutionOptions.titles(), expectedSolution.title);
        if (answer == 0) {
            return SolutionOptions.CONSTANT_VALUE;
        } else if (answer == 1) {
            return SolutionOptions.FILE_PATH;
        } else if (answer == 2) {
            return SolutionOptions.IGNORE;
        } else if (answer == 3) {
            return SolutionOptions.TERMINATE;
        } else return null;
    }

    private String createMessage(String message) {
        return MAIN_MESSAGE_START + message + MAIN_MESSAGE_END;
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }


    public enum SolutionOptions {
        CONSTANT_VALUE ("Constant value", "Enter constant value:"),
        FILE_PATH ("File path", ""),
        IGNORE("Ignore", ""),
        TERMINATE("Exit", "");

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
}
