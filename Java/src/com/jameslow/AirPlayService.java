package com.jameslow;

import javax.jmdns.*;
import java.net.*;

public class AirPlayService {
	public String name;
	public String hostname;
	public int port;
	
	public AirPlayService(String hostname, int port) {
		this(hostname,port,hostname);
	}
	
	public AirPlayService(String hostname, int port, String name) {
		this.hostname = hostname;
		this.port = port;
		this.name = name;
	}
}
