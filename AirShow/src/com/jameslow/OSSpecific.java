package com.jameslow;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import com.jameslow.FileUtils.Filefilter;

//Other:
//ALLUSERPROFILE (XP) /Users/Shared on OSX
//TMP/TEMP

public class OSSpecific {
	private static final String MAIN_BUNDLE = "main";
	private static final String BUILD_BUNDLE = "build";
	//Locale("fr", "CH"): result MyResources_fr_CH.class, parent MyResources_fr.properties, parent MyResources.class 
	//Locale("fr", "FR"): result MyResources_fr.properties, parent MyResources.class 
	//Locale("de", "DE"): result MyResources_en.properties, parent MyResources.class 
	//Locale("en", "US"): result MyResources_en.properties, parent MyResources.class
	//Locale("en", "GB"): result MyResources_en.properties, parent MyResources.class
	//Locale("es", "ES"): result MyResources_es_ES.class, parent MyResources.class
	protected static final ResourceBundle build = ResourceBundle.getBundle(BUILD_BUNDLE, Locale.getDefault());
	protected static final ResourceBundle main = ResourceBundle.getBundle(MAIN_BUNDLE, Locale.getDefault());
	protected static final Properties properties = System.getProperties();
	protected static final String osname = properties.getProperty("os.name").toLowerCase();
	protected static final String javaversion = System.getProperty("java.version");
	
	//Class variables
	protected JFileChooser fc;
	
	//Constants
	protected String homedir;
	protected int shortcutkey;
	protected String fileseparator;
	protected String lineseparator;
	protected String pathseparator;
	protected String appname;
	protected String settingsdir;
	protected String logdir;
	protected String tempdir;
	protected String docsdir;
	protected String libdir;
	
	protected OSSpecific() {
		homedir = properties.getProperty("user.home");
		fileseparator = properties.getProperty("file.separator");
		lineseparator = properties.getProperty("line.separator");
		pathseparator = properties.getProperty("path.separator");
		appname = build.getString("application.name");
		setSettingsDir();
		setNativeLibDir();
		setLogDir();
		setTempDir();
		setDocumentsDir();
		this.preSwing();
			shortcutkey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
			setFont();
			//try {
				//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				//"apple.laf.AquaLookAndFeel"
				//"com.sun.java.swing.plaf.gtk.GTKLookAndFeel" //Linux should return this as the default, but it doesn't
				//"com.sun.java.swing.plaf.motif.MotifLookAndFeel"
				//"com.sun.java.swing.plaf.windows.WindowsLookAndFeel"
				//"com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel"
				//UIManager.getCrossPlatformLookAndFeelClassName();
				//properties.setProperty("swing.defaultlaf","com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
			//} catch (Exception e) { }
		this.postSwing();
		Main.os = this;
	}
	//Statics
	public static OSSpecific getInstance() {
		if (isOSX()) {
			return new OSSpecificOSX();
		} else if (isWindows()) {
			return new OSSpecificWindows();
		} else {
			return new OSSpecific();
		}
	}
	public static boolean isOSX() {
		return osname.startsWith("mac os x"); 
	}
	public static boolean isWindows() {
		return osname.startsWith("windows"); 
	}
	public static boolean isOther() {
		return !isOSX() && !isWindows();
	}
	
	//Callbacks
	public void preSwing() {}
	public void postSwing() {}
	public void setFont() {
		Font font = new Font("Arial", Font.PLAIN, 11);
		Enumeration keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource)
				UIManager.put(key,font);
		}
	}
	public void setSettingsDir() {
		settingsdir = homeDir() + fileSeparator() + "." + appName();
	}
	public void setNativeLibDir() {
		libdir = settingsDir() + fileSeparator() + "lib";
	}
	public void setLogDir() {
		logdir = settingsdir;
	}
	public void setTempDir() {
		tempdir = System.getProperty("java.io.tmpdir");
	}
	public void setDocumentsDir() {
		docsdir = homeDir();
	}
	
	//Helper Functions
	public void openURL(String url) {
		String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
		String browser = null;
		for (int count = 0; count < browsers.length && browser == null; count++) {
			try {
				if (Runtime.getRuntime().exec(new String[] {"which", browsers[count]}).waitFor() == 0) {
					browser = browsers[count];
				}
			} catch (Exception e) {} 
		}
		if (browser != null) {
			try {
				Runtime.getRuntime().exec(new String[] {browser, url});
			} catch (Exception e) {}
		}
	}
	public void openFile(String file) {
		executeProcess(new String[]{"open",file});
	}
	public void openFolder(String folder) {
		executeProcess(new String[]{"open",folder});
	}
	public void showFile(String file) {
		//TODO: Implement For Unix based OS
	}
	public ResourceBundle getMainProps() {
		return main;
	}
	public ResourceBundle getBuildProps() {
		return build;
	}
	
	//Save / Open dialogs
	public File openFileDialog(Frame parent) {
		return openFileDialog(parent,false);
	}
	public File openFileDialog(Frame parent, boolean dirsonly) {
		return openFileDialog(parent,dirsonly,null);
	}
	public File openFileDialog(Frame parent, boolean dirsonly, String title) {
		return openFileDialog(parent,dirsonly,title,null);
	}
	public File openFileDialog(Frame parent, boolean dirsonly, String title, String dir) {
		return openFileDialog(parent,dirsonly,title,dir,new Filefilter[0]);
	}
	public File openFileDialog(Frame parent, boolean dirsonly, String title, String dir, Filefilter[] filters) {
		return saveOpenFileDialog(parent,dirsonly,title,dir,filters,false);
	}
	public boolean dialogShowing() {
		return !(fc == null) && fc.isVisible();
	}
	protected File saveOpenFileDialog(Frame parent, boolean dirsonly, String title, String dir, Filefilter[] filters, boolean save) {
		File file = null;
		if (!dialogShowing()) {
			fc = new JFileChooser(dir);
			if (dirsonly) {
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			}
			for (int i=0; i<filters.length; i++) {
				fc.addChoosableFileFilter(FileUtils.getExtFileFilter(filters[i]));
			}
			if (title != null) {
				fc.setDialogTitle(title);
			}
			if (save) {
				fc.setDialogType(JFileChooser.SAVE_DIALOG);
			}
			int returnVal = fc.showOpenDialog(parent);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = fc.getSelectedFile();
			}
			fc = null;
		}
		return file;
	}
	public File saveFileDialog(Frame parent) {
		return saveFileDialog(parent,null);
	}
	public File saveFileDialog(Frame parent, String title) {
		return saveFileDialog(parent,title,null);
	}
	public File saveFileDialog(Frame parent, String title, String dir) {
		return saveFileDialog(parent,title,dir,new Filefilter[0]);
	}
	public File saveFileDialog(Frame parent, String title, String dir, Filefilter[] filters) {
		return saveOpenFileDialog(parent,false,title,dir,filters,true);
	}
	
	//Some process commands
	public boolean killProcess(String name) {
		String[] args = {"ps","-a"};
		//String[] args = {"ps","-ef"}; This doesn't execute on some verions of java due to security issues
		String result = executeProcessWait(args);
		BufferedReader reader = new BufferedReader(new StringReader(result));
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				if (line.indexOf(name) >= 0) {
					/* For ps -ef
					int i = 0;
					while (i < line.length() && line.substring(i, i+1).trim().length() == 0) {
						i++;
					}
					while (i < line.length() && line.substring(i, i+1).trim().length() != 0) {
						i++;
					}
					while (i < line.length() && line.substring(i, i+1).trim().length() == 0) {
						i++;
					}
					int pid = Integer.parseInt(line.substring(i,line.indexOf(" ",i)).trim());
					*/
					int pid = Integer.parseInt(line.substring(0,line.indexOf(" ")).trim());
					killProcess(pid);
				}
			}
			return true;
		} catch (Exception e) {
			Main.Logger().warning("Could not kill process: "+e.getMessage());
		}
		return false;
	}
	public boolean killProcess(int pid) {
		String[] args = new String[3];
		args[0] = "kill";
		args[1] = "-9";
		args[2] = ""+pid;
		executeProcessWait(args);
		return true;
	}
	public void executeProcess(String[] args) {
		try {
			Process p = Runtime.getRuntime().exec(args);
		} catch (Exception e) {
			Main.Logger().warning("Error executing process: "+e.getMessage());
		}
	}
	public String executeProcessWait(String[] args) {
		return executeProcessWait(args,0);
	}
	public String executeProcessWait(String[] args, int expectedreturn) {
		try {
			Process p = Runtime.getRuntime().exec(args);
			int exitVal = p.waitFor();
			int c;
			StringBuffer buf = new StringBuffer();
			if (exitVal == expectedreturn) {
				InputStream i = p.getInputStream();
				while ((c = i.read()) != -1) {
				    buf.append((char) c);
				}
				if (buf.length() > 0) {
					Main.Logger().info(buf.toString());
				}
				return buf.toString();
			} else {
				InputStream e = p.getErrorStream();
				while ((c = e.read()) != -1) {
				    buf.append((char) c);
				}
				if (buf.length() > 0) {
					Main.Logger().warning(buf.toString());
				}
				return buf.toString();
			}
		} catch (Exception e) {
			Main.Logger().warning("Error executing process: "+e.getMessage());
		}
		return null;
	}
	public String getNativeLibFile(String resource) {
		return nativeLibDir() + fileSeparator() + FileUtils.getFilename(resource);
	}
	public boolean writeNativeLib(String resource) {
		try {
			File dir = new File(nativeLibDir());
			dir.mkdirs();
			InputStream in = Main.Settings().getResourceAsStream(resource);
			File file = new File(getNativeLibFile(resource));
			OutputStream out = new FileOutputStream(file);
			FileUtils.WriteStream(in, out);
			try {
				out.close();
				String[] args = { "chmod", "744",file.getAbsolutePath()};
				String result = executeProcessWait(args);
				return result.length() == 0;
			} catch (Exception e) {
				//Only works on Unix like systems
			}
		} catch (FileNotFoundException e) {
			Main.Logger().warning("Cannot copy native library: " + e.getMessage());
		}
		return false;
	}
	
	//Constants
	public String appName() {
		return appname;
	}
	public int shortCutKey() {
		return shortcutkey;
	}
	public String fileSeparator() {
		return fileseparator;
	}
	public String lineSeparator() {
		return lineseparator;
	}
	public String pathSeparator() {
		return pathseparator;
	}
	public String homeDir() {
		return homedir;
	}
	public String settingsDir() {
		return settingsdir;
	}
	public String logDir() {
		return logdir;
	}
	public String tempDir() {
		return tempdir;
	}
	public String documentsDir() {
		return docsdir;
	}
	public String nativeLibDir() {
		return libdir;
	}
	public boolean addQuit() {
		return true;
	}
	public boolean addIcon() {
		return true;
	}
	public boolean settingsImmediate() {
		return false;
	}
	public int getMenuBar() {
		return 0;
	}
	public boolean javaVersion(char major, char minor) {
		char thismajor = javaversion.charAt(0);
		char thisminor = javaversion.charAt(2);
		char thispoint = javaversion.charAt(4);
		return (thismajor >= major && thisminor >= minor);
	}
}