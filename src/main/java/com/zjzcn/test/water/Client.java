package com.zjzcn.test.water;


import java.net.SocketAddress;

public interface Client {

	void connect();
	
	void reconnect();
	
	void close();

	boolean isConnected();

	SocketAddress getLocalAddress();

	SocketAddress getRemoteAddress();
	
	Response send(Request req);

    Response send(Request req, long timeoutMillis);

}
