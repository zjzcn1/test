package com.zjzcn.test.control.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class NettyEncoder extends MessageToByteEncoder<Message> {

	private Codec codec;

	public NettyEncoder(Codec codec) {
		this.codec = codec;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Message in, ByteBuf out) throws Exception {
		codec.encode(in, out);
	}

}
