package com.jameslow.gui;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import javax.imageio.*;
import javax.swing.border.*;

public class ImagePane extends JPanel {
	private CentredBackgroundBorder background = new CentredBackgroundBorder();
	private boolean showimage = true;
	public static final int DRAW_MODE_SCALE = 0;
	public static final int DRAW_MODE_ASPECT = 1;
	public static final int DRAW_MODE_WIDTH = 2;
	public static final int DRAW_MODE_HEIGHT = 3;
	private int drawmode = DRAW_MODE_SCALE;

	public ImagePane() {
		super();
		setBorder(background);
	}	
	public ImagePane(BufferedImage image) {
		this();
		setImage(image);
	}
	public ImagePane(String filename) throws IOException  {
		this();
		setImage(filename);
	}
	public void setImage(BufferedImage image) {
		background.setImage(image);
		repaint();
	}
	public void setImage(String filename) throws IOException {
		if (filename != null) {
			if ("".compareTo(filename) != 0) {
				background.setImage(filename);
			}
		}
		repaint();
	}
	public int getDrawMode() {
		return drawmode;
	}
	public void setDrawMode(int drawmode) {
		this.drawmode = drawmode; 
	}
	public boolean getShowImage() {
		return showimage;
	}
	public void setShowImage(boolean value) {
		showimage = value;
		repaint();
	}
	private class CentredBackgroundBorder implements Border {
		private BufferedImage image;
		public void setImage(BufferedImage image) {
			this.image = image;
		}
		public void setImage(String filename) throws IOException {
			setImage(ImageIO.read(new File(filename)));
		}
		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			if (image != null && showimage) {
				//TODO: add ability to scale maintaining aspect ratio fitting to size of parent
				//TODO: Need to add not zoom all the way, but zoom to a box of aspect ratio 4/3
				Graphics2D g2 = (Graphics2D) g;
				AffineTransform tran = new AffineTransform(1f,0f,0f,1f,0,0);
				float widthscale = (float)c.getWidth()/image.getWidth();
				float heightscale = (float)c.getHeight()/image.getHeight();
				switch (drawmode) {
					case DRAW_MODE_ASPECT:
						float scale;
						if (widthscale < heightscale) {
							scale = widthscale;
						} else {
							scale = heightscale;
						}
						tran.scale(scale,scale);
						g2.drawImage(image, new AffineTransformOp(tran, AffineTransformOp.TYPE_BILINEAR), (int)(c.getWidth() - image.getWidth()*scale)/2, (int)(c.getHeight() - image.getHeight()*scale)/2);
					break;
		            case DRAW_MODE_WIDTH:
		            	tran.scale(widthscale,widthscale);
		            	g2.drawImage(image, tran, null);
		            	break;
		            case DRAW_MODE_HEIGHT:
		            	tran.scale(heightscale,heightscale);
		            	g2.drawImage(image, tran, null);
		            break;
		            default:
		            	tran.scale(widthscale,heightscale);
		            	g2.drawImage(image, tran, null);
		            break;
				}
			}
		}
		public Insets getBorderInsets(Component c) {
			return new Insets(0,0,0,0);
		}
		public boolean isBorderOpaque() {
			return true;
		}
	}
}
