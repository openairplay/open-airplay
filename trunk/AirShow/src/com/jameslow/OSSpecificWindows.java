package com.jameslow;

import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;

public class OSSpecificWindows extends OSSpecific {

	public OSSpecificWindows() {
		super();
	}
	public void setSettingsDir() {
		//windows 95/98/Me - ?
		//windows nt - \Windows\Profiles
		//windows 2000/2003/XP/Vista - \Documents and Settings\
		settingsdir = System.getenv("APPDATA") + fileSeparator() + appName();
	}
	public void setDocumentsDir() {
		JFileChooser fr = new JFileChooser();
		FileSystemView fw = fr.getFileSystemView();
		docsdir = fw.getDefaultDirectory().getAbsolutePath();
	}
	public void preSwing() {
		//shortcutkey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}
	}
	public void openURL(String url) {
		try {
			Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
		} catch (Exception e) {
			Main.Logger().warning("Could not open url: " + e.getMessage());
		}
	}
	public void openFile(String file) {
		/*
		try {
			Process p = Runtime.getRuntime().exec("explorer.exe " + file);
			//p.waitFor();
		} catch (Exception e) {
			Main.Logger().warning("Could not open file: " + e.getMessage());
		}
		*/
		openURL((new File(file)).toURI().toString());
	}
	public void openFolder(String folder) {
		openFile(FileUtils.GetFolder(folder));
	}
	public void showFile(String file) {
		openFolder(FileUtils.GetFolder(file));
	}
	public boolean killProcess(String name) {
		return true;
	}
	public boolean killProcess(int pid) {
		return true;
	}
}
