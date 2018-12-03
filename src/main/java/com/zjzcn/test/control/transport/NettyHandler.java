package com.zjzcn.test.control.transport;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NettyHandler extends ChannelInboundHandlerAdapter {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private List<Handler> handlers;

	public NettyHandler(List<Handler> handlers) {
		this.handlers = handlers;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		for (Handler handler : handlers) {
            handler.handle(ctx, msg);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable t) {
		logger.error("ExceptionCaught: remote={} local={}", ctx.channel().remoteAddress(), ctx.channel().localAddress(), t);
		ctx.close();
	}

}
