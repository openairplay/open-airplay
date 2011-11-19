package com.jameslow;

import javax.swing.*;

public class WindowSettings {
	private int width, height, left, top, extended_state;
	private boolean visible;
	
	public WindowSettings() {
		this(320,240,0,0);
	}
	public WindowSettings(int width, int height, int left, int top) {
		this(width,height,left,top,true);
	}
	public WindowSettings(int width, int height, int left, int top, boolean visible) {
		this(width,height,left,top,visible,JFrame.NORMAL);
	}
	public WindowSettings(int width, int height, int left, int top, boolean visible, int extended_state) {
		this.width = width;
		this.height = height;
		this.left = left;
		this.top = top;
		this.visible = visible;
		this.extended_state = extended_state;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public int getLeft() {
		return left;
	}
	public int getTop() {
		return top;
	}
	public boolean getVisible() {
		return visible;
	}
	public int getExtendedState() {
		return extended_state;
	}
	public void setWdith(int width) {
		this.width = width;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public void setLeft(int left) {
		this.left = left;
	}
	public void setTop(int top) {
		this.top = top;
	}
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	public void setExtendedState(int extended_state) {
		this.extended_state = extended_state;
	}
}
