package com.zjzcn.test.transport.netty;

import com.zjzcn.test.transport.MessageHandler;
import com.zjzcn.test.transport.Request;
import com.zjzcn.test.transport.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {

	private static Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

	private MessageHandler messageHandler;
	
	public NettyClientHandler(MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		if (msg instanceof Request) {
			Request request = (Request) msg;
			long processStartTime = System.currentTimeMillis();
			try {
				Response response = messageHandler.handleRequest(request);
				response.setProcessTime(System.currentTimeMillis() - processStartTime);
				if (ctx.channel().isActive()) {
					ctx.write(response);
				}
			} catch (Exception e) {
				String errorMsg = "NettyHandler handle requset error.";
				logger.error(errorMsg, e);
				Response response = new Response();
				response.setRequestId(request.getRequestId());
				response.setException(new RuntimeException(errorMsg, e));
				response.setProcessTime(System.currentTimeMillis() - processStartTime);
				ctx.write(response);
			}
		} else if (msg instanceof Response) {
			Response response = (Response)msg;
			messageHandler.handleResponse(response);
		} else {
			String errorMsg = "NettyHandler messageReceived type not support: class=" + msg.getClass();
			logger.error(errorMsg);
			throw new RuntimeException(errorMsg);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable t) {
		logger.error("Exception:", t);
		ctx.close();
	}

}
