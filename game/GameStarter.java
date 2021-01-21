package game;

import Dialogs.StartMenu;

public class GameStarter {
	
	private String ipAddress;
	private int port = 6000;
	private boolean isFullScreen = false;
	private boolean isSingle = false;
	private boolean gamepad = false;
	private boolean audio = true;
	private String avatar;
	
	private MyGame g;
	
	private boolean running = false;
	
	public static void main(String[] args) {
		new GameStarter().init();
	}
	
	public void init() {
		new StartMenu(this);
		if(this.audio != true)
			this.g = new MyGameNoAudio(this.ipAddress, this.port, this.isFullScreen, this.avatar, this.isSingle, this.gamepad);
		else
			this.g = new MyGame(this.ipAddress, this.port, this.isFullScreen, this.avatar, this.isSingle, this.gamepad);
		if (running) {
			this.g.init();
			this.running = false;
			init();
		} else {
			System.exit(0);
		}
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean getIsRunning() {
		return running;
	}

	public void setIsRunning(boolean run) {
		this.running = run;
	}

	public boolean isFullScreen() {
		return isFullScreen;
	}

	public void setFullScreen(boolean isFullScreen) {
		this.isFullScreen = isFullScreen;
	}
	
	public boolean isSingle() {
		return isSingle;
	}
	
	public void setSingle(boolean isSingle) {
		this.isSingle = isSingle;
	}
	
	public boolean usingGamepad() {
		return gamepad;
	}
	
	public void setGamepad(boolean gamepad) {
		this.gamepad = gamepad;
	}
	
	public boolean hasAudio() {
		return audio;
	}
	
	public void setAudio(boolean audio) {
		this.audio = audio;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	
}