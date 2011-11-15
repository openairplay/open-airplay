package com.jameslow;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.imageio.*;
import javax.jmdns.*;
import javax.swing.*;

public class AirPlay {
	public static final String DNSSD_TYPE = "_airplay._tcp.local.";
	
	public static final String NONE = "None";
	public static final String SLIDE_LEFT = "SlideLeft";
	public static final String SLIDE_RIGHT = "SlideRight";
	public static final String DISSOLVE = "Dissolve";
	
	protected String hostname;
	protected int port;
	protected Thread screenthread;

	protected static AirPlayService[] formatSearch(ServiceInfo[] services) throws IOException {
		AirPlayService[] results = new AirPlayService[services.length];
		for (int i = 0; i < services.length; i++) {
			ServiceInfo service = services[i];
			Inet4Address[] addresses = service.getInet4Addresses();
			results[i] = new AirPlayService(addresses[0].getHostAddress(), service.getPort(), service.getName());
		}
		return results;
	}
	public static AirPlayService[] search() throws IOException {
		return search(6000);
	}
	public static AirPlayService[] search(int timeout) throws IOException {
		final JmDNS jmdns = JmDNS.create();
		AirPlayService[] results = formatSearch(jmdns.list(DNSSD_TYPE, timeout));
		jmdns.close();
		return results;
	}
	public AirPlay(String hostname) {
		this(hostname,7000);
	}
	public AirPlay(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;
	}
	protected String doHTTP(String method, String uri) throws IOException {
		return doHTTP(method, uri, null);
	}
	protected String doHTTP(String method, String uri, ByteArrayOutputStream os) throws IOException {
		return doHTTP(method, uri, os, null);
	}
	protected String doHTTP(String method, String uri, ByteArrayOutputStream os, Map headers) throws IOException {
		//TODO: Need to make sure this keeps alive
		URL url = null;
		try {
			url = new URL("http://"+hostname+":"+port+uri);
		} catch(MalformedURLException e) { }
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setUseCaches(false);
		conn.setDoOutput(true);
		conn.setRequestMethod(method);
		
		if (headers != null) {
			conn.setRequestProperty("User-Agent","MediaControl/1.0");
			Object[] keys = headers.keySet().toArray();
			for (int i = 0; i < keys.length; i++) {
				conn.setRequestProperty((String) keys[i],(String) headers.get(keys[i]));
			}
		}
		
		if (os != null) {
			byte[] data = os.toByteArray();
			conn.setRequestProperty("Content-Length",""+data.length);
		}
		conn.connect();
		if (os != null) {
			os.writeTo(conn.getOutputStream());
			os.flush();
			os.close();
		}
		
		//TODO: Only readback Content-Length?
		//conn.getHeaderField("Content-Length");
		InputStream is = conn.getInputStream();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		String line;
		StringBuffer response = new StringBuffer(); 
		while((line = rd.readLine()) != null) {
			response.append(line);
			response.append("\r\n");
		}
		rd.close();
		return response.toString();
	}
	public void stop() {
		try {
			stopScreen();
			doHTTP("POST", "/stop");
		} catch (Exception e) { }
	}
	public void photo(String filename) throws IOException {
		this.photo(filename,NONE);
	}
	public void photo(String filename, String transition) throws IOException {
		this.photo(new File(filename),transition);
	}
	public void photo(File imagefile) throws IOException {
		this.photo(imagefile,NONE);
	}
	public void photo(File imagefile, String transition) throws IOException {
		BufferedImage image = ImageIO.read(imagefile);
		photo(image,transition);
	}
	public void photo(BufferedImage image) throws IOException {
		this.photo(image,NONE);
	}
	public void photo(BufferedImage image, String transition) throws IOException {
		Map headers = new HashMap();
		headers.put("X-Apple-Transition",transition);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		boolean resultWrite = ImageIO.write(image, "PNG", os);
		/* TODO: Could adjust quality
		 * http://www.java.net/node/689678
		 * http://www.exampledepot.com/egs/javax.imageio/JpegWrite.html
		 * http://www.sussmanprejza.com/ar/card/DataUpload.java
		 */
		doHTTP("PUT", "/photo", os, headers);
	}
	public static BufferedImage captureScreen() throws AWTException {
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension dim = tk.getScreenSize();
		Rectangle rect = new Rectangle(dim);
		Robot robot = new Robot();
		BufferedImage image = robot.createScreenCapture(rect);
		return image;
	}
	
	public class ScreenThread extends Thread {
		final AirPlay airplay;
		public ScreenThread(AirPlay airplay) {
			this.airplay = airplay;
		}
		public void run() {
			while (!Thread.interrupted()) {
				try {
					airplay.photo(AirPlay.captureScreen());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void stopScreen() {
		if (screenthread != null) {
			screenthread.interrupt();
			screenthread = null;
		}
	}
	public void screen() throws AWTException, IOException {
		stopScreen();
		screenthread = new ScreenThread(this);
		screenthread.start();
	}
	
	public static void main(String[] args) {
		try {
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
