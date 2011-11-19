package com.jameslow;

import java.awt.Frame;

import javax.swing.JPanel;

public class PrefPanel extends JPanel {
	Frame parent;
	
	public void setParentFrame(Frame parent) {
		this.parent = parent;
	}
	public Frame getParentFrame() {
		return parent;
	}
	public void savePreferences() {}
}
