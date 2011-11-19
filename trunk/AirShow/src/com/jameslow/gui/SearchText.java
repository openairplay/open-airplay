package com.jameslow.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import com.jameslow.*;

public class SearchText extends JTextField implements KeyListener {
	private ImagePane imagepanel;
	private BufferedImage imageon, imageoff;
	private SearchTextFilter filter;
	private boolean filterimmediate;
	
	public SearchText(boolean filterimmediate, SearchTextFilter filter) {
		setLayout(new BorderLayout());
		addKeyListener(this);
		this.filter = filter;
		this.filterimmediate = filterimmediate;
		JPanel panel = new JPanel();
			panel.setLayout(null);
			panel.setOpaque(false);
			try {
				imageoff = ImageIO.read(Main.Settings().getResourceAsStream("images/crossoff.gif"));
				imageon = ImageIO.read(Main.Settings().getResourceAsStream("images/crosson.gif"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			panel.setPreferredSize(new Dimension(imageon.getWidth(),imageon.getHeight()));
			imagepanel = new ImagePane();
				imagepanel.setOpaque(false);
				imagepanel.setImage(imageoff);
				imagepanel.setPreferredSize(new Dimension(imageon.getWidth(),imageon.getHeight()));
				imagepanel.setVisible(false);						
				final JTextField parent = this;
				imagepanel.addMouseListener(new MouseListener() {
					public void mouseClicked(MouseEvent e) {
						parent.setText("");
						imagepanel.setVisible(false);
						filterText();
					}
					public void mouseEntered(MouseEvent e) {
						imagepanel.setImage(imageon);
					}
					public void mouseExited(MouseEvent e) {
						imagepanel.setImage(imageoff);
					}
					public void mousePressed(MouseEvent e) {}
					public void mouseReleased(MouseEvent e) {}
				});
				panel.add(imagepanel);
				imagepanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				imagepanel.setBounds(0 + panel.getInsets().left, 0 + panel.getInsets().right, imageon.getWidth(), imageon.getHeight());
		add(panel, BorderLayout.EAST);
	}
	public void keyTyped(KeyEvent e) {}
	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			setText("");
		}
		if (getText().compareTo("") == 0) {
			imagepanel.setVisible(false);
		} else {
			imagepanel.setVisible(true);
		}
		if (filterimmediate || e.getKeyCode() == KeyEvent.VK_ENTER) {
			filterText();
		}
	}
	public void filterText() {
		if (filter != null) {
			filter.doFilter(getText());
		}
	}

}
