package ui;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 23.02.2008
 * <br>Time: 20:52:27
 */
class CommentDialog extends JDialog implements ActionListener {

    private JPanel upperPanel;
    private JButton yes_Btn;
    private JButton no_Btn;
    private boolean commentAdded;
    private JTextArea textArea;
    private String comment;


    public CommentDialog(Frame owner, String title) throws HeadlessException {
        super(owner, title, true);

        setResizable(false);
        setSize(300, 180);
        setLocationRelativeTo(owner);
        getContentPane().setLayout(new BorderLayout());


        upperPanel = new JPanel();
        upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.Y_AXIS));
        JPanel labelPanel = new JPanel();
        JLabel label_1 = new JLabel("<html><br>File has been successfully converted.<br><br>" +
                "Do you want to add a comment <br> to the very beginning of the destination file?</html>");
        labelPanel.add(label_1);
        upperPanel.add(labelPanel);
        upperPanel.add(Box.createVerticalStrut(10));


        JPanel lowerPanel = new JPanel();
        lowerPanel.setPreferredSize(new Dimension(300, 40));
        lowerPanel.setMinimumSize(new Dimension(300, 40));
        lowerPanel.setMaximumSize(new Dimension(300, 40));
        yes_Btn = new JButton("Yes");
        no_Btn = new JButton("No");
        yes_Btn.addActionListener(this);
        no_Btn.addActionListener(this);
        lowerPanel.add(yes_Btn);
        lowerPanel.add(Box.createRigidArea(new Dimension(40, 0)));
        lowerPanel.add(no_Btn);


        getContentPane().add(upperPanel, BorderLayout.CENTER);
        getContentPane().add(lowerPanel, BorderLayout.PAGE_END);

        pack();
        no_Btn.requestFocusInWindow();
        setVisible(true);

    }

    public boolean isCommentAdded() {
        return commentAdded;
    }

    public String getComment() {
        return comment;
    }

    public void actionPerformed(ActionEvent e) {

        JComponent source = (JComponent) e.getSource();

        if (source == yes_Btn) {

            if (commentAdded) {
                comment = textArea.getText();
                this.dispose();

            } else {

                this.setSize(300, 350);
                textArea = new JTextArea();
                textArea.setFont(new Font("Tahoma", Font.PLAIN, 11));
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setViewportView(textArea);
                scrollPane.setPreferredSize(new Dimension(250, 170));
                scrollPane.setMinimumSize(new Dimension(250, 170));
                scrollPane.setMaximumSize(new Dimension(250, 170));
                upperPanel.add(scrollPane);
                yes_Btn.setText("Add");
                yes_Btn.setFocusable(false);
                no_Btn.setFocusable(false);
                commentAdded = true;

            }

            this.validate();
            this.repaint();

        } else if (source == no_Btn) {

            this.dispose();

        }

    }
}