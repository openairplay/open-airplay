package com.jameslow;

import java.awt.event.*;
import javax.swing.*;

public class MainWindow extends AbstractWindow implements WindowListener {
	protected Action aboutAction, exitAction, prefAction, closeAction;
	
	public MainWindow() {
		this(true);
	}
	public MainWindow(boolean quitonclose) {
		super();
		if (Main.OS().addQuit() && quitonclose) {
			addWindowListener(this);
		}
		createActions();
		setJMenuBar(createMenu());
	}
	public void postLoad() {};
	public String getDefaultTitle() {
		return Main.Settings().getTitle();
	}
	public WindowSettings getDefaultWindowSettings() {
		return new WindowSettings(320,160,0,0,true,JFrame.NORMAL);
	}
	public boolean alwaysShow() {
		return false;
	}
	public void windowClosing(WindowEvent e) {
		Main.quit();
	}
	public void windowClosed(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowGainedFocus(WindowEvent e) {}
	public void windowLostFocus(WindowEvent e) {}
	public void windowStateChanged(WindowEvent e) {}
	public void createActions() {
		int shortcutKeyMask = Main.OS().shortCutKey();
		if (Main.OS().addQuit()) {
			aboutAction = new aboutActionClass("About", KeyStroke.getKeyStroke(KeyEvent.VK_B, shortcutKeyMask));
			exitAction = new exitActionClass("Exit", KeyStroke.getKeyStroke(KeyEvent.VK_X, shortcutKeyMask));
			prefAction = new prefActionClass("Options", KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcutKeyMask));
		} else {
			closeAction = new closeActionClass("Close",KeyStroke.getKeyStroke(KeyEvent.VK_W, shortcutKeyMask));
		}
		createOtherActions();
	}
	public void createOtherActions() {}
	public void createFileMenu(JMenu fileMenu) {}
	public void createEditMenu(JMenu editMenu) {}
	public void createViewMenu(JMenu viewMenu) {}
	public void createHelpMenu(JMenu helpMenu) {}
	public void createOtherMenus(JMenuBar mainMenuBar) {}
	public JMenuBar createMenu() {
		JMenuBar mainMenuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
			createFileMenu(fileMenu);
			if (fileMenu.getItemCount() >= 1) {
				fileMenu.addSeparator();
			}
			if (!Main.OS().addQuit()) {
				fileMenu.add(new JMenuItem(closeAction));
			} else {
				fileMenu.add(new JMenuItem(exitAction));	
			}
			mainMenuBar.add(fileMenu);
		JMenu editMenu = new JMenu("Edit");
			createEditMenu(editMenu);
			if (editMenu.getItemCount() >= 1) {
				mainMenuBar.add(editMenu);
			}
		JMenu viewMenu = new JMenu("View");
			createViewMenu(viewMenu);
			if (Main.OS().addQuit()) {
				if (viewMenu.getItemCount() >= 1) {
					viewMenu.addSeparator();
				}
				viewMenu.add(new JMenuItem(prefAction));	
			}
			if (viewMenu.getItemCount() >= 1) {
				mainMenuBar.add(viewMenu);
			}
		createOtherMenus(mainMenuBar);
		JMenu helpMenu = new JMenu("Help");
			if (Main.OS().addQuit()) {
				helpMenu.add(new JMenuItem(aboutAction));	
			}
			mainMenuBar.add(helpMenu);
		return mainMenuBar;
	}
	public class exitActionClass extends AbstractAction {
		public exitActionClass(String text, KeyStroke shortcut) {
			super(text);
			putValue(ACCELERATOR_KEY, shortcut);
		}
		public void actionPerformed(ActionEvent e) {
			Main.quit();
		}
	}
	public class aboutActionClass extends AbstractAction {
		public aboutActionClass(String text, KeyStroke shortcut) {
			super(text);
			putValue(ACCELERATOR_KEY, shortcut);
		}
		public void actionPerformed(ActionEvent e) {
			Main.about();
		}
	}
	public class prefActionClass extends AbstractAction {
		public prefActionClass(String text, KeyStroke shortcut) {
			super(text);
			putValue(ACCELERATOR_KEY, shortcut);
		}
		public void actionPerformed(ActionEvent e) {
			Main.preferences();
		}
	}
	public class closeActionClass extends AbstractAction {
		public closeActionClass(String text, KeyStroke shortcut) {
			super(text);
			putValue(ACCELERATOR_KEY, shortcut);
		}
		public void actionPerformed(ActionEvent e) {
			Main.closeActiveWindow();
		}
	}
}