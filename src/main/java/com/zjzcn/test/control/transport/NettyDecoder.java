package com.zjzcn.test.control.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class NettyDecoder extends ByteToMessageDecoder {

	private Codec codec;

	public NettyDecoder(Codec codec) {
	    this.codec = codec;
    }

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        codec.decode(in, out);
    }

}
