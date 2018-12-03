package com.zjzcn.test.control;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zjzcn.test.control.transport.Codec;
import com.zjzcn.test.control.transport.Message;
import com.zjzcn.test.control.transport.Response;
import com.zjzcn.test.control.waterapi.JsonUtil;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class WaterCodec implements Codec<Message, ByteBuf> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void encode(Message in, ByteBuf out) {
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

        logger.debug("----------> Sending data: {}", sb.toString());
        // write data
        out.writeBytes(bytes);
    }

    @Override
    public void decode(ByteBuf in, List<Message> out) {
        in.markReaderIndex();
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);

        String data = new String(bytes);
        logger.debug("<---------- Received data: {}", data);

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
