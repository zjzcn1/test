package com.zjzcn.test;


import com.google.common.geometry.*;
import org.junit.Test;

/**
 * @Description: ${NOTE}
 * @Author: pingxin
 * @version:
 * @Date: 2017/1/19 14:29
 */
public class CellIdTest {

    @Test
    public void TestLatAndLgt(){
        S2LatLng s2LatLng = S2LatLng.fromDegrees(22.536120, 113.950630);
        S2Point s2Point = s2LatLng.toPoint();


        //to cell
        S2CellId s2CellId = S2CellId.fromLatLng(s2LatLng);


        System.out.println(s2CellId.rangeMax());
        System.out.println(s2CellId.toLatLng().latDegrees());

        S2Cap s2Cap = S2Cap.fromAxisHeight(s2Point, 1000);
        System.out.println(s2Cap.toString());

        //判断点是否在区域内113.94891,22.539783
        S2Point point = S2LatLng.fromDegrees(22.539783, 113.94891).toPoint();


        boolean contains = s2Cap.contains(point);
        System.out.println(contains);

    }

    @Test
    public void testRegine(){
        S2Region s2Region = new S2Polygon();
        GeometryTestCase.makePolygon("22.538464:113.9496;22.538464:113.94891;22.539783:113.94891;22.539783:113.9496");


        S2RegionCoverer s2RegionCoverer = new S2RegionCoverer();
        s2RegionCoverer.setLevelMod(1);
        s2RegionCoverer.setMinLevel(1);
        s2RegionCoverer.setMaxLevel(30);
        s2RegionCoverer.setMaxCells(100);

        S2CellUnion covering = s2RegionCoverer.getCovering(s2Region);


        S2CellId s2CellId = null;
        S2Cell carCell = new S2Cell(s2CellId);

        boolean contains = s2Region.contains(carCell);
        System.out.println("===" + contains);

    }

    private S2CellId getLngLatCellID(double latX, double lngY){
        S2LatLng s2LatLng = S2LatLng.fromDegrees(22.536120, 113.950630);
        S2Point s2Point = s2LatLng.toPoint();



        //to cell
        S2CellId s2CellId = S2CellId.fromLatLng(s2LatLng);
        return s2CellId;
    }

    @Test
    public void testCellIds(){
        S2CellId cellId = getLngLatCellID(22.536120, 113.950630);
        System.out.println(cellId.id());



        S2Cell carCell = new S2Cell(cellId);
        System.out.println(carCell.toString());

        double v = 22.536120 / 180 * S2.M_PI;
        double v1 = 113.950630 / 180 * S2.M_PI;
        System.out.println(v + ";" + v1);
    }
}