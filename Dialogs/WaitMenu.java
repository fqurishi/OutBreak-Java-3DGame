package Dialogs;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class WaitMenu extends JDialog {
	private static final long serialVersionUID = -4837039494584074528L;
	private JButton playButton;
	
	
	public WaitMenu(boolean start) {
		if (start == false) {
			try {
				initWindow();
				this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
				this.setVisible(true);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		else {
			dispose();
		}
	}
	

	private void initWindow() throws UnknownHostException {
		this.setTitle("OutBreak: biohazard conatinment");
		this.setResizable(false);
		this.setPreferredSize(new Dimension(400, 200));
		
		JLabel waitLabel = new JLabel("Waiting for player 2");
		
		playButton = new JButton("OK");
		playButton.addActionListener(new PlayButtonAction());
		playButton.setPreferredSize(new Dimension(50, 50));
		
		this.getContentPane().setLayout(new BorderLayout());
		
		final JPanel topContainer = new JPanel();
		topContainer.setLayout(new FlowLayout());
		topContainer.setSize(500, 250);
		
		topContainer.add(waitLabel);
		
		this.getContentPane().add(topContainer, BorderLayout.NORTH);
		this.getContentPane().add(playButton, BorderLayout.SOUTH);
		
		final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		
		this.pack();
		
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	private class PlayButtonAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt) {
				try {
					dispose();
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				
			}
		}
	

}