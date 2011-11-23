package com.jameslow.airshow;

import java.awt.AWTException;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import com.jameslow.*;

public class AirShowWindow extends MainWindow implements ActionListener {
	protected AirPlay airplay;
	protected JButton start, stop, browse;
	protected JComboBox type;
	protected JComboBox transition;
	protected JLabel filelabel;
	protected JTextField fileurl;
	protected String[] types = {"Desktop","Photos","Video"};
	
	public AirShowWindow() {
		super();
		setResizable(false);
		getContentPane().setLayout(null);
		
		Map transitions = new LinkedHashMap();
		transitions.put("None",AirPlay.NONE);
		transitions.put("Slide Left",AirPlay.SLIDE_LEFT);
		transitions.put("Slide Right",AirPlay.SLIDE_RIGHT);
		transitions.put("Dissolve",AirPlay.DISSOLVE);
		
		//First row
		type = new JComboBox(types);
			type.setBounds(10,10,120,30);
		add(type);
		transition = new JComboBox(transitions.keySet().toArray());
			transition.setBounds(130,10,120,30);
		add(transition);
		start = new JButton("Start");
			start.setBounds(250,10,100,30);
			start.addActionListener(this);
		add(start);
		stop = new JButton("Stop");
			stop.setBounds(350,10,100,30);
			stop.addActionListener(this);
		add(stop);
		
		//Second row
		filelabel = new JLabel("File or URL:");
			filelabel.setHorizontalTextPosition(JLabel.RIGHT);
			filelabel.setBounds(15,50,120,30);
		add(filelabel);
		fileurl = new JTextField();
			fileurl.setBounds(130,50,220,30);
		add(fileurl);
		browse = new JButton("Browse");
			browse.setBounds(350,50,100,30);
			browse.addActionListener(this);
		add(browse);
	}
	public WindowSettings getDefaultWindowSettings() {
		return new WindowSettings(470,160,0,0,true,JFrame.NORMAL);
	}
	
	public void startAirPlay() {
		stopAirPlay();
		if (type.getSelectedIndex() == 0) {
			try {
				//Search for airplay services
				JDialog search = new JDialog(this, "Searching...");
				search.setVisible(true);
				search.setBounds(0,0,200,100);
				search.setLocationRelativeTo(this);
				search.toFront();
				search.setVisible(true);
				AirPlay.Service[] services = AirPlay.search();
				search.setVisible(false);
				if (services.length > 0) {
					//Choose AppleTV
					String[] choices = new String[services.length];
					for (int i = 0; i < services.length; i++) {
						choices[i] = services[i].name + " (" + services[i].hostname + ")"; 
					}
					String input = (String) JOptionPane.showInputDialog(this,"","Select AppleTV",JOptionPane.PLAIN_MESSAGE, null,choices,choices[0]);
					if (input != null) {
						int index = -1;
						for (int i = 0; i < choices.length; i++) {
							if (input == choices[i]) {
								index = i;
								break;
							}
						}
						if (index >= 0) {
							AirPlay.Service service = services[index];
							airplay = new AirPlay(service);
							try {
								airplay.desktop();
							} catch (Exception e) {
								error(e);
							}
						}
					}
				} else {
					JOptionPane.showMessageDialog(this,"No AppleTVs found, please make sure you're connected to a network.","Not found",JOptionPane.PLAIN_MESSAGE);
				}
			} catch (IOException e) {
				error(e);
			}
		} else {
			JOptionPane.showMessageDialog(this,"This feature is not yet implemented, but will be in a future version.","Not implemented",JOptionPane.PLAIN_MESSAGE);
		}
	}
	public void error(Exception e) {
		JOptionPane.showMessageDialog(this,"An error occured: "+e.getMessage(),"Error",JOptionPane.WARNING_MESSAGE);
	}
	public void stopAirPlay() {
		if (airplay != null) {
			airplay.stop();
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == start) {
			startAirPlay();
		} else if (e.getSource() == stop) {
			stopAirPlay();
		} else if (e.getSource() == browse) {
			System.out.println("browse");
		}
	}
}