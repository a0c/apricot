package ui.utils.uiWithWorker;

import ui.ExtendedException;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Arrays;

import io.ConsoleWriter;
import io.QuietCloser;

/**
 * <p>User: Anton Chepurov
 * <br>Date: 13.08.2007
 * <br>Time: 22:08:26
 */
public abstract class TaskSwingWorker extends SwingWorker<Boolean, Integer> {
    public static final int NO_TIMEOUT = -1;

    protected boolean isProcessFinished = false;
    private final String[] executableCommand;
    protected Runnable executableRunnable;
    private int executionTimeout;
    private OutputStream infoOut;
    private OutputStream errorOut;
    private ConsoleWriter consoleWriter;

    protected UIInterface uiHolder;
    private Process process;

    protected ExtendedException occurredException;


//    public TaskSwingWorker(String[] executableCommand, int executionTimeout, OutputStream infoOut, OutputStream errorOut) {
//        this.executableCommand = executableCommand;
//        this.executionTimeout = executionTimeout;
//        this.infoOut = infoOut;
//        this.errorOut = errorOut;
//    }


    protected TaskSwingWorker(List<String> executableCommand, OutputStream infoOut, OutputStream errorOut, ConsoleWriter consoleWriter) {
        this.errorOut = errorOut;
        this.consoleWriter = consoleWriter;
        this.executableCommand = executableCommand.toArray(new String[executableCommand.size()]);
        this.executionTimeout = NO_TIMEOUT;
        this.infoOut = infoOut;
        /* Disable alternative */
        executableRunnable = null;
    }

    protected TaskSwingWorker() {
        /* Disable alternative */
        executableCommand = null;        
    }

    protected Boolean doInBackground() {
        if (executableCommand != null) {
            InputStream inputStream = null;
            InputStream errorStream = null;
            try {

                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                process = Runtime.getRuntime().exec(executableCommand);

                inputStream = process.getInputStream();
                errorStream = process.getErrorStream();
                StringBuilder consoleTracer = new StringBuilder();

                int timeout = executionTimeout;
                int exitValue = -999;

                while (!isProcessFinished && !Thread.interrupted()) {
                    /* Read OUTPUT */
                    int byteCount = inputStream.available();
                    if (byteCount > 0) {
                        for (int i = 0; i < byteCount; i++) {
                            int aChar = inputStream.read();
                            infoOut.write(aChar); //todo: read N at once. + use buffered reader??
                            consoleWriter.write(String.valueOf((char) aChar));
                            consoleTracer.append((char) aChar);
                        }
                        infoOut.flush();
                    }
                    /* Read ERROR */
                    int errorBytesAvailable = errorStream.available();
                    if (errorBytesAvailable > 0) {
                        for (int i = 0; i < errorBytesAvailable; i++) {
                            errorOut.write(errorStream.read());
                        }
                        errorOut.flush();
                    }

                    try {
                        exitValue = process.exitValue();
                        isProcessFinished = true;
                    }
                    catch (IllegalThreadStateException e) {
                        // indicates that process.exitValue() cannot return any value yet!!!
                        try {
                            // still running.
                            StringBuilder errorMessage = new StringBuilder();
                            Thread.sleep(300);
                            if (timeout != NO_TIMEOUT) {
                                timeout = timeout - 300;
                                if (timeout < 0 && timeout >= -300) {
                                    errorMessage.append("ALERT: Command doesn't terminate:\n");
                                    errorMessage.append(Arrays.toString(executableCommand)).append("\n");
                                    errorMessage.append("Shutting down command...\n");
                                    errorOut.write(errorMessage.toString().getBytes());
                                    errorOut.flush();
                                    process.destroy();
                                } else if (timeout < 0) {
                                    errorMessage.append("ALERT: Command STILL doesn't terminate:\n");
                                    errorMessage.append(Arrays.toString(executableCommand)).append("\n");
                                    errorOut.write(errorMessage.toString().getBytes());
                                    errorOut.flush();
                                    Thread.sleep(1000);
                                }
                            }
                        } catch (InterruptedException e1) {
                            // doesn't matter
                        }
                    }
                }
                if (isProcessFinished) {
                    // finished running
                    if (exitValue == 0) {
                        infoOut.write("Terminated without errors".getBytes());
                        infoOut.flush();
                        uiHolder.showSuccessDialog();
                    } else {
                        errorOut.write(("Exit code " + exitValue + " while performing command " + Arrays.toString(executableCommand)).getBytes());
                        errorOut.flush();
                        if (consoleTracer.length() > 0) {
                            uiHolder.showErrorDialog(consoleTracer.toString());
                        }
                    }
                } else {
                    process.destroy();
                }
                return exitValue == 0;

            }
            catch (IOException e) {
                uiHolder.hideDialog();
                Thread.currentThread().interrupt();
                /* Remember the exceptions because none of the Exceptions thrown from doInBackground() method is caught.
                 * Process the occurred exception in done() method, run in EDT.  */
                occurredException = ExtendedException.create(e);
                return false;
            }

            finally {
                QuietCloser.closeQuietly(inputStream);
                QuietCloser.closeQuietly(errorStream);
            }
        }  else if (executableRunnable != null) {
            
            executableRunnable.run();
            if (occurredException == null) {
                uiHolder.showSuccessDialog();
            }
            return isProcessFinished;

        }
        String message = TaskSwingWorker.class.getName() + " didn't receive neither an executableCommand nor a Runnable to run";
        occurredException = new ExtendedException(message, ExtendedException.ERROR_TEXT);
        return false;
    }

    public void stopWorker() {
        if (process != null) {
            if (getState() == SwingWorker.StateValue.STARTED) {
                process.destroy();
            }
        } else if (executableRunnable != null) {
            cancel(true);
        }
    }

    protected void done() {
        if (uiHolder != null) {
            uiHolder.hideDialog();
        }

        if (occurredException != null) {
            throw new RuntimeException(occurredException);
        }
    }

    public void setUi(UIInterface ui) {
        uiHolder = ui;
    }
}
