package com.jameslow;

import org.apache.tools.ant.*;

public class Substring extends Task {
	private int startindex;
	private int endindex;
	private String property;
	private String string;

	public void execute() throws BuildException {
		if (endindex < 0) {
			getProject().setProperty(property, string.substring(startindex));
		} else {
			getProject().setProperty(property, string.substring(startindex,endindex));
		}
	}

	public void setStartIndex(int startindex) {
		this.startindex = startindex;
	}

	public void setEndIndex(int endindex) {
		this.endindex = endindex;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public void setString(String string) {
		this.string = string;
	}

}