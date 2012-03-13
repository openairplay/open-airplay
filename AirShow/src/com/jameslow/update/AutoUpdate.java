package com.jameslow.update;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.apple.eio.FileManager;
import com.jameslow.Main;

//This is a big hack because I'm trying to get this into a single java class
//For some reason I thought that would be easier to copy from the jar to a temp location to launch, and it might be, we'll see
public class AutoUpdate extends Thread implements ActionListener, ItemListener, Comparator, HyperlinkListener {
	//Common
	private static final String s = System.getProperty("file.separator");
	private static final String tempdir = System.getProperty("java.io.tmpdir");
	private static final String fullname = AutoUpdate.class.getName();
	private static final int lastdot = fullname.lastIndexOf(".");
	private static final String pack = fullname.substring(0, lastdot);
	private static final String classname = fullname.substring(lastdot+1);
	private static final String osname = System.getProperty("os.name").toLowerCase();
	private static final boolean isosx = osname.startsWith("mac os x");
	private static final boolean iswindows = osname.startsWith("windows");
	private static final String CLASS = "class";
	
	//Constants
	private static final String LIMEGREEN_BUILD = "limegreen:build";
	private static final String LIMEGREEN_VERSION = "limegreen:version";
	private static final String LIMEGREEN_EXPERIMENTAL = "limegreen:experimental";
	private static final String ATOM_ENTRY = "entry";
	private static final String ATOM_ID = "id";
	private static final String ATOM_TITLE = "title";
	private static final String ATOM_CONTENT = "content";
	private static final String ATOM_UPDATED = "updated";
	private static final String ATOM_LINK = "link";
	private static final String ATOM_LINK_HREF = "href";
	private static final String ATOM_LINK_REL = "rel";
	private static final String ATOM_LINK_ENCLOSURE = "enclosure";
	private static final String ATOM_LINK_LENGTH = "length";
	private static final String ATOM_LINK_TYPE = "type";
	private static final String VERSION_SPLIT = "\\.";
	
	//Stuff associated with checking for new versions
	private JFrame window;
	private JEditorPane editor;
	private JScrollPane scroll;
	private JPanel checkpanel;
		private JCheckBox checkforupdatesbutton;
		private JCheckBox includeexperimentalbutton;
		private JCheckBox includeminorbutton;
		private JButton updatebutton;
		private JButton cancelbutton;
	private JPanel installpanel;
		private JProgressBar progressBar;
		private JButton installbutton;
	private String download;
	private String appname;
	private File downloadfile;
	private boolean autoupdate;
	private boolean includeexperimental;
	private boolean includeminor;
	private boolean allowexperimental;
	private boolean allowminor;
	private ActionListener updatelistener;
	private ActionListener cancellistener;
	private boolean isapp;
	private boolean isexe;
	private String running;
	private int apppos;
	private String lastmsg = "";
	
	//Stuff associated with downloading and extracting update
	public static final String AUTOUPDATE = "AutoUpdate";
	private static final int ARG_COUNT = 3;
	private static final String USAGE = "usage: " + fullname + " download_file copy_target launch_app [delete_first]";
	
	//We're at the checking for updates stage
	//Consuming classes should implement an ActionListener for when the auto update proceeds and cancelled
	//Consuming classes can check if the user has checked check for updates in the future by calling getCheckForUpdates(), this can then be saved as a setting
	public void checkForUpdates(String appname, String url, String version, int build,
				boolean allowminor, boolean allowexperimental, boolean allowautoupdate,
				boolean includeminor, boolean includeexperimental, boolean autoupdate,
				ActionListener update, ActionListener cancel, WindowListener closewindow) {
		if (allowautoupdate && autoupdate) {
			this.autoupdate = autoupdate;
			this.includeexperimental = includeexperimental;
			this.includeminor = includeminor;
			cancellistener = cancel;
			updatelistener = update;
			this.appname = appname;
			this.allowexperimental = allowexperimental;
			this.allowminor = allowminor;
			try {
				String versionxml = getHttpContent(url);
				running = AutoUpdate.class.getProtectionDomain().getCodeSource().getLocation().toURI().toString();
				apppos = running.indexOf(".app/Contents/Resources/Java");
			    isapp = isosx && apppos >= 0;
			    if (isapp) {
			    	isexe = false;
			    } else {
			    	//jsmooth copies jar to a temp location before running, that looks something like this:
			    	isexe = iswindows && Pattern.compile("temp[0-9]+\\.jar").matcher(running).find();
			    }
			    String versioninfo;
			    try {
			    	if ((versioninfo = parseXML(versionxml, version, build)) != null) {
			    		constructWindow(versioninfo,closewindow);
			    	} else {
			    		cancel("Version up to date.");
			    	}
			    } catch (Exception e) {
			    	cancelError("Error parsing XML: "+e.getMessage());
			    }
			} catch (URISyntaxException e) {
				cancelError("Could not get running applcation: "+e.getMessage());
			} catch (IllegalArgumentException e) {
				cancel("Could not connect to server/Not connected to internet: "+e.getMessage());
			} catch (IOException e) {
				//Not connected to the internet or can't contact webpage, just go on
				cancel("Could not connect to server/Not connected to internet: "+e.getMessage());
			}
		}
	}
	private Element getElement(Element element, String tag) {
		NodeList nl = element.getElementsByTagName(tag);
		if (nl != null) {
			if (nl.getLength() > 0) {
				return (Element)nl.item(0);
			}
		}
		return null;	
	}
	private String getTagValue(Element element, String tag) {
		return getTagValue(element,tag,"");
	}
	private String getTagValue(Element element, String tag, String defaultvalue) {
		try {
			Element element2 = getElement(element,tag);
			Node node = element2.getFirstChild();
			if (node == null) {
				return "";
			} else {
				return node.getNodeValue();
			}
		} catch (Exception e) {
			return defaultvalue;
		}
	}
	private int getTagValue(Element element, String tag, int defaultvalue) {
		return Integer.parseInt(getTagValue(element,tag,""+defaultvalue));
	}
	private boolean getTagValue(Element element, String tag, boolean defaultvalue) {
		return Boolean.parseBoolean(getTagValue(element,tag,""+defaultvalue));
	}
	private int[] getTagValue(Element element, String tag, int[] defaultvalue) {
		return getIntArray(getTagValue(element,tag,""+defaultvalue),defaultvalue);
	}
	private int[] getIntArray(String value, int[] defaultvalue) {
		return getIntArray(value.split(VERSION_SPLIT),defaultvalue);
	}
	private int[] getIntArray(String[] values, int[] defaultvalue) {
		int[] result = new int[values.length];
		try {
			for (int i=0; i<values.length; i++){
				result[i] = Integer.parseInt(values[i]);
			}
			return result;
		} catch (Exception e) {
			return defaultvalue;
		}
	}
	private String getAttributeValue(Element element, String attribute) {
		return getAttributeValue(element,attribute,"");
	}
	private String getAttributeValue(Element element, String attribute, String defaultvalue) {
		try {
			return element.getAttribute(attribute);
		} catch (Exception e) {
			return defaultvalue;
		}
	}
	private String getAttributeValue(String tag, Element element, String attribute) {
		return getAttributeValue(getElement(element,tag),attribute);
	}
	private String getAttributeValue(String tag, Element element, String attribute, String defaultvalue) {
		return getAttributeValue(getElement(element,tag),attribute,defaultvalue);
	}
	public int compare(Object o1, Object o2) {
		Element e1 = (Element) o1;
		Element e2 = (Element) o2;
		return compare(e1,e2,true);
	}
	public int compare(Element e1, Element e2, boolean comparebuild) {
		int build1 = getTagValue(e1,LIMEGREEN_BUILD,0);
		int build2 = getTagValue(e2,LIMEGREEN_BUILD,0);
		int[] version1 = getTagValue(e1,LIMEGREEN_VERSION,new int[0]);
		int[] version2 = getTagValue(e2,LIMEGREEN_VERSION,new int[0]);
		return compare(version1,build1,version2,build2,comparebuild);
	}
	public int compare(int[] version1, int build1, Element e2, boolean comparebuild) {
		int build2 = getTagValue(e2,LIMEGREEN_BUILD,0);
		int[] version2 = getTagValue(e2,LIMEGREEN_VERSION,new int[0]);
		return compare(version1,build1,version2,build2,comparebuild);
	}
	public int compare(int[] version1, int build1, int[] version2, int build2, boolean comparebuild) {
		int[] compare1;
		int[] compare2;
		int comparebuild1;
		int comparebuild2;
		boolean swap = false;
		if (version2.length > version1.length) {
			compare1 = version2;
			compare2 = version1;
			comparebuild1 = build2;
			comparebuild2 = build1;
			swap = true;
		} else {
			compare1 = version1;
			compare2 = version2;
			comparebuild1 = build1;
			comparebuild2 = build2;
		}
		for(int i = 0; i<compare1.length; i++){
			if (i<compare2.length) {
				int part1 = compare1[i];
				int part2 = compare2[i];
				if (part1 > part2) {
					return (swap ? 1 : -1);
				} else if (part2 > part1) {
					return (swap ? -1 : 1);
				}
			} else {
				return (swap ? 1 : -1);
			}
		}
		if (comparebuild) { 
			if (comparebuild1 > comparebuild2) {
				return  (swap ? 1 : -1);
			} else if (comparebuild1 < comparebuild2) {
				return  (swap ? -1 : 1);
			}
		}
		return 0;
	}
	private String parseXML(String xversionxml, String version, int build) throws ParserConfigurationException, SAXException, IOException {
		//TODO: For some reasons we don't preserve line feeds in node content
		if ("".compareTo(xversionxml) != 0) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			Document dom = db.parse(new InputSource(new StringReader(xversionxml)));
			Element docEle = dom.getDocumentElement();
			NodeList entries = docEle.getElementsByTagName(ATOM_ENTRY);
			//Go through entries looking for the newest one
			Element newer = null;
			if (entries != null) {
				if (entries.getLength() > 0) {
					for(int i = 0 ; i < entries.getLength();i++) {
						Element entry = (Element)entries.item(i);
						if (!Boolean.parseBoolean(getTagValue(entry, LIMEGREEN_EXPERIMENTAL)) || (includeexperimental)) {
							if (newer == null) {
								//Include minor here, to see if we update if version the same, but builds different
								if (compare(getIntArray(version,new int[0]),build,entry,includeminor) > 0) {
									newer = entry;
								}
							} else {
								//Always get the latest build of version if we're updating
								if (compare(newer,entry,true) > 0) {
									newer = entry;
								}
							}
						}
					}
				}
			}
			if (newer != null) {
				String search;
				if (isapp) {
					//search for -mac- file
					search = "-mac-";
				} else if (isexe) {
					//search for -win- file
					search = "-win-";
				} else {
					//search for -other- file
					search = "-other-";
				}
				NodeList links = newer.getElementsByTagName(ATOM_LINK);
				String thislink = "";
				if (links != null) {
					if (links.getLength() > 0) {
						for(int i = 0 ; i < links.getLength();i++) {
							Element link = (Element)links.item(i);
							String rel = getAttributeValue(link,ATOM_LINK_REL,"");
							String href = getAttributeValue(link,ATOM_LINK_HREF,"");
							if (ATOM_LINK_ENCLOSURE.compareTo(rel) == 0) {
								if (href.lastIndexOf(search) > href.lastIndexOf("/")) {
									download = href;
								}
							} else if ("".compareTo(rel) == 0) {
								thislink = href;
							}
						}
					}
				}
				StringBuffer versionhtml = new StringBuffer();
				if (download != null) {
					versionhtml.append("<html><table><tr><td>");
					versionhtml.append("<h1><font face=Arial>"+getTagValue(newer, ATOM_TITLE)+"</h1>");
					versionhtml.append("<font face=Arial>Link: <a href=\""+thislink+"\">"+thislink+"</a>");
					versionhtml.append("<br><font face=Arial>Version: "+getTagValue(newer,LIMEGREEN_VERSION)+" Build: "+getTagValue(newer,LIMEGREEN_BUILD));
					versionhtml.append("<br><br><font face=Arial>"+getTagValue(newer,ATOM_CONTENT).replaceAll("<br>(\\n|\\r\\n)|(\\n|\\r\\n)<br>|(\\n|\\r\\n)", "<br>\n"));
					versionhtml.append("</td></tr></table></html>");
				}
				return versionhtml.toString();
			}
		}
		return null;
	}
	private void constructWindow(String version, WindowListener closewindow) {
		//Construct window
		int width = 550;
		int height = 350;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Point center = ge.getCenterPoint();
		window = new JFrame(appname + " - " + AUTOUPDATE);
			Container pane = window.getContentPane();
			editor = new JTextPane();
			scroll = new JScrollPane(editor);
				editor.setContentType("text/html");
				editor.setText(version);
				editor.addHyperlinkListener(this);
				editor.setEditable(false);
			pane.add(scroll,BorderLayout.CENTER);
			checkpanel = new JPanel();
				checkforupdatesbutton = new JCheckBox("Check for updates?",autoupdate);
					checkforupdatesbutton.addItemListener(this);
				checkpanel.add(checkforupdatesbutton);
				if (allowexperimental) {
					includeexperimentalbutton = new JCheckBox("Include Experimental?",includeexperimental);
						includeexperimentalbutton.addItemListener(this);
					checkpanel.add(includeexperimentalbutton);
				}
				if (allowminor) {
					includeminorbutton = new JCheckBox("Include Minor?",includeminor);
						includeminorbutton.addItemListener(this);
					checkpanel.add(includeminorbutton);
				}
				cancelbutton = new JButton("Cancel");
					cancelbutton.addActionListener(this);
				checkpanel.add(cancelbutton);
				updatebutton = new JButton("Update");
					updatebutton.addActionListener(this);
				checkpanel.add(updatebutton);
			pane.add(checkpanel,BorderLayout.SOUTH);
			installpanel = new JPanel();
				progressBar = new JProgressBar();
				installpanel.add(progressBar,BorderLayout.CENTER);
				installbutton = new JButton("Install and relaunch");
					installbutton.addActionListener(this);
					installbutton.setEnabled(false);
				installpanel.add(installbutton, BorderLayout.WEST);
		window.setBounds((int) (center.getX() - width/2),(int) (center.getY() - height/2), width, height);
		//window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.addWindowListener(closewindow);
		window.show();
	}
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();
		if (source == checkforupdatesbutton) {
			autoupdate = e.getStateChange() == ItemEvent.SELECTED; 
		} else if (source == includeexperimentalbutton) {
			includeexperimental = e.getStateChange() == ItemEvent.SELECTED;
		} else if (source == includeminorbutton) {
			includeminor = e.getStateChange() == ItemEvent.SELECTED;
		}
	}
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == installbutton) {
			installAndRelaunch(e);
		} else if (source == updatebutton) {
			update();
		} else if (source == cancelbutton) {
			cancelHide(e);
		}
	}
	private void hideWindow() {
		window.hide();
	}
	public boolean getCheckForUpdates() {
		return autoupdate;
	}
	public boolean getIncludeExperiemental() {
		return includeexperimental;
	}
	public boolean getIncludeMinor() {
		return includeminor;
	}
	private void cancelHide(ActionEvent e) {
		hideWindow();
		cancel(new ActionEvent(e.getSource(),e.getID(),"User Cancelled"));
	}
	private void cancelHide(String msg) {
		hideWindow();
		cancel(msg);
	}
	private void cancel(ActionEvent e) {
		cancellistener.actionPerformed(e);
	}
	private void cancel(String msg) {
		//TODO: Not sure if we need to make sure number is correct
		cancel(new ActionEvent(this,0,msg));
	}
	private void cancelError(String msg) {
		Error(msg,window);
		cancel(msg);
	}
	private void update() {
		try {
			Container pane = window.getContentPane();
			pane.remove(checkpanel);
			pane.add(installpanel,BorderLayout.SOUTH);
			this.start();
		} catch (Exception e) {
			cancelHide("Could not launch update program: " + e.getMessage());
		}
	}
	public void run() {
		downloadUpdate();
    }
	
	private void downloadUpdate() {
		final String couldnot = "Autoupdate could not be completed: ";
		try {
			int i = 0;
			//while file exists work out name to download to incrementing suffix
			//TODO: This doesn't work if file is redirect from the download url
			String downloadfilename = download.substring(download.lastIndexOf("/")+1);
			int pos = downloadfilename.lastIndexOf(".");
			while ((downloadfile = new File(tempdir + s + downloadfilename.substring(0,pos) + "-" + i + downloadfilename.substring(pos))).exists()) {
				i++;
			}
			if (downloadfile.createNewFile()) {
				getHttpContent(download, new FileOutputStream(downloadfile));
				//TODO: check file integrity against size / MD5
				installbutton.setEnabled(true);
			} else {
				cancelHide(couldnot + "Could not create temporary file.");
			}
		} catch (IOException e) {
			cancelHide(couldnot + e.getMessage());
		}
	}
	private String myReplace(String subject, String regex, String with) {
		String[] split = subject.split(regex);
		String full="";
		for (int i=0; i<split.length; i++) {
			if (full.compareTo("") == 0) {
				full = split[i];
			} else {
				full = full + with + split[i];
			}
		}
		return full;
	}
	private void installAndRelaunch(ActionEvent e) {
		try {
			final String p = " ";
			hideWindow();
			updatelistener.actionPerformed(e);
			File dir = new File(tempdir + s + myReplace(pack,"\\.",s));
			dir.mkdirs();
			String classfile = dir.toString()+s+classname;
			File file = new File(classfile+"."+CLASS);
			InputStream is = AutoUpdate.class.getResourceAsStream("/"+fullname.replaceAll("\\.", "/")+ "."+CLASS);
			copyInputStream(is,new FileOutputStream(file));
			String deploy;
			String launch;
			
			String us = "/"; //URI seperator
			if (isapp) {
				//jar:file:/Applications/Template.app/Contents/Resources/Java/Template.jar!/main/main.jar
				deploy = new File(myReplace(running.substring(running.indexOf(us),apppos+".app".length()),"%20"," ")).getAbsolutePath();
				launch = deploy.substring(deploy.lastIndexOf(us)+1);
			} else if (isexe) {
				//jar:file:/C:/Users/{Username}/AppData/Local/Temp/temp0.jar!/main/main.jar
				deploy = System.getProperty("EXEPATH");
				if (deploy == null || "".compareTo(deploy) == 0) {
					deploy = System.getProperty("user.dir");
				}
				launch = System.getProperty("EXENAME");
				if (launch == null || "".compareTo(launch) == 0) {
					launch = appname+".exe";
				}
			} else {
				//jar:file:/Users/James/Documents/Programs/James/Eclipse/Template/build/dist/Template.jar!/main/main.jar
				int exclaim = running.lastIndexOf("!");
				int last;
				if (exclaim >= 0) {
					last = running.lastIndexOf(us, exclaim);
				} else {
					last = running.lastIndexOf(us);
				}
				deploy = new File(myReplace(running.substring(running.indexOf(us),last),"%20"," ")).getAbsolutePath();
				launch = running.substring(last+1, exclaim);
			}
			String[] cmd = new String[5];
			cmd[0] = "java";
			cmd[1] = fullname;
			cmd[2] = downloadfile.getAbsolutePath();
			cmd[3] = deploy;
			cmd[4] = launch;
			Runtime.getRuntime().exec(cmd,null,new File(tempdir));
			//Force quit here, then updatelistener only needs to handle things like saving settings
			System.exit(0);
		} catch (Exception ex) {
			cancelHide("Could not install or relaunch: " + ex.getMessage());
		}
	}
	private void getHttpContent(String url, OutputStream out) throws IOException {
		URL u = new URL(url); 
		HttpURLConnection huc = (HttpURLConnection) u.openConnection();
		huc.setRequestMethod("GET"); 
		huc.connect(); 
		int code = huc.getResponseCode();
		if (code >= 200 && code < 300) {
			int length = huc.getContentLength();
			copyInputStream(huc.getInputStream(), out, length);
			out.close();
		} else {
			throw new IOException("Response code is "+code);
		}
		huc.disconnect();
	}
	private String getHttpContent(String url) throws IOException {
		URL u = new URL(url); 
		HttpURLConnection huc = (HttpURLConnection) u.openConnection();
		huc.setRequestMethod("GET"); 
		huc.connect(); 
		int code = huc.getResponseCode();
		StringBuffer result = new StringBuffer();
		if (code >= 200 && code < 300) {
			BufferedReader in = new BufferedReader(new InputStreamReader(huc.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result.append(line);
			}
		} else {
			throw new IOException("Response code is "+code);
		}
		huc.disconnect();
		return result.toString();
	}
	private void copyInputStream(InputStream in, OutputStream out, int length) throws IOException {
		byte[] buffer = new byte[1024];
		int len;
		progressBar.setMaximum(length);
		int total = 0;
		while((len = in.read(buffer)) >= 0) {
			out.write(buffer, 0, len);
			total = total + len;
			progressBar.setValue(total);
			progressBar.setStringPainted(true);
		}
		in.close();
		out.close();
	}	
	
	//We're at the download and extracting stage
	public static void main(String[] args) {
		try {
			System.setProperty("com.apple.mrj.application.apple.menu.about.name",AUTOUPDATE);
			System.setProperty("apple.laf.useScreenMenuBar","true");
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (args.length >= ARG_COUNT) {
			String filename = args[0];
			String copylocation = args[1];
			String launchapp = args[2];
			boolean deletefirst = false;
			int mode = 0;
			if (args.length > ARG_COUNT) {
				try {
					mode = Integer.parseInt(args[3]);
					deletefirst = mode > 0;
				} catch (NumberFormatException e) {
					Error(USAGE + " - delete_first must be a number");
				}
			}
			File downloadfile = new File(filename);
			downloadfile.deleteOnExit();
			Pause(3);
			unzip(downloadfile,copylocation,deletefirst);
			downloadfile.delete();
			if (!launchApplication(copylocation,launchapp)) {
				Error("Could not relaunch application.");
			}
		} else {
			Error(USAGE);
		}
		System.exit(0);
	}
	private static boolean launchApplication(String path, String application) {
		try {
			if (application.endsWith(".jar")) {
				if (isosx) {
					String[] cmd = {"open",path+s+application};
					Runtime.getRuntime().exec(cmd).waitFor();
				} else {
					String[] cmd = {"java","-jar",path+s+application};
					Runtime.getRuntime().exec(cmd).waitFor();
				}
			} else {
				if (isosx) {
					String[] cmd = {"open","-a",path};
					Runtime.getRuntime().exec(cmd).waitFor();
				} else if (iswindows) {
					String[] cmd = new String[1];
					if (path.endsWith(s)) {
						cmd[0] = path+application;
					} else {
						cmd[0] = path+s+application;
					}
					Runtime.getRuntime().exec(cmd).waitFor();
				} else {
					String[] cmd = {"java","-jar",path+s+application};
					Runtime.getRuntime().exec(cmd).waitFor();
				}
			}
			return true;
		} catch (Exception e) {
			Error("Could not launch application: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	private static void createEntry(ZipFile zipFile, ZipEntry entry, String outdir, String prefix) throws IOException {
		String name = entry.getName();
		if (prefix != null) {
			name = name.substring(prefix.length());
		}
		//Error(name);
		name = outdir + s + name;
		//Error(name);
		if(entry.isDirectory()) {
			//Assume directories are stored parents first then children.
			//This is not robust, just for demonstration purposes.
			(new File(outdir, name)).mkdir();
		} else {
			copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(name)));
		}
	}
	private static boolean unzip(File infile, String outdir, boolean deletefirst) {
		//TODO: delete all in directory, maybe we should have a delete first, accept for .app on OSX
		Enumeration entries;
		ZipFile zipFile;
		try {
			zipFile = new ZipFile(infile);
			
			//Scan through all to see if they're inside a single directory
			entries = zipFile.entries();
			ZipEntry first = (ZipEntry)entries.nextElement();
			String firstname = null;
			if (first.isDirectory()) {
				firstname = first.getName();
				while(entries.hasMoreElements()) {
					ZipEntry entry = (ZipEntry)entries.nextElement();
					if (!entry.getName().startsWith(firstname)) {
						firstname = null;
						break;
					}
				}
			}

			//Extract zip
			entries = zipFile.entries();
			first = (ZipEntry)entries.nextElement();
			if (firstname == null) {
				createEntry(zipFile, first, outdir, firstname);
			}
			while(entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry)entries.nextElement();
				createEntry(zipFile, entry, outdir, firstname);
			}
			zipFile.close();
			return true;
		} catch (IOException e) {
			Error("Could not extract zip file: "+e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	private static void copyInputStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len;
		int total = 0;
		while((len = in.read(buffer)) >= 0) {
			out.write(buffer, 0, len);
		}
		in.close();
		out.close();
	}
	
	//Common
	public static void Error(String msg) {
		Error(msg,null);
	}
	public static void Error(String msg, Component parent) {
		JOptionPane.showMessageDialog((parent == null ? new JFrame() : parent), msg);
	}
	public static void Pause(long s) {
		try {
			Thread.sleep(s * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public void hyperlinkUpdate(HyperlinkEvent e) {
		try {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				String url = e.getURL().toString();
				if (isosx) {
					FileManager.openURL(url);
				} else if(iswindows) {
					Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
				} else {
					String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
					String browser = null;
					for (int count = 0; count < browsers.length && browser == null; count++) {
						if (Runtime.getRuntime().exec(new String[] {"which", browsers[count]}).waitFor() == 0) {
							browser = browsers[count];
						}
					}
					if (browser != null) {
						Runtime.getRuntime().exec(new String[] {browser, url});
					}
				}
			}
		} catch (Exception ex) {

		}
	}
}