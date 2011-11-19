package com.jameslow;

import java.awt.Color;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.logging.*;
import java.util.prefs.*;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;

import javax.swing.JFrame;

import com.jameslow.update.AutoUpdate;

/**
 * Standard way of doing settings
 * @author James
 */
public class Settings {
	private OSSpecific os;
	private XMLHelper xmlhelper;
	
	private String aboutimage;
	private String abouttext;
	private String abouttextmore;
	private String abouturl;
	private int buildnumber;
	private String builddate;
	private String mainclass;
	private String title;
	private String version;
	private String settingsfile;
	private String logfile;
	
	private Level loglevel;
	private boolean logtoconsole;
	private boolean logtofile;
	
	private String updateurl;
	private boolean minor;
	private boolean experimental;
	private boolean autoupdate;
	private boolean allowminor;
	private boolean allowexperimental;
	private boolean allowautoupdate;
	private boolean usegoogle;
	private String applinkbase;
	private String googleproject;
	
	public static final String HEIGHT = "Height";
	public static final String WIDTH = "Width";
	public static final String TOP = "Top";
	public static final String LEFT = "Left";
	public static final String VISIBLE = "Visible";
	public static final String EXTENDED_STATE = "ExtendedState";
	public static final String WINDOW = "Windows.Window";
	
	public static final String AUTOUPDATE = AutoUpdate.AUTOUPDATE+".AutoUpdate";
	public static final String EXPERIMENTAL = AutoUpdate.AUTOUPDATE+".Experimental";
	public static final String MINOR = AutoUpdate.AUTOUPDATE+".Minor";
	
	public Settings() {
		os = Main.OS();
		loadProperties();
		loadFiles();
		loadCommonSettings();
	}
	public void loadCommonSettings() {
		logtofile = getSetting("Common.Log.ToFile", logtofile);
		logtoconsole = getSetting("Common.Log.ToConsole", logtoconsole);
		try {
			loglevel = Level.parse(getSetting("Common.Log.Level",loglevel.toString()));
		} catch (Exception e) {
			Main.Logger().warning("Log level in settings file not recognised.");
		}
		//TODO: Only save if we we're ok loading settings before, otherwise we loose default?
		//saveSettings();
	}
	public void loadSettings() {}
	public void loadFiles() {
		String dir = os.settingsDir();
        (new File(dir)).mkdirs();
        settingsfile = dir + os.fileSeparator()  + os.appName() + ".xml";
        dir = os.logDir();
        (new File(dir)).mkdirs();
        logfile = dir + os.fileSeparator()  + os.appName() + ".log";
        xmlhelper = new XMLHelper(settingsfile,os.appName());
	}
	public String getLogFile() {
		return logfile;
	}
	public String getSettingsFile() {
		return settingsfile;
	}
	public String getTitle() {
		return title;
	}
	public String getAboutText() {
		return abouttext;
	}
	public String getAboutTextMore() {
		return abouttextmore;
	}
	public String getAboutImage() {
		return aboutimage;
	}
	public int getBuild() {
		return buildnumber;
	}
	public String getVersion() {
		return version;
	}
	public String getBuildDate() {
		return builddate;
	}
	public String getMainClass() {
		return mainclass;
	}
	public String getAboutURL() {
		return abouturl;
	}
	public Level getLogLevel() {
		return loglevel;
	}
	public boolean getLogToConsole() {
		return logtoconsole;
	}
	public boolean getLogToFile() {
		return logtofile;
	}
	private void loadProperties() {
		ResourceBundle build = os.getBuildProps();
			builddate = getProperty(build,"build.date");
			//build number written into file, is incremented by 1;
			buildnumber = checkIntProperty(build,"build.number",1)-1;
			version = getProperty(build,"build.version");
			mainclass = getProperty(build,"main.class");
			title = getProperty(build,"application.name");
			usegoogle = checkBooleanProperty(build,"autoupdate.usegoogle");
			applinkbase = getProperty(build,"autoupdate.applinkbase");
			googleproject = getProperty(build,"google.project");
			//mainclass = readManifest(Attributes.Name.MAIN_CLASS);
			//build = readManifest("Build-Number");
			//version = readManifest("Build-Version");
			
			abouttext = getProperty("about.text",title);
			abouttextmore = getProperty("about.text.more","");
			aboutimage = getProperty("about.image");
			abouturl = getProperty("about.url","");
			logtofile = getBooleanProperty("log.tofile");
			logtoconsole = getBooleanProperty("log.toconsole");
			updateurl = getProperty("update.url");
			if (updateurl == null || "".compareTo(updateurl) == 0) {
				if (usegoogle) {
					updateurl = "http://" + googleproject + ".googlecode.com/files/" + title + ".xml";
				} else {
					updateurl = applinkbase + "/" + title + ".xml";
				}
			}
			minor = getBooleanProperty("update.minor");
			experimental = getBooleanProperty("update.experimental");
			autoupdate = getBooleanProperty("update.autoupdate");
			allowminor = getBooleanProperty("update.allow.minor");
			allowexperimental = getBooleanProperty("update.allow.experimental");
			allowautoupdate = getBooleanProperty("update.allow.autoupdate");
			
			try {
				loglevel = Level.parse(getProperty("log.level","WARNING"));
			} catch (Exception e) {
				//Something very basic has gone wrong, log everything
				Main.Logger().severe("Log level in properties file not recognised.");
				loglevel = Level.ALL;
			}
	}
	public int checkIntProperty(ResourceBundle bundle, String key, int def) {
		try {
			return Integer.parseInt(getProperty(bundle,key));
		} catch (NumberFormatException e) {
			return def;
		}
	}
	public boolean checkBooleanProperty(ResourceBundle bundle, String key) {
		//try {
			return Boolean.parseBoolean(getProperty(bundle,key));
		//} catch (NumberFormatException e) {
		//	return def;
		//}
	}
	public boolean getBooleanProperty(String key) {
		return Boolean.parseBoolean(getProperty(key));
	}
	public int getIntProperty(String key) {
		return Integer.parseInt(getProperty(key));
	}
	public float getFloatProperty(String key) {
		return Float.parseFloat(getProperty(key));
	}
	public String getProperty(String key) {
		return getProperty(key,null);
	}
	public String getProperty(String key, String def) {
		return getProperty(os.getMainProps(),key,def);
	}
	public String getProperty(ResourceBundle bundle, String key) {
		return getProperty(bundle,key,"");
	}
	public String getProperty(ResourceBundle bundle, String key, String def) {
		try {
			return bundle.getString(key);
		} catch (Exception e) {
			Main.Logger().warning("Property Not Found: " + key);
			return def;
		}
	}

	public String[] getPropertyList(String key) {
		try {
			return os.getMainProps().getString(key).split(",");
		} catch (Exception e) {
			Main.Logger().warning("Property Not Found: " + key);
			return new String[0];
		}
	}
	public InputStream getResourceAsStream(String resource) {
		return getClass().getClassLoader().getResourceAsStream(resource);
	}
	public String readManifest(String attribute) {
		try {
			return getManifest().getMainAttributes().getValue(attribute);
		} catch (Exception e) {
			Main.Logger().warning("Cannot get manifest attribute: " + attribute);
			return null;
		}
	}
	public String readManifest(Name name) {
		try {
			return getManifest().getMainAttributes().getValue(name);
		} catch (Exception e) {
			Main.Logger().warning("Cannot get manifest name: " + name);
			return null;
		}
	}
	public Manifest getManifest() {
		Manifest manifest = null;
		try {
			InputStream is = Main.class.getResourceAsStream("/main/main.jar");
			//InputStream is = getResourceAsStream("/main/main.jar");
			JarInputStream jis = new JarInputStream(is);
			manifest = jis.getManifest();
			return manifest;
		} catch (Exception e) {
			try {
				String pathToThisClass = Main.class.getResource("/com/jameslow/Main.class").toString();
		        String manifestPath = pathToThisClass.substring(0, pathToThisClass.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
	        	manifest = new Manifest(new URL(manifestPath).openStream());
			} catch (Exception e2) {
				Main.Logger().warning("Cannot get manifest file: " + e2.getMessage());
				return null;
			}
		}
		return manifest;
	}
	public boolean writeNativeLib(String resource) {
		try {
			File dir = new File(os.nativeLibDir());
			dir.mkdirs();
			InputStream in = getResourceAsStream(resource);
			File file = new File(os.nativeLibDir() + os.fileSeparator() + FileUtils.getFilename(resource));
			OutputStream out = new FileOutputStream(file);
			FileUtils.WriteStream(in, out);
			try {
				out.close();
				String[] args = { "chmod", "744",file.getAbsolutePath()};
				Process p = Runtime.getRuntime().exec(args);
				/*
				StringBuffer buf = new StringBuffer();
				InputStream e = p.getErrorStream();
				int c;
				while ((c = e.read()) != -1) {
				    buf.append((char) c);
				}
				int exitVal = p.waitFor();*/   
			} catch (Exception e) {
				//Only works on Unix like systems
			}
			return true;
		} catch (FileNotFoundException e) {
			Main.Logger().warning("Cannot copy native library: " + e.getMessage());
		}
		return false;
	}
	public void saveSettings() {
		preSaveSettings();
		//TODO: Handle window position saving
		xmlhelper.save(settingsfile);
		postSaveSettings();
	}
	public void preSaveSettings() {}
	public void postSaveSettings() {}
	public String getUpdateUrl() {
		return updateurl;
	}
	public boolean getAllowAutoUpdate() {
		return allowautoupdate;
	}
	public boolean getAllowExperimental() {
		return allowexperimental;
	}
	public boolean getAllowMinor() {
		return allowminor;
	}
	public boolean getAutoUpdate() {
		return getSetting(AUTOUPDATE,autoupdate);
	}
	public boolean getExperimental() {
		return getSetting(EXPERIMENTAL,experimental);
	}
	public boolean getMinor() {
		return getSetting(MINOR,minor);
	}
	public void setAutoUpdate(boolean value) {
		setSetting(AUTOUPDATE,value);
	}
	public void setExperimental(boolean value) {
		setSetting(EXPERIMENTAL,value);
	}
	public void setMinor(boolean value) {
		setSetting(MINOR,value);
	}
	public String getSetting(String key, String def) {
		return xmlhelper.getValue(key, def);
	}
	public int getSetting(String key, int def) {
		return xmlhelper.getValue(key, def);
	}
	public float getSetting(String key, float def) {
		return xmlhelper.getValue(key, def);
	}
	public boolean getSetting(String key, boolean def) {
		return xmlhelper.getValue(key, def);
	}
	public Color getSetting(String key, Color def) {
		return xmlhelper.getValue(key, def);
	}
	public void setSetting(String key, String value) {
		xmlhelper.setValue(key, value);
	}
	public void setSetting(String key, int value) {
		xmlhelper.setValue(key, value);
	}
	public void setSetting(String key, float value) {
		xmlhelper.setValue(key, value);
	}
	public void setSetting(String key, boolean value) {
		xmlhelper.setValue(key, value);
	}
	public void setSetting(String key, Color value) {
		xmlhelper.setValue(key, value);
	}
	public String getSetting64(String key, String def) {
		return Base64Coder.decodeString(xmlhelper.getValue(key, Base64Coder.encodeString(def)));
	}
	public void setSetting64(String key, String value) {
		xmlhelper.setValue(key,Base64Coder.encodeString(value));
	}
	public byte[] getSetting64(String key, byte[] def) {
		return getSetting64(key,def.toString()).getBytes();
	}
	public void setSetting64(String key, byte[] value) {
		xmlhelper.setValue(key,value.toString());
	}
	public XMLHelper getXMLHelper(String key) {
		return xmlhelper.getSubNode(key);
	}
	public XMLHelper getXMLHelperByName(String key, String name) {
		return xmlhelper.getSubNodeByName(key,name);
	}
	public XMLHelper[] getXMLHelpers(String key) {
		return xmlhelper.getSubNodeList(key);
	}
	public XMLHelper getWindowXMLHelper(String classname) {
		return getXMLHelperByName(WINDOW, classname);
	}
	public WindowSettings getWindowSettings(String classname) {
		return getWindowSettings(classname,new WindowSettings());
	}
	public WindowSettings getWindowSettings(String classname, int width, int height, int left,int top, boolean visible, int extended_state) {
		return getWindowSettings(classname,new WindowSettings(width,height,left,top,visible,extended_state));
	}
	public WindowSettings getWindowSettings(String classname, WindowSettings ws) {
		XMLHelper window = getWindowXMLHelper(classname);
		if (!window.getIsNewNode()) {
			ws = new WindowSettings(window.getValue(WIDTH, ws.getWidth()),window.getValue(HEIGHT, ws.getHeight()),window.getValue(LEFT, ws.getLeft()),window.getValue(TOP, ws.getTop()),window.getValue(VISIBLE, ws.getVisible()),window.getValue(EXTENDED_STATE,ws.getExtendedState()));
		}
		setWindowSettings(classname,ws);
		return ws;
	}
	public void setWindowSettings(String classname, int width, int height, int left,int top, boolean visible) {
		XMLHelper window = getWindowXMLHelper(classname);
		window.getSubNode(WIDTH).setValue(width);
		window.getSubNode(HEIGHT).setValue(height);
		window.getSubNode(LEFT).setValue(left);
		window.getSubNode(TOP).setValue(top);
		window.getSubNode(VISIBLE).setValue(visible);
	}
	public void setWindowSettings(String classname, WindowSettings ws) {
		setWindowSettings(classname,ws.getWidth(),ws.getHeight(),ws.getLeft(),ws.getTop(),ws.getVisible());
	}
	public XMLHelper getXMLHelper() {
		return xmlhelper;
	}
}