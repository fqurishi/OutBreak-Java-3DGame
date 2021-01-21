package Dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.UnknownHostException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import game.GameStarter;


public class StartMenu extends JDialog {

	private static final long serialVersionUID = -4837039494584074528L;

	private boolean gameStarted = false;
	
	private JTextField serverIPAddress;
	private JTextField serverPort;
	private JCheckBox fullscreen;
	private JCheckBox singlePlayer;
	private JCheckBox gamepad;
	private JCheckBox audio;
	private JComboBox<String> avatarSelect;
	private JButton playButton;
	private String[] avatarNames = {"Grim", "Alex"};
	
	private final GameStarter gs;
	
	public StartMenu(GameStarter gs) {
		this.gs = gs;
		try {
			initWindow();
			this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
			this.setVisible(true);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	

	private void initWindow() throws UnknownHostException {
		this.setTitle("OutBreak: biohazard conatinment");
		this.setResizable(false);
		this.setPreferredSize(new Dimension(400, 200));
		
		JLabel serverIPAddressLabel = new JLabel("Server IP:");
		JLabel serverPortLabel = new JLabel("Server Port:");
		JLabel chooseAvatar = new JLabel("Character:");
		
		serverIPAddress = new JTextField(gs.getIpAddress());
		serverIPAddress.setPreferredSize(new Dimension(75, 25));
		
		serverPort = new JTextField(String.valueOf(gs.getPort()));
		serverPort.setPreferredSize(new Dimension(50, 25));
		
		fullscreen = new JCheckBox("Fullscreen", gs.isFullScreen());
		singlePlayer = new JCheckBox("SinglePlayer", gs.isSingle());
		gamepad = new JCheckBox("Gamepad", gs.usingGamepad());
		audio = new JCheckBox("Audio", gs.hasAudio());
		avatarSelect = new JComboBox<String>(avatarNames);
		
		
		
		playButton = new JButton("Play");
		playButton.addActionListener(new PlayButtonAction());
		playButton.setPreferredSize(new Dimension(50, 50));
		
		this.getContentPane().setLayout(new BorderLayout());
		
		final JPanel topContainer = new JPanel();
		topContainer.setLayout(new FlowLayout());
		topContainer.setSize(500, 250);
		
		topContainer.add(serverIPAddressLabel);
		topContainer.add(serverIPAddress);
		topContainer.add(serverPortLabel);
		topContainer.add(serverPort);
		
		final JPanel midContainer = new JPanel();
		midContainer.setLayout(new BorderLayout());
		
		
		final JPanel secondContainer = new JPanel();
		secondContainer.setLayout(new FlowLayout());
		
		secondContainer.add(fullscreen);
		secondContainer.add(singlePlayer);
		secondContainer.add(gamepad);
		secondContainer.add(audio);
		midContainer.add(secondContainer, BorderLayout.NORTH);
		midContainer.add(chooseAvatar, BorderLayout.CENTER);
		midContainer.add(avatarSelect, BorderLayout.SOUTH);
		
		this.getContentPane().add(topContainer, BorderLayout.NORTH);
		this.getContentPane().add(midContainer, BorderLayout.CENTER);
		this.getContentPane().add(playButton, BorderLayout.SOUTH);
		
		final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		
		this.pack();
		
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	
	private class PlayButtonAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt) {
			if (gameStarted) {
				gameStarted = false;
			} else {
				try {
					gs.setIpAddress(serverIPAddress.getText().trim());
					gs.setPort(Integer.parseInt(serverPort.getText().trim()));
					gs.setFullScreen(fullscreen.isSelected());
					gs.setSingle(singlePlayer.isSelected());
					gs.setAvatar((String) avatarSelect.getSelectedItem());
					gs.setIsRunning(true);
					gs.setGamepad(gamepad.isSelected());
					gs.setAudio(audio.isSelected());
					gameStarted = true;
					dispose();
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				
			}
		}
	}

}