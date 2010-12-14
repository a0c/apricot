package ui.fileViewer;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;

/**
 * @author Anton Chepurov
 */
public class TabComponent extends JPanel {

	private static final Color DIRTY_COLOR = Color.ORANGE;
	private Color color;
	private Color defaultColor;

	private final JTabbedPane tabbedPane;

	private final String title;

	private final TabButton button;

	public TabComponent(JTabbedPane tabbedPane, String title, String toolTip, MouseListener mouseListener) {

		super(new FlowLayout(FlowLayout.LEFT, 0, 0));
		this.tabbedPane = tabbedPane;
		this.title = title;
		setOpaque(false);
		defaultColor = getBackground();
		color = defaultColor;

		/* Make JLabel read title from tabbedPane */
		JLabel label = new JLabel(title);
		setToolTipText(toolTip);

		add(label);

		add(Box.createHorizontalStrut(5));

		add(button = new TabButton());

		addMouseListener(mouseListener);
	}

	public JButton getButton() {
		return button;
	}

	public String getTitle() {
		return title;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
		if (color != null) {
			setOpaque(true);
			setBackground(color);
		} else {
			setOpaque(false);
			setBackground(defaultColor);
			this.color = defaultColor;
		}
	}

	public static void setBackgroundFor(Component component, boolean isDirty) {

		if (component instanceof TabComponent) {
			TabComponent tabComponent = (TabComponent) component;

			if (isDirty) {
				tabComponent.setOpaque(true);
				component.setBackground(DIRTY_COLOR);
			} else {
				tabComponent.setOpaque(false);
				component.setBackground(tabComponent.defaultColor);
			}
		}
	}

	public boolean isDirty() {
		return getBackground() == DIRTY_COLOR;
	}

	public void markMutated(boolean isMutated) {
		setBorder(isMutated ? TableForm.MUTATION_BORDER : null);
	}

	private class TabButton extends JButton implements ActionListener {

		private TabButton() {
			int size = 17;
			setPreferredSize(new Dimension(size, size));
			setUI(new BasicButtonUI());
			/* Make button transparent */
			setContentAreaFilled(false);
			/* No need to be focusable */
			setFocusable(false);
			/* Create future animation border */
			setBorder(BorderFactory.createEtchedBorder());
			setBorderPainted(false);
			/* Same listener for all buttons */
			addMouseListener(BUTTON_MOUSE_LISTENER);
			setRolloverEnabled(true);

			/* Close the proper tab by clicking the button */
			addActionListener(TabButton.this);
		}

		/**
		 * Disable update of the component
		 */
		public void updateUI() {
		}

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			/* Shift the image for pressed buttons */
			if (getModel().isPressed()) {
				g2.translate(1, 1);
			}

			g2.setStroke(new BasicStroke(2));
			g2.setColor(Color.RED);

			int delta = 6;
			g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
			g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
			g2.dispose();
		}

		public void actionPerformed(ActionEvent e) {
			int index = tabbedPane.indexOfTabComponent(TabComponent.this);
			if (index != -1) {
				tabbedPane.remove(index);
				System.gc();
			}
		}
	}

	private final static MouseListener BUTTON_MOUSE_LISTENER = new MouseAdapter() {
		public void mouseEntered(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof AbstractButton) {
				((AbstractButton) component).setBorderPainted(true);
			}
		}

		public void mouseExited(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof AbstractButton) {
				((AbstractButton) component).setBorderPainted(false);
			}
		}
	};
}
