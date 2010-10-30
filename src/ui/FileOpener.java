package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

/**
 * @author Anton Chepurov
 */
public class FileOpener extends MouseAdapter {

	private final JTextField textField;

	public FileOpener(JTextField textField) {
		this.textField = textField;
	}

	@Override
	public void mouseClicked(MouseEvent e) {

		if (!Desktop.isDesktopSupported()) {
			return;
		}

		String tooltip = textField.getToolTipText();

		if (e.getClickCount() == 2 && tooltip != null) {

			File file = new File(tooltip);

			Desktop desktop = Desktop.getDesktop();

			try {

				desktop.open(file);

			} catch (IOException e1) {
				/* do nothing, let it be :( */
			} catch (Throwable e2) {
				/* do nothing, let it be =) */
			}
		}

	}
}
