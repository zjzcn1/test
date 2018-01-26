package com.zjzcn.test.transport;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zjzcn.test.transport.netty.NettyClient;

public class Test {

    public static void main(String[] args) {
        Client client = new NettyClient("192.168.10.10", 31001,
                new WaterEncoder(), new WaterDecoder());
        client.connect();

        Request request = new Request();
        request.setRequestId(RequestId.newId() + "");
        request.setMessageType("/api/markers/query_list");
        request.setData("/api/markers/query_list");

        Response send = client.send(request);
        System.out.println(send);
    }

}

class WaterEncoder implements Encoder {

    @Override
    public byte[] encode(Message msg) {
        String data = (String)msg.getData();
        return data.getBytes();
    }
}

class WaterDecoder implements Decoder {

    @Override
    public Message decode(byte[] data) {
        String msg = new String(data);
        JSONObject jo = JSON.parseObject(msg);
        String uuid = jo.getString("uuid");
        String command = jo.getString("command");
        Response response = new Response();
        response.setRequestId(uuid);
        response.setMessageType(command);

        return null;
    }
}
