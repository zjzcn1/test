package com.zjzcn.test.transport;

/**
 * | magic | type | ext | request id | body length | body           |
 * +-------+------+-----+------------+-------------+----------------+
 * | 2byte |1byte |1byte| 8byte      | 4byte       |body length byte|
 * 
 * @author zjz
 *
 */
public class Protocols {
	
    public static final int MESSAGE_MAGIC = 0xCECE;
    public static final int MESSAGE_HEADER_LENGTH = 16;
    
    // heartbeat constants start
    public static final int CLIENT_HEARTBEAT_INTERVAL = 2;
    public static final int CLIENT_IDLE_TIMEOUT= 20;
    public static final int SERVER_IDLE_TIMEOUT = 60;
    
	// netty config value
	public static final int CLIENT_MAX_REQUEST = 2000;
	public static final int REQUEST_TIMEOUT_TIMER_PERIOD = 100;
    
	public static class MessageType {
		public static final byte HEARTBEAT_REQ = 1;
		public static final byte HEARTBEAT_RESP = 2;
		public static final byte MESSAGE_REQ = 3;
		public static final byte MESSAGE_RESP = 4;
	}
	
}
