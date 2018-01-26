package com.zjzcn.test.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class NettyEncoder extends MessageToByteEncoder<Message> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	protected void encode(ChannelHandlerContext ctx, Message in, ByteBuf out) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(in.getMessageType());
		sb.append("?");

		Map<String, Object> params = in.getAttachments();
		if(params != null) {
            for (String key : params.keySet()) {
                sb.append(key).append("=").append(params.get(key)).append("&");
            }
        }

		sb.append("uuid=" + in.getRequestId());

		byte[] bytes = sb.toString().getBytes();

        logger.info("Client sending data: {}", sb.toString());
        // write data
		out.writeBytes(bytes);
	}

}
