package com.jameslow;

import java.awt.*;

import javax.swing.*;

public class AboutPanel extends JPanel {
	private static final String BR = "<br>";
	public AboutPanel() {
		super();
		Font font = new Font("Arial", Font.PLAIN, 10);
		setFont(font);
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		Dimension size = new Dimension(200,150); 
		setMinimumSize(size);
		setPreferredSize(size);
		setMaximumSize(size);
		//Box.createRigidArea(size)
		Settings settings = Main.Settings();
		String string = new String("<html><body style=\"font-family:Arial;font-size:9px;\">");
		string = string + "<h2>" + settings.getTitle() + "</h2>";
		string = string + BR + settings.getAboutText() + BR;
		string = string + BR + "Version: " + settings.getVersion();
		string = string + BR + "Build: " + settings.getBuild();
		string = string + BR + "Date: " + settings.getBuildDate();
		string = string + BR + settings.getAboutTextMore();
		string = string + "</body></html>";
		//JLabel text = new JLabel(string);
		JTextPane text = new JTextPane();
		text.setContentType("text/html");
		text.setFont(font);
		text.setText(string);
		text.setEditable(false);
		JScrollPane scroll = new JScrollPane(text);
		add(scroll);
		text.setSelectionStart(0);
		text.setSelectionEnd(0);
		Link link = new Link(settings.getAboutURL(),settings.getAboutURL());
		add(link);
	}
}
