package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * @author Anton Chepurov
 */
class CommentDialog extends JDialog implements ActionListener, KeyListener {

	private JButton addBtn;
	private JTextArea textArea;
	private String comment = null;


	public CommentDialog(Frame owner, String title) throws HeadlessException {
		super(owner, title, true);
		setIconImage(owner.getIconImage());

		setResizable(false);
		setSize(300, 180);
		setLocationRelativeTo(owner);
		getContentPane().setLayout(new BorderLayout());


		JPanel upperPanel = new JPanel();
		upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.Y_AXIS));
		JPanel labelPanel = new JPanel();
		JLabel label = new JLabel("<html><br>File has been successfully converted.<br><br>" +
				"Add a comment to the very beginning <br> of the destination file:</html>");
		labelPanel.add(label);
		upperPanel.add(labelPanel);
		upperPanel.add(Box.createVerticalStrut(10));

		this.setSize(300, 350);
		textArea = new JTextArea();
		textArea.setFont(new Font("Tahoma", Font.PLAIN, 11));
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setViewportView(textArea);
		scrollPane.setPreferredSize(new Dimension(250, 170));
		scrollPane.setMinimumSize(new Dimension(250, 170));
		scrollPane.setMaximumSize(new Dimension(250, 170));
		upperPanel.add(scrollPane);

		JPanel lowerPanel = new JPanel();
		lowerPanel.setPreferredSize(new Dimension(300, 40));
		lowerPanel.setMinimumSize(new Dimension(300, 40));
		lowerPanel.setMaximumSize(new Dimension(300, 40));
		addBtn = new JButton("Add");
		addBtn.setMnemonic(KeyEvent.VK_A);
		JButton cancelBtn = new JButton("Cancel");
		addBtn.addActionListener(this);
		cancelBtn.addActionListener(this);
		cancelBtn.setMnemonic(KeyEvent.VK_C);
		lowerPanel.add(addBtn);
		lowerPanel.add(Box.createRigidArea(new Dimension(40, 0)));
		lowerPanel.add(cancelBtn);


		getContentPane().add(upperPanel, BorderLayout.CENTER);
		getContentPane().add(lowerPanel, BorderLayout.PAGE_END);

		textArea.addKeyListener(this);
		addBtn.addKeyListener(this);
		cancelBtn.addKeyListener(this);

		pack();
		setVisible(true);

	}

	public String getComment() {
		return comment;
	}

	public void actionPerformed(ActionEvent e) {

		JComponent source = (JComponent) e.getSource();

		if (source == addBtn) {

			comment = textArea.getText();
		}

		this.dispose();

	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			dispose();
		}
	}
}
