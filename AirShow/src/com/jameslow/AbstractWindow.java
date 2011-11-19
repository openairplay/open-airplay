package com.jameslow;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public abstract class AbstractWindow extends JFrame {
	protected static final int BUFFER_WIDTH = 100;
	protected static final int BUFFER_HEIGHT = 100;
	
	public AbstractWindow() {
		super();
		setTitle(getDefaultTitle());
		setSettingBounds();
		if (Main.OS().addIcon()) {
			ImageIcon image = Main.AboutImage();
			if (image != null) {
				setIconImage(image.getImage());
			}
		}
		addComponentListener(new ComponentListener() {
			public void componentHidden(ComponentEvent e) {}
			public void componentMoved(ComponentEvent e) {
				saveWindowSettings();
			}
			public void componentResized(ComponentEvent e) {
				saveWindowSettings();
			}
			public void componentShown(ComponentEvent e) {
				saveWindowSettings();
			}
		});
		Main.addWindow(this);
	}
	public abstract void postLoad();
	public abstract String getDefaultTitle();
	public abstract WindowSettings getDefaultWindowSettings();
	public void saveWindowSettings() {
		Main.Settings().setWindowSettings(getWindowSettingsKey(), getSaveWindowSettings());
		Main.Settings().saveSettings();
	}
	public WindowSettings getSaveWindowSettings() {
		WindowSettings ws = getCurrentWindowSettings();
		if (alwaysShow()) {
			ws.setVisible(true);
		} else if (alwaysHide()) {
			ws.setVisible(false);
		}
		return ws;
	}
	public WindowSettings getCurrentWindowSettings() {
		return new WindowSettings(getWidth(), getHeight(), getX(), getY(), isVisible(), getExtendedState());
	}
	public WindowSettings getWindowSettings() {
		WindowSettings ws = Main.Settings().getWindowSettings(getWindowSettingsKey(),getDefaultWindowSettings());
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment(); 
		GraphicsDevice[] gd = ge.getScreenDevices();
		int j = 0;
		boolean onscreen = false;
		while (!onscreen && j < gd.length) { 
			GraphicsConfiguration[] gc = gd[j].getConfigurations();
			int i = 0;
			while (!onscreen && i < gc.length) { 
				Rectangle screen = gc[i].getBounds();
				if (ws.getLeft() >= screen.x
					&& ws.getLeft() <  (screen.x +  screen.width - (screen.width > BUFFER_WIDTH ? BUFFER_WIDTH : 0))
					&& ws.getTop() >= screen.y
					&& ws.getTop() <  (screen.y +  screen.height - (screen.height > BUFFER_HEIGHT ? BUFFER_HEIGHT : 0))) {
					onscreen = true;
				}
			    i++;
			}
			j++;
		}
		if (onscreen) {
			return ws;
		} else {
			return getDefaultWindowSettings();
		}
	}
	public Dimension getInnerSize() {
		Insets i = getInsets();
		return new Dimension(getWidth()-i.left-i.right,getHeight()-i.top-i.bottom);
	}
	public void setInnerSize(Dimension d) {
		Insets i = getInsets();
		setSize((int) (d.getWidth()+i.left+i.right),(int) (d.getHeight()+i.top+i.bottom));
	}
	public void setBounds(WindowSettings settings) {
		setBounds(settings.getLeft(), settings.getTop(), settings.getWidth(), settings.getHeight());
		setExtendedState(settings.getExtendedState());
	}
	public void setDefaultBounds() {
		setBounds(getDefaultWindowSettings());
	}
	public void setSettingBounds() {
		setBounds(getWindowSettings());
	}
	public boolean getJustHide() {
		//Can override this if your window contains a document that you want to close on OSX 
		//Or if you want to minimize an application to the system tray on Windows/Unix
		return !Main.OS().addQuit();
	}
	public boolean onClose() {
		return true;
	}
	public boolean alwaysShow() {
		return false;
	}
	public boolean alwaysHide() {
		return false;
	}
	public String getWindowSettingsKey() {
		return getClass().getName();
	}
}