package com.jameslow;

import java.awt.*;
import java.awt.event.*;
import java.util.logging.*;
import java.util.List;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;

import com.jameslow.update.*;

public class Main {
	protected static OSSpecific os;
	protected static Settings settings;
	protected static AutoUpdate autoupdate;
	protected static AbstractWindow window;
	protected static AbstractWindow pref;
	protected static List windows = new ArrayList();
	protected static String prefclass;
	protected static String aboutclass;
	protected static Main instance;
	protected static CommandLine cmd;
	
	protected static Logger logger;
	protected static Level templevel;
	static {
		//Use anonymous logger (System.err), until settings read, set initial level to WARNING
		logger = Logger.getAnonymousLogger();
		templevel = logger.getLevel();
		logger.setLevel(Level.WARNING);
	}
	public static ImageIcon AboutImage() {
		ImageIcon image = null;
		String imagename = Settings().getAboutImage();
		if ((imagename != null) && "".compareTo(imagename) != 0 ) {
			image = new ImageIcon();
			try {
				image.setImage(ImageIO.read(Settings().getResourceAsStream(imagename)));
			} catch (Exception e) {
				Logger().warning("About image not found " + imagename + ": " + e.getMessage());
			}
		}
		return image;
	}
	public static void about() {
		Component parent = null;
		if (window.isVisible()) {
			parent = window;
		}
		AboutPanel panel;
		if (aboutclass == null) {
			panel = new AboutPanel();
		} else {
			panel = (AboutPanel) newInstance(aboutclass);
		}
		JOptionPane.showMessageDialog(parent,panel,"About",JOptionPane.INFORMATION_MESSAGE,AboutImage());
	}
	public static void setPref(AbstractWindow window) {
		if (pref != null) {
			removeWindow(pref);
		}
		pref = window;
		addWindow(pref);
	}
	public static void addWindow(AbstractWindow window) {
		windows.add(window);
	}
	public static void removeWindow(AbstractWindow window) {
		windows.remove(window);
	}
	public static List getWindows() {
		return windows;
	}
	public static void closeActiveWindow() {
		for(int i=0; i<windows.size(); i++) {
			try {
				AbstractWindow window = (AbstractWindow)windows.get(i);
				if (window != null && window.isActive()) {
					closeWindow(window);
				}
			} catch (Exception e) {
				logger.warning("Cannot close window: " + e.getMessage());
			}
		}
	}
	public static void closeWindow(AbstractWindow window) {
		if (window.getJustHide()) {
			window.setVisible(false);
		} else {
			if (window.onClose() && instance.onClose(window)) {
				window.setVisible(false);
				removeWindow(window);
			}
		}

	}
	public static void preferences() {
		if (pref == null) {
			JFrame parent = null;
			if (window.isVisible()) {
				parent = window;
			}
			PrefPanel panel;
			if (prefclass == null) {
				panel = new PrefPanel();
			} else {
				panel = (PrefPanel) newInstance(prefclass);
			}
			panel.setParentFrame(parent);
			int result = JOptionPane.showConfirmDialog(parent,panel,"Preferences",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE,new ImageIcon());
			if (result == JOptionPane.OK_OPTION) {
				panel.savePreferences();
			} else {
				
			}
		} else {
			pref.setVisible(true);
		}
	}
	public static void quit() {
		try {
			if (instance.onQuit()) {
				System.exit(0);
			}	
		} catch (Exception e) {
			Logger().severe("Error when quiting: " + e.getMessage());
		}
	}

	public static AbstractWindow Window() {
		return window;
	}
	public static OSSpecific OS() {
		return os;
	}
	public static Settings Settings() {
		return settings;
	}
	public static Logger Logger() {
		return logger;
	}
	
	protected static Object newInstance(String name) {
		try {
			Class clazz = Class.forName(name);
			return clazz.newInstance();
		} catch (Exception e) {
			Logger().severe("Cannot extatiate class " + name + ": " + e.getMessage());
			return null;
		}
	}
	public static Logger initLogger() {
		Logger initlogger = Logger.getLogger(settings.getMainClass());
		initlogger.setLevel(settings.getLogLevel());
		boolean addconsole = false;
		if (settings.getLogToFile()) {
			try {
				Handler handler = new FileHandler(settings.getLogFile(),true);
				handler.setFormatter(new SimpleFormatter());
				initlogger.addHandler(handler);
			} catch (Exception e) {
				Logger().warning("Could not set logfile " + settings.getLogFile() + ": " + e.getMessage());
				addconsole = true;
			}
		}
		addconsole = settings.getLogToConsole() || addconsole;
		if (addconsole) {
			initlogger.addHandler(new ConsoleHandler());
		}
		//restore original log state
		logger.setLevel(templevel);
		templevel = null;
		
		return initlogger;
	}
	
	public Main(String args[]) {
		this(args,null,null,null,null,null,null,null);
	}
	public Main(String args[], String cmd_name, OSSpecific os_name, String settings_name, final String window_name, String logger_name, String about_name, String pref_name) {
		if (cmd == null) {
			cmd = new CommandLine(args);
		}

		if (!cmd.getHelp()) {
			if (os_name == null) {
				os = OSSpecific.getInstance();  
			} else {
				os = (OSSpecific) newInstance(cmd_name);
			}
			if (settings_name == null) {
				settings = new Settings();
			} else {
				settings = (Settings) newInstance(settings_name);
			}
			settings.loadSettings();
			if (logger_name == null) {
				logger = initLogger();
			} else {
				logger = (Logger) newInstance(logger_name);
			}
			Logger().info("Custom logger now in use.");
			
			aboutclass = about_name;
			prefclass = pref_name;
			
			ActionListener update = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					saveUpdateSettings();
				}
			};
			ActionListener cancel = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					cancelUpdate(window_name);
				}
			};
			WindowListener closewindow = new WindowListener() {
				public void windowClosing(WindowEvent e) {
					cancelUpdate(window_name);
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
			};
			autoupdate = new AutoUpdate();
			autoupdate.checkForUpdates(settings.getTitle(), settings.getUpdateUrl(), settings.getVersion(), settings.getBuild(),
					settings.getAllowMinor(), settings.getAllowExperimental(), settings.getAllowAutoUpdate(),
					settings.getMinor(), settings.getExperimental(), settings.getAutoUpdate(),
					update, cancel, closewindow);
		}
	}
	public void cancelUpdate(String window_name) {
		try {
			saveUpdateSettings();
			if (!cmd.getQuiet()) {
				if (window_name == null) {
					window = new MainWindow();
				} else {
					window = (AbstractWindow) newInstance(window_name);
				}
				addWindow(window);
				window.setVisible(window.getWindowSettings().getVisible());
				window.postLoad();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.severe("Could not start application: "+ex.getMessage());
		}
	}
	private static void saveUpdateSettings() {
		settings.setAutoUpdate(autoupdate.getCheckForUpdates());
		settings.setExperimental(autoupdate.getIncludeExperiemental());
		settings.setMinor(autoupdate.getIncludeMinor());
		settings.saveSettings();
	}
	public static void main(String args[]) {
		instance = new Main(args);
	}
	protected boolean onClose(AbstractWindow window) {
		return true;
	}
	protected boolean onQuit() {
		return true;
	}
	public static void showLogError(String msg, String title) {
		showLogError(msg,title,null);
	}
	public static void showLogError(String msg, String title, Exception e) {
		showLogError(msg,title,null,new JFrame());
	}
	public static void showLogError(String msg, String title, Exception e, Component comp) {
		JOptionPane.showMessageDialog(comp,msg,title,JOptionPane.WARNING_MESSAGE);
		Main.Logger().warning(msg+(e == null ? "" : ": "+e.getMessage()));
	}
}