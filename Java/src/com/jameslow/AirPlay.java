package com.jameslow;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.imageio.*;
import javax.jmdns.*;
import javax.swing.*;
import jargs.gnu.*;

public class AirPlay {
	public static final String DNSSD_TYPE = "_airplay._tcp.local.";
	
	public static final String NONE = "None";
	public static final String SLIDE_LEFT = "SlideLeft";
	public static final String SLIDE_RIGHT = "SlideRight";
	public static final String DISSOLVE = "Dissolve";
	public static final int PORT = 7000;
	
	protected String hostname;
	protected int port;
	protected PhotoThread photothread;
	protected Auth auth;
	
	//AirPlay class
	public AirPlay(Service service) {
		this(service.hostname,service.port);
	}
	public AirPlay(String hostname) {
		this(hostname,PORT);
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
		//TODO: Add authentication for 401, username = Airplay, check for auth otherwise fail
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
			stopPhotoThread();
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
		stopPhotoThread();
		photoRaw(image,transition);
		photothread = new PhotoThread(this,image,5000);
		photothread.start();
	}
	public void photoRaw(BufferedImage image, String transition) throws IOException {
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
	
	public class PhotoThread extends Thread {
		private final AirPlay airplay;
		private BufferedImage image = null;
		private int timeout = 5000;
		
		public PhotoThread(AirPlay airplay) {
			this(airplay,null,1000);
		}
		public PhotoThread(AirPlay airplay, BufferedImage image, int timeout) {
			this.airplay = airplay;
			this.image = image;
			this.timeout = timeout;
		}
		public void run() {
			while (!Thread.interrupted()) {
				try {
					if (image == null) {
						airplay.photoRaw(AirPlay.captureScreen(),NONE);
					} else {
						airplay.photoRaw(image,NONE);
						Thread.sleep(Math.round(0.9*timeout));
					}
				} catch (InterruptedException e) {
					break;
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
		}
	}
	public void stopPhotoThread() {
		if (photothread != null) {
			photothread.interrupt();
			while (photothread.isAlive());
			photothread = null;
		}
	}
	public void desktop() throws AWTException, IOException {
		stopPhotoThread();
		photothread = new PhotoThread(this);
		photothread.start();
	}
	
	public static interface Auth {
		public abstract String getAuth(String hostname, String name);
	}
	
	//Bonjour classes
	public static class Service {
		public String name;
		public String hostname;
		public int port;
		
		public Service(String hostname) {
			this(hostname,PORT);
		}
		public Service(String hostname, int port) {
			this(hostname,port,hostname);
		}
		
		public Service(String hostname, int port, String name) {
			this.hostname = hostname;
			this.port = port;
			this.name = name;
		}
	}
	protected static Service[] formatSearch(ServiceInfo[] services) throws IOException {
		Service[] results = new Service[services.length];
		for (int i = 0; i < services.length; i++) {
			ServiceInfo service = services[i];
			Inet4Address[] addresses = service.getInet4Addresses();
			results[i] = new Service(addresses[0].getHostAddress(), service.getPort(), service.getName());
		}
		return results;
	}
	public static Service[] search() throws IOException {
		return search(6000);
	}
	public static Service[] search(int timeout) throws IOException {
		final JmDNS jmdns = JmDNS.create();
		Service[] results = formatSearch(jmdns.list(DNSSD_TYPE, timeout));
		jmdns.close();
		return results;
	}
	public static AirPlay searchDialog(Window parent) throws IOException {
		return searchDialog(parent, 6000);
	}
	public static AirPlay searchDialog(Window parent, int timeout) throws IOException {
		//TODO: could improve this dialog
		JDialog search = new JDialog(parent, "Searching...");
		search.setVisible(true);
		search.setBounds(0,0,200,100);
		search.setLocationRelativeTo(parent);
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
			String input = (String) JOptionPane.showInputDialog(parent,"","Select AppleTV",JOptionPane.PLAIN_MESSAGE, null,choices,choices[0]);
			if (input != null) {
				int index = -1;
				for (int i = 0; i < choices.length; i++) {
					if (input == choices[i]) {
						index = i;
						break;
					}
				}
				return new AirPlay(services[index]);
			}
			return null;
		}
		throw new IOException("No AppleTVs Found");
	}
	
	// Command line functions
	public static void usage() {
		System.out.println("commands: -s {stop} | -p file {photo} | -d {desktop}");
		System.out.println("java -jar airplay.jar -h hostname[:port] command");
	}
	public static void waitforuser() {
		System.out.println("Press return to quit");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String s;
		try {
			while ((s = in.readLine()) != null && !(s.length() >= 0)) { }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		try {
			CmdLineParser cmd = new CmdLineParser();
			CmdLineParser.Option hostopt = cmd.addStringOption('h',"hostname");
			CmdLineParser.Option stopopt = cmd.addBooleanOption('s',"stop");
			CmdLineParser.Option photoopt = cmd.addStringOption('p',"photo");
			CmdLineParser.Option desktopopt = cmd.addBooleanOption('d',"desktop");
			cmd.parse(args);
			String hostname = (String) cmd.getOptionValue(hostopt);
			
			if (hostname == null) {
				usage();
			} else {
				AirPlay airplay;
				String[] hostport = hostname.split(":",2);
				if (hostport.length > 1) {
					airplay = new AirPlay(hostport[0],Integer.parseInt(hostport[1]));
				} else {
					airplay = new AirPlay(hostport[0]);
				}
				String photo;
				if (cmd.getOptionValue(stopopt) != null) {
					airplay.stop();
				} else if ((photo = (String) cmd.getOptionValue(photoopt)) != null) {
					System.out.println("Press ctrl-c to quit");
					airplay.photo(photo);
				} else if (cmd.getOptionValue(desktopopt) != null) {
					System.out.println("Press ctrl-c to quit");
					airplay.desktop();
				} else {
					usage();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
