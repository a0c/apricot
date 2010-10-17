package ui;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.*;

/**
 * @author Anton Chepurov
 */
public class TooltipShowingFocusAdapter extends FocusAdapter {

	public void focusGained(FocusEvent e) {
		showTooltip(e);
	}

	public void focusLost(FocusEvent e) {
		showTooltip(e);
	}

	private void showTooltip(FocusEvent e) {
		Component sourceComponent = (Component) e.getSource();
		sourceComponent.dispatchEvent(new KeyEvent(sourceComponent, KeyEvent.KEY_PRESSED, 0,
				KeyEvent.CTRL_MASK, KeyEvent.VK_F1, (char) KeyEvent.VK_F1));
	}
}
