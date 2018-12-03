package com.zjzcn.test.control.waterapi;

import com.zjzcn.test.control.WaterCodec;
import com.zjzcn.test.control.transport.Client;
import com.zjzcn.test.control.transport.NettyClient;
import com.zjzcn.test.control.transport.Request;
import com.zjzcn.test.control.transport.RequestId;
import com.zjzcn.test.control.transport.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class WaterApi {

    private static final Logger logger = LoggerFactory.getLogger(WaterApi.class);

    private Client client;

    public Client getClient() {
        return client;
    }

    public WaterApi(String serverHost, int serverPort) {
        client = new NettyClient(serverHost, serverPort, new WaterCodec());
    }

    private String send(String command, Map<String, Object> params) {
        Request request = new Request();
        request.setRequestId(RequestId.newStringId());
        request.setMessageType(command);
        request.setAttachments(params);
        Response response = client.send(request);
        return (String)response.getData();
    }
    private String send(String command) {
        return send(command, null);
    }

    public String robotStatus() {
        for (int i=0; i<3; i++) {
            try {
                String response = send("/api/robot_status");
                return response;
            } catch (Exception e) {
                logger.error("Get robotStatus error.", e);
            }
        }

        throw new RuntimeException("Retry 3 count.");
    }

    public String moveToMarker(String markerName) {
        Map<String, Object> params = new HashMap<>();
        params.put("marker", markerName);
        String response = send("/api/move", params);
        return response;
    }

    public String moveToLocation(double x, double y, double theta) {
        Map<String, Object> params = new HashMap<>();
        params.put("location", x + "," + "," + theta);
        String response = send("/api/move", params);
        return response;
    }

    public String joyControl(double linearVelocity, double angularVelocity) {
        Map<String, Object> params = new HashMap<>();
        params.put("angular_velocity", angularVelocity);
        params.put("linear_velocity", linearVelocity);
        String response = send("/api/joy_control", params);
        return response;
    }

    public String setCurrentMap(String hotelId, int floor) {
        Map<String, Object> params = new HashMap<>();
        params.put("map_name", hotelId);
        params.put("floor", floor);
        String response = send("/api/map/set_current_map", params);
        return response;
    }

    public String getCurrentMap() {
        Map<String, Object> params = new HashMap<>();
        String response = send("/api/map/get_current_map");
        return response;
    }

    public String getParams() {
        String response = send("/api/get_params");
        return response;
    }

    public String markerList() {
        String response = send("/api/markers/query_list");

        return response;
    }

    public String mapList() {
        String response = send("/api/map/list");

        return response;
    }

    public String insertMarker(String name, String type) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("type", type);
        String response = send("/api/markers/insert", params);

        return response;
    }


    public String deleteMarker(String name) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        String response = send("/api/markers/delete", params);

        return response;
    }

    public static void main(String[] args) {

        WaterApi waterApi = new WaterApi("192.168.10.10", 31001);

        String s = waterApi.robotStatus();
        System.out.println(s);

        String s1 = waterApi.getParams();
        System.out.println(s1);

        waterApi.joyControl(0.1, 0);

//        String test1 = waterApi.moveToMarker("test1");
//        System.out.println(test1);

    }
}
