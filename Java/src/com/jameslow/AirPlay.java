package com.jameslow;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.security.*;
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
	public static final String USERNAME = "Airplay";
	public static final int PORT = 7000;
	public static final int APPLETV_WIDTH = 1280;
	public static final int APPLETV_HEIGHT = 720;
	public static final float APPLETV_ASPECT = (float) APPLETV_WIDTH/APPLETV_HEIGHT;
	
	protected String hostname;
	protected String name;
	protected int port;
	protected PhotoThread photothread;
	protected String password;
	protected String authorization;
	protected Auth auth;
	protected int appletv_width = APPLETV_WIDTH;
	protected int appletv_height = APPLETV_HEIGHT;
	protected float appletv_aspect = APPLETV_ASPECT;
	
	//AirPlay class
	public AirPlay(Service service) {
		this(service.hostname,service.port,service.name);
	}
	public AirPlay(String hostname) {
		this(hostname,PORT);
	}
	public AirPlay(String hostname, int port) {
		this(hostname,port,hostname);
	}
	public AirPlay(String hostname, int port, String name) {
		this.hostname = hostname;
		this.port = port;
		this.name = name;
	}
	public void setScreenSize(int width, int height) {
		appletv_width = width;
		appletv_height = height;
		appletv_aspect = (float) width/height;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public void setAuth(Auth auth) {
		this.auth = auth;
	}
	protected String md5Digest(String input) {
		byte[] source;
		try {
			//Get byte according by specified coding.
			source = input.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			source = input.getBytes();
		}
		String result = null;
		char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7',
				'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(source);
			//The result should be one 128 integer
			byte temp[] = md.digest();
			char str[] = new char[16 * 2];
			int k = 0;
			for (int i = 0; i < 16; i++) {
				byte byte0 = temp[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			result = new String(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;

	}
	protected String makeAuthorization(Map params, String password, String method, String uri) {
		String realm = (String) params.get("realm");
		String nonce = (String) params.get("nonce");
		String ha1 = md5Digest(USERNAME+":"+realm+":"+password);
		String ha2 = md5Digest(method+":"+uri);
		String response = md5Digest(ha1+":"+nonce+":"+ha2);
		authorization = "Digest username=\""+USERNAME+"\", "
			+"realm=\""+realm+"\", "
			+"nonce=\""+nonce+"\", "
			+"uri=\""+uri+"\", "
			+"response=\""+response+"\"";
		return authorization;
	}
	protected Map getAuthParams(String authString) {
		Map params = new HashMap();
		int firstSpace = authString.indexOf(' ');
		String digest = authString.substring(0,firstSpace);
		String rest = authString.substring(firstSpace+1).replaceAll("\r\n"," ");
		String[] lines = rest.split("\", ");
		for (int i = 0; i < lines.length; i++) {
			int split = lines[i].indexOf("=\"");
			String key = lines[i].substring(0,split);
			String value = lines[i].substring(split+2);
			if (value.charAt(value.length()-1) == '"') {
				value = value.substring(0,value.length()-1);
			}
			params.put(key,value);
		}
		return params;
	}
	protected String getResponse(HttpURLConnection conn, String method, String uri) throws IOException {
		String authstring = conn.getHeaderFields().get("WWW-Authenticate").get(0);
		Map params = getAuthParams(authstring);
		if (password != null) {
			return makeAuthorization(params,password,method,uri);
		} else {
			if (auth != null) {
				String password = auth.getPassword(hostname,name);
				if (password != null) {
					return makeAuthorization(params,password,method,uri);
				} else {
					return null;
				}
			} else {
				throw new IOException("Authorisation requied");
			}
		}
	}
	protected String doHTTP(String method, String uri) throws IOException {
		return doHTTP(method, uri, null);
	}
	protected String doHTTP(String method, String uri, ByteArrayOutputStream os) throws IOException {
		return doHTTP(method, uri, os, null);
	}
	protected String doHTTP(String method, String uri, ByteArrayOutputStream os, Map headers) throws IOException {
		return doHTTP(method, uri, os, new HashMap(), true);
	}
	protected String doHTTP(String method, String uri, ByteArrayOutputStream os, Map headers, boolean repeat) throws IOException {
		URL url = null;
		try {
			url = new URL("http://"+hostname+":"+port+uri);
		} catch(MalformedURLException e) { }
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setUseCaches(false);
		conn.setDoOutput(true);
		conn.setRequestMethod(method);
		
		if (authorization != null) {
			//Try to reuse authorizatin if already set
			headers.put("Authorization",authorization);
		}
		if (headers.size() > 0) {
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
		
		if (conn.getResponseCode() == 401) {
			if (repeat) {
				String response = getResponse(conn,method,uri);
				if (response != null) {
					return doHTTP(method,uri,os,headers,false);
				} else {
					return null;
				}
			} else {
				throw new IOException("Incorrect password");
			}
		} else {
			//TODO: Only readback Content-Length? - right now not doing, seems to work different than PHP
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
	}
	public void stop() {
		try {
			stopPhotoThread();
			doHTTP("POST", "/stop");
		} catch (Exception e) { }
	}
	protected BufferedImage scaleImage(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		if (width <= appletv_width && height <= appletv_height) {
			return image;
		} else {
			int scaledheight;
			int scaledwidth;
			float image_aspect = (float) width/height;
			if (image_aspect > appletv_aspect) {
				scaledheight = new Float(appletv_width / image_aspect).intValue();
				scaledwidth = appletv_width;
			} else {
				scaledheight = appletv_height;
				scaledwidth = new Float(appletv_height * image_aspect).intValue();
			}
			BufferedImage scaledimage = new BufferedImage(scaledwidth, scaledheight, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = scaledimage.createGraphics();
			g.drawImage(image, 0, 0, scaledwidth, scaledheight, null); 
			g.dispose();
			return scaledimage;
		}
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
		BufferedImage scaledimage = scaleImage(image);
		photoRaw(scaledimage,transition);
		photothread = new PhotoThread(this,scaledimage,5000);
		photothread.start();
	}
	protected void photoRawCompress(BufferedImage image, String transition) throws IOException {
		BufferedImage scaledimage = scaleImage(image);
		photoRaw(scaledimage, transition);
	}
	protected void photoRaw(BufferedImage image, String transition) throws IOException {
		Map headers = new HashMap();
		headers.put("X-Apple-Transition",transition);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		boolean resultWrite = ImageIO.write(image, "jpg", os);
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
						airplay.photoRawCompress(AirPlay.captureScreen(),NONE);
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
	
	//Auth classes
	public static interface Auth {
		public abstract String getPassword(String hostname, String name);
	}
	public static class AuthDialog implements Auth {
		private Window parent;
		public AuthDialog(Window parent) {
			this.parent = parent;
		}
		public String getPassword(String hostname, String name) {
			final JPasswordField password = new JPasswordField();
			JOptionPane optionPane = new JOptionPane(password,JOptionPane.PLAIN_MESSAGE,JOptionPane.OK_CANCEL_OPTION);
			JDialog dialog = optionPane.createDialog(parent,"Password:");
			dialog.setLocationRelativeTo(parent);
			dialog.setVisible(true);
			int result = (Integer)optionPane.getValue();
			dialog.dispose();
			if(result == JOptionPane.OK_OPTION){
				return new String(password.getPassword());
			}
			return null;
		}
	}
	public static class AuthConsole implements Auth {
		public String getPassword(String hostname, String name) {
			String display = hostname == name ? hostname : name+" ("+hostname+")";
			return AirPlay.waitforuser("Please input password for "+display);
		}
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
				AirPlay airplay = new AirPlay(services[index]);
				airplay.setAuth(new AuthDialog(parent));
				return airplay;
			}
			return null;
		}
		throw new IOException("No AppleTVs Found");
	}
	
	// Command line functions
	public static void usage() {
		System.out.println("commands: -s {stop} | -p file {photo} | -d {desktop}");
		System.out.println("java -jar airplay.jar -h hostname[:port] [-a password] command");
	}
	public static String waitforuser() {
		return waitforuser("Press return to quit");
	}
	public static String waitforuser(String message) {
		System.out.println(message);
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String s = null;
		try {
			while ((s = in.readLine()) != null && !(s.length() >= 0)) { }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}
	public static void main(String[] args) {
		try {
			CmdLineParser cmd = new CmdLineParser();
			CmdLineParser.Option hostopt = cmd.addStringOption('h',"hostname");
			CmdLineParser.Option stopopt = cmd.addBooleanOption('s',"stop");
			CmdLineParser.Option photoopt = cmd.addStringOption('p',"photo");
			CmdLineParser.Option desktopopt = cmd.addBooleanOption('d',"desktop");
			CmdLineParser.Option passopt = cmd.addStringOption('a',"password");
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
				airplay.setAuth(new AuthConsole());
				String password = (String) cmd.getOptionValue(passopt);
				airplay.setPassword(password);
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
