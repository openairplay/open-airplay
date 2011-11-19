package com.jameslow;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.imageio.*;
import javax.swing.*;

public class GUIUtils {
	public static void addPopupListener(JComponent comp, List list) {
		final JPopupMenu popup = new JPopupMenu();
		for(int i=0; i < list.size(); i++) {
			JMenuItem menuItem = (JMenuItem) list.get(i);
			popup.add(menuItem);
		}
		comp.add(popup);
		comp.addMouseListener(new MouseAdapter() {
		    public void mousePressed(MouseEvent e) {
		        maybeShowPopup(e);
		    }
		    public void mouseReleased(MouseEvent e) {
		        maybeShowPopup(e);
		    }
		    private void maybeShowPopup(MouseEvent e) {
		        if (e.isPopupTrigger()) {
		            popup.show(e.getComponent(), e.getX(), e.getY());
		        }
		    }
		});
	}
	public static BufferedImage componentToImage(JComponent comp) throws IOException {
		int w = comp.getWidth(), h = comp.getHeight();
		BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = image.createGraphics();
		comp.paint(g2);
		g2.dispose();
		return image;
	}
	public static void componentToImage(JComponent comp, String filename) throws IOException {
		int w = comp.getWidth(), h = comp.getHeight();
		BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = image.createGraphics();
		comp.paint(g2);
		g2.dispose();
		ImageIO.write(image, "jpeg", new File(filename));
	}
}
