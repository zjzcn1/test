package com.zjzcn.test.transport.netty;

import com.zjzcn.test.transport.Decoder;
import com.zjzcn.test.transport.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class NettyDecoder extends ByteToMessageDecoder {

	private Decoder decoder;

	public NettyDecoder(Decoder decoder) {
		this.decoder = decoder;
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		int length = in.readableBytes();

		byte[] data = new byte[length];
		in.readBytes(data);

		Message msg = decoder.decode(data);

		out.add(msg);
	}


}
