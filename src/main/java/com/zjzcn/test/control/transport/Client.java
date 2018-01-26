package com.zjzcn.test.control.transport;


import java.net.SocketAddress;

public interface Client {

	void reconnect();
	
	void close();

	boolean isConnected();

	SocketAddress getLocalAddress();

	SocketAddress getRemoteAddress();
	
	Response send(Request req);

    Response send(Request req, long timeoutMillis);

}
