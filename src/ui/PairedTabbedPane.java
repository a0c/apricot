package ui;

import javax.swing.*;

/**
 * @author Anton Chepurov
 */
public class PairedTabbedPane extends JTabbedPane {

	private JTabbedPane pair;

	public void setPair(JTabbedPane pair) {
		this.pair = pair;
	}

	public JTabbedPane getPair() {
		return pair;
	}
}
