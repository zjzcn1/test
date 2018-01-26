package com.zjzcn.test.transport.netty;

import com.zjzcn.test.transport.Encoder;
import com.zjzcn.test.transport.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class NettyEncoder extends MessageToByteEncoder<Message> {

	private Encoder encoder;

	public NettyEncoder(Encoder encoder) {
		this.encoder = encoder;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Message in, ByteBuf out) throws Exception {

		byte[] bytes = encoder.encode(in);
		out.writeBytes(bytes);
	}

}
