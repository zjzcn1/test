package com.zjzcn.test.water;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NettyDecoder extends ByteToMessageDecoder {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        in.markReaderIndex();
		byte[] bytes = new byte[in.readableBytes()];
		in.readBytes(bytes);

		String data = new String(bytes);
		logger.info("Server received data: {}", data);

        String[] array = data.split("\n");
        if (!JsonUtil.isValidJson(array[array.length-1])) {
            in.resetReaderIndex();
            logger.info("Invalid json data, data: {}", data);
            return;
        }

        for (String json : array) {
            JSONObject jo = JSON.parseObject(json);
            if ("response".equals(jo.getString("type"))) {
                String uuid = jo.getString("uuid");
                if (uuid == null) {
                    logger.warn("uuid is null, data: {}", json);
                    return;
                }
                Response response = new Response();
                response.setRequestId(uuid);
                response.setData(json);
                out.add(response);
            } else {
                logger.info("not response type: {}", json);
            }
        }
    }

}
