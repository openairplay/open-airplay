package com.jameslow.update;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.text.SimpleDateFormat; 
import javax.swing.*;
import com.jameslow.*;

public class Generator extends JFrame implements WindowListener, ItemListener, ActionListener {
	//Other
	private volatile boolean active = true;
	private String versionpage;
	private boolean experimental;
	private JLabel contentlabel = new JLabel("Enter version information (html ok) here:");
	private JTextArea contenttextarea = new JTextArea();
	private JPanel bottompanel = new JPanel();
	private JScrollPane contentscroll = new JScrollPane(contenttextarea);
	
	private JLabel versionlabel = new JLabel("Enter a unique link for this build here:");
	private JTextField versionpagefield = new JTextField();
	private JCheckBox experientalbutton = new JCheckBox("This is an experimental build",false);
	private JPanel small = new JPanel();
	private JButton donebutton = new JButton("Done");
	
	//Generated
	private String atomfile;
	private String timestamp;
	
	//General
	private String atompath;
	private String appname;
	private String apppage;
	private String applinkbase;
	private String xmllinkbase;

	//Version specific
	private String version;
	private String build;
	private String macdmg;
	private String maczip;
	private String winzip;
	private String otherzip;
	private String sourcezip;

	//Constants
	private static final String AUTOUPDATE = "AutoUpdate";
	private static final String LIMEGREEN = "Limegreen";
	private static final String GENERATOR = "Generator";
	private static final String XMLNS = "xmlns";
	private static final String XMLNS_LIMEGREEN = "xmlns:limegreen";
	private static final String LIMEGREEN_BUILD = "limegreen:build";
	private static final String LIMEGREEN_VERSION = "limegreen:version";
	private static final String LIMEGREEN_EXPERIMENTAL = "limegreen:experimental";
	private static final String LIMEGREEN_XMLNS = "http://code.google.com/p/limegreen";
	private static final String ATOM_FEED = "feed";
	private static final String ATOM_XMLNS = "http://www.w3.org/2005/Atom";
	private static final String ATOM_ENTRY = "entry";
	private static final String ATOM_ID = "id";
	private static final String ATOM_TITLE = "title";
	private static final String ATOM_CONTENT = "content";
	private static final String ATOM_UPDATED = "updated";
	private static final String ATOM_AUTHOR = "author";
	private static final String ATOM_AUTHOR_NAME = "name";
	private static final String ATOM_LINK = "link";
	private static final String ATOM_LINK_HREF = "href";
	private static final String ATOM_LINK_REL = "rel";
	private static final String ATOM_LINK_REL_SELF = "self";
	private static final String ATOM_LINK_REL_ENCLOSURE = "enclosure";
	private static final String ATOM_LINK_LENGTH = "length";
	private static final String ATOM_LINK_TYPE = "type";
	private static final String LIMEGREENBUILD = "limegreenbuild";
	private static final String MIME_ZIP = "application/zip";
	private static final String MIME_DMG = "application/octet-stream";
	private static final String VERSION_SPLIT = "\\.";
	private static final String s = "/";
	private static final String d = ".";
	private static final String VERSION = "%version%";

	public Generator() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			System.setProperty("com.apple.mrj.application.apple.menu.about.name",AUTOUPDATE);
			System.setProperty("apple.laf.useScreenMenuBar","true");
			Font font = new Font("Arial", Font.PLAIN, 11);
			Enumeration keys = UIManager.getDefaults().keys();
			while (keys.hasMoreElements()) {
				Object key = keys.nextElement();
				Object value = UIManager.get(key);
				if (value instanceof javax.swing.plaf.FontUIResource)
					UIManager.put(key,font);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		addWindowListener(this);
		experientalbutton.addItemListener(this);
		donebutton.addActionListener(this);
		setBounds(0, 0, 500, 400);
		Container pane = getContentPane();
		pane.add(contentlabel,BorderLayout.NORTH);
		pane.add(contentscroll,BorderLayout.CENTER);
		bottompanel.setLayout(new GridLayout(2,2));
		bottompanel.add(versionlabel);
		bottompanel.add(versionpagefield);
		bottompanel.add(experientalbutton);
			small.add(donebutton);
		bottompanel.add(small);
		pane.add(bottompanel,BorderLayout.SOUTH);
	}
	public void windowClosing(WindowEvent e) {
		active = false;
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
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == donebutton) {
			active = false;
			hide();
		}
	}
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();
		if (source == experientalbutton) {
			experimental = e.getStateChange() == ItemEvent.SELECTED; 
		}
	}
	
	//General
	public void setAtompath(String atompath) {
		this.atompath = atompath;
	}
	public void setAppname(String appname) {
		this.appname = appname;
	}
	public void setApppage(String apppage) {
		this.apppage = apppage;
	}
	public void setApplinkbase(String applinkbase) {
		this.applinkbase = applinkbase;
	}
	public void setXmllinkbase(String xmllinkbase) {
		this.xmllinkbase = xmllinkbase;
	}
	
	//Version specific
	public void setVersion(String version) {
		this.version = version;
	}
	public void setBuild(String build) {
		this.build = build;
	}
	public void setMacdmg(String macdmg) {
		this.macdmg = macdmg;
	}
	public void setMaczip(String maczip) {
		this.maczip = maczip;
	}
	public void setOtherzip(String otherzip) {
		this.otherzip = otherzip;
	}
	public void setWinzip(String winzip) {
		this.winzip = winzip;
	}
	public void setSourcezip(String sourcezip) {
		this.sourcezip = sourcezip;
	}
	
	public void createXML() {
		//Create general stuff
		XMLHelper helper = new XMLHelper(atompath,ATOM_FEED);
		helper.setAttribute(XMLNS,ATOM_XMLNS);
		helper.setAttribute(XMLNS_LIMEGREEN,LIMEGREEN_XMLNS);
		helper.setValue(ATOM_ID,apppage);
		helper.setValue(ATOM_TITLE,appname+" "+AUTOUPDATE);
		XMLHelper[] links = helper.getSubNodeList(ATOM_LINK);
		boolean doneself = false;
		boolean donepage = false;
		for (int i=0; i < links.length; i++) {
			String rel = links[i].getAttribute(ATOM_LINK_REL);
			if (ATOM_LINK_REL_SELF.compareTo(rel) == 0) {
				links[i].setAttribute(ATOM_LINK_HREF,xmllinkbase+s+atomfile);
				doneself = true;
			} else {
				links[i].setAttribute(ATOM_LINK_HREF,apppage);
				donepage = true;
			}
		}
		if (!doneself) {
			XMLHelper link = helper.createSubNode(ATOM_LINK);
			link.setAttribute(ATOM_LINK_REL,ATOM_LINK_REL_SELF);
			link.setAttribute(ATOM_LINK_HREF,xmllinkbase+s+atomfile);
		}
		if (!donepage) {
			XMLHelper link = helper.createSubNode(ATOM_LINK);
			link.setAttribute(ATOM_LINK_HREF,apppage);
		}
		helper.setValue(ATOM_AUTHOR+d+ATOM_AUTHOR_NAME,LIMEGREEN+" "+AUTOUPDATE+" "+GENERATOR);
		helper.setValue(ATOM_UPDATED,timestamp);

		//Create new entry
		XMLHelper entry = helper.createSubNode(ATOM_ENTRY);
		entry.setValue(ATOM_ID,versionpage);
		entry.setValue(ATOM_TITLE,appname+" "+version+" (build "+build+")");
		entry.setValue(LIMEGREEN_BUILD,""+build);
		entry.setValue(LIMEGREEN_VERSION,""+version);
		entry.setValue(LIMEGREEN_EXPERIMENTAL,""+experimental);
		entry.setValue(ATOM_UPDATED,timestamp);
		entry.setAttribute(ATOM_LINK,ATOM_LINK_HREF,versionpage);
		createZipLink(entry,"Win",winzip);
		createZipLink(entry,"Mac", maczip);
		createZipLink(entry,"Other",otherzip);
		createZipLink(entry,"Source",sourcezip);
		createEnclosureLink(entry,appname+" Mac Disk Image",macdmg,MIME_DMG);
		entry.setValue(ATOM_CONTENT,contenttextarea.getText());
		
		//Save
		helper.save(atompath);
	}
	private void createZipLink(XMLHelper entry, String title, String filepath) {
		createEnclosureLink(entry,appname+" "+title+" Zip",filepath,MIME_ZIP);
	}
	private void createEnclosureLink(XMLHelper entry, String fulltitle, String filepath, String type) {
		File file = new File(filepath);
		if (file.exists() && !file.isDirectory()) {
			XMLHelper link = entry.createSubNode(ATOM_LINK);
			link.setAttribute(ATOM_LINK_REL, ATOM_LINK_REL_ENCLOSURE);
			link.setAttribute(ATOM_LINK_TYPE, type);
			link.setAttribute(ATOM_TITLE, fulltitle);
			link.setAttribute(ATOM_LINK_LENGTH, ""+(new File(filepath)).length());
			link.setAttribute(ATOM_LINK_HREF, applinkbase+s+getFileName(filepath));
		}
	}
	private String getFileName(String filepath) {
		File file = new File(filepath);
		return file.getName();
	}
	private String replaceVersion(String str) {
		return str.replaceAll(VERSION, version);
	}
	public void execute() {
		//derived properties
		xmllinkbase = xmllinkbase != null ? xmllinkbase : applinkbase;
		xmllinkbase = replaceVersion(xmllinkbase);
		applinkbase = replaceVersion(applinkbase);
		atomfile = getFileName(atompath);
		
		//timestamp
		//convert YYYYMMDDTHH:mm:ss+HH00 into YYYYMMDDTHH:mm:ss+HH:00 
		//http://stackoverflow.com/questions/289311/output-rfc-3339-timestamp-in-java
		timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:m:ssZ").format(new Date());
		timestamp = timestamp.substring(0, timestamp.length()-2) + ":" + timestamp.substring(timestamp.length()-2);
		
		//show user form
		String versionpagedefault = apppage+(apppage.lastIndexOf("?")>=0 ? "&" : "?" )+LIMEGREENBUILD+"="+build;
		versionpagefield.setText(versionpagedefault);
		show();
		
		while (active) {
			//this will finish once the user clicks done
		}
		versionpage = versionpagefield.getText();
		if (versionpage == null || "".compareTo(versionpage) == 0) {
			versionpage = versionpagedefault;
		}
		
		createXML();
		System.out.println("AutoUpdate xml generated: " + atomfile);
	}
}