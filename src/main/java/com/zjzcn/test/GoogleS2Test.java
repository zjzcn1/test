package com.zjzcn.test;

import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import com.google.common.geometry.*;

import java.util.ArrayList;
import java.util.List;

public class GoogleS2Test {

    public static void main(String[] args) {

        String str = "31.883742,117.254568;31.885026,117.25463;31.886586,117.254635;31.886651,117.256142;31.886831,117.263231;31.886927,117.266044;31.886947,117.269116;31.886957,117.270652;31.886962,117.27142;31.886964,117.271804;31.885646,117.272029;31.885269,117.272103;31.884496,117.272279;31.883595,117.272516;31.883209,117.272614;31.882707,117.272788;31.881498,117.273503;31.879608,117.274664;31.879599,117.274226;31.879582,117.27335;31.879546,117.271598;31.87954,117.27019;31.879211,117.270031;31.87923,117.269693;31.879192,117.268833;31.879128,117.267435;31.879013,117.265791;31.878907,117.264018;31.879709,117.263155;31.880602,117.262024;31.881901,117.260291;31.882234,117.258473;31.882422,117.254678;31.883742,117.254568";
        String[] points = str.split(";");

        S2LatLngRect rect = new S2LatLngRect(S2LatLng.fromDegrees(-51.264871, -30.241701),
                S2LatLng.fromDegrees(-51.04618, -30.000003));
        S2RegionCoverer coverer = new S2RegionCoverer();
        coverer.setMinLevel(8);
        coverer.setMaxLevel(20);
        coverer.setMaxCells(500);
        S2CellUnion cellIds = coverer.getCovering(makePolygon(str));

        JSONArray ja = new JSONArray();
        List<S2Polygon> polygons = new ArrayList<>();
        for (S2CellId cellId : cellIds) {
            List<S2LatLng> latLngs = toLatLngs(cellId);
            System.out.println("latLngs = [" + latLngs + "]");
            JSONArray polygon = new JSONArray();
            for (S2LatLng latLng : latLngs) {
                double lat = latLng.latDegrees();
                double lnt = latLng.lngDegrees();
                JSONArray ll = new JSONArray();
                ll.add(lnt);
                ll.add(lat);
                polygon.add(ll);
                System.out.println("ll = [" + ll + "]");
            }
            ja.add(polygon);
        }
        System.out.println("json = " + ja + "");
        S2Polygon union = S2Polygon.destructiveUnionSloppy(polygons, S1Angle.degrees(0.9));
        System.out.println("args = [" + union.numLoops() + "]");
    }

    public static S2Polygon makePolygon(String str) {
        List<S2Point> vertices = Lists.newArrayList();

        JSONArray ja = new JSONArray();
        for (String token : str.split(";")) {
            double lat = Double.parseDouble(token.split(",")[0]);
            double lng = Double.parseDouble(token.split(",")[1]);
            vertices.add(S2LatLng.fromDegrees(lat, lng).toPoint());
            JSONArray ll = new JSONArray();
            ll.add(lng);
            ll.add(lat);
            ja.add(ll);
        }
        System.out.println("polygon = [" + ja + "]");
        S2Loop loop = new S2Loop(vertices);
        loop.normalize();
        return new S2Polygon(loop);
    }

    public static List<S2LatLng> toLatLngs(S2CellId cellId) {
        List<S2LatLng> points = new ArrayList<>();
        S2Cell cell = new S2Cell(cellId);
        for (int i = 0; i < 4; i++) {
            S2Point point = cell.getVertex(i);
            S2LatLng latLng = new S2LatLng(point);
            points.add(latLng);
        }
        return points;
    }
}