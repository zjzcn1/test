package com.zjzcn.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zjzcn.test.util.GeoUtils;
import com.zjzcn.test.util.HttpClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class WeatherSpider {

    private static final String POLYGON_FILE = "shanghai_geo_polygon.json";

    private static final String WEATHER_API = "https://api.caiyunapp.com/v2/YHD2fHiYNYjvPMNn/%s/realtime.json";

    public static void main(String[] args) throws IOException {
        String path = Thread.class.getResource("/").getPath();
        String json = FileUtils.readFileToString(new File(path + POLYGON_FILE), "UTF-8");
        JSONObject jo = JSON.parseObject(json);
        JSONObject feature = (JSONObject)jo.getJSONArray("features").get(0);
        JSONArray polygonJA = feature.getJSONObject("geometry").getJSONArray("coordinates").getJSONArray(0);

        List<Point2D> polygon = new ArrayList<>();
        for (Object point : polygonJA) {
            JSONArray p = (JSONArray) point;
            polygon.add(new Point2D.Double(p.getDouble(0), p.getDouble(1)));
        }

        double minLon = 180;
        double maxLon = 0;
        double minLat = 90;
        double maxLat = 0;

        for (Point2D point : polygon) {
            minLon = Math.min(point.getX(), minLon);
            maxLon = Math.max(point.getX(), maxLon);
            minLat = Math.min(point.getY(), minLat);
            maxLat = Math.max(point.getY(), maxLat);
        }
        System.out.println("[" + minLon + "," + minLat + "], [" + maxLon + "," + maxLat+"]");

        long lonRange = Math.round((maxLon - minLon) * 100);
        long latRange = Math.round((maxLat - minLat) * 100);
        List<Point2D.Double> points = new ArrayList<>();
        for (int i = 0; i <= lonRange; i++) {
            double lon = minLon + i * 0.01;
            for (int j = 0; j <= latRange; j++) {
                double lat = minLat + j * 0.01;
                Point2D.Double point = new Point2D.Double(lon, lat);
                boolean isIn = GeoUtils.isPointInPolygon(point, polygon);
                if (isIn) {
                    points.add(point);
                }
            }
        }

        System.out.println("points: " + points.size());

        System.out.println("1day:" + points.size() * 8);
        boolean isIn = GeoUtils.isPointInPolygon(new Point2D.Double(121.99051,30.99455), polygon);
        System.out.println(isIn);

        HttpClient httpClient = new HttpClient();
        String result = httpClient.get(String.format(WEATHER_API, "120.8568,30.6774"));
        System.out.println(result);

        result = httpClient.get(String.format(WEATHER_API, "121.99698,31.51206"));
        log.info(result);
    }


}
