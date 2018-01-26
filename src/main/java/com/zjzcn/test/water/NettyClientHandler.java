package com.zjzcn.test.water;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private CallbackManager callbackManager;


	public NettyClientHandler(CallbackManager callbackManager) {
		this.callbackManager = callbackManager;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		Response response = (Response)msg;

		handleResponse(response);
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


	private void handleResponse(Response response) {
		ResponseFuture responseFuture = callbackManager.removeCallback(response.getRequestId());
		if (responseFuture == null) {
			logger.warn("Client has response from server, but responseFuture not exist,  requestId={}", response.getRequestId());
			return;
		}

		if (response.getException() != null) {
			responseFuture.onFailure(response);
		} else {
			responseFuture.onSuccess(response);
		}
	}
}
