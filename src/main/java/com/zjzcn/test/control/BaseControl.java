package com.zjzcn.test.control;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zjzcn.test.control.waterapi.Constants;
import com.zjzcn.test.control.waterapi.MathUtils;
import com.zjzcn.test.control.waterapi.VectorUtils;
import com.zjzcn.test.control.waterapi.WaterApi;
import com.zjzcn.test.util.ThreadUtil;
import com.zjzcn.test.util.Tuple;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BaseControl {

    private final Logger log = LoggerFactory.getLogger(this.getClass());


    private static final int MOVE_MAX_STEP = 30; // 移动的最大步数
    private static final double MOVE_MIN_RAD = 0.1; // 弧度
    private static final double STOP_MIN_RAD = 0.05; // 弧度
    private static final double STOP_MIN_DIS = 0.05; // 米

    private WaterApi waterApi;

    private boolean isStop = true;

    public BaseControl() {

    }

    public WaterApi getWaterApi() {
        return waterApi;
    }

    public void init(String serverHost, int serverPort) {
        waterApi = new WaterApi(serverHost, serverPort);
    }

    public void stop() {
        isStop = true;
    }

    public void moveToMarker(String markerName) {
        waterApi.moveToMarker(markerName);
    }

    public boolean waitToMoveSucceeded(String markerName) {
        isStop = false;
        while (!isStop) {
            if (isMoveSucceeded(markerName)) {
                return true;
            }
            else {
                ThreadUtil.sleep(1000);
            }
        }
        return false;
    }

    public boolean isMoveSucceeded(String markerName) {
        JSONObject robotStatus = getRobotStatus();

        String move_status = robotStatus.getString("move_status");
        String move_target = robotStatus.getString("move_target");
        return "succeeded".equals(move_status) && markerName.equals(move_target);
    }

    public void moveAndTuningToMarker(String markerName) {
        moveToMarker(markerName);

        if (waitToMoveSucceeded(markerName)) {
            //机器人自动移动后进行微调
            tuningRobot(markerName);
        }
    }

    public void moveAndWaitToMarker(String markerName) {
        moveToMarker(markerName);

        waitToMoveSucceeded(markerName);
    }

    private void tuningRobot(String markerName) {
        long start = System.currentTimeMillis();
        tuningRobotLocation(markerName);
        recoveryRobotDirection(markerName);
        log.info("==========微调完成==========");
        log.info("耗时: {}ms ", System.currentTimeMillis() - start);
        log.info("目标点: ", markerName);
        log.info("位置差：{}m.", diffDistance(markerName));
        log.info("角度差: {}rad.", diffAngleWithMarker(markerName));
    }

    /**
     * 调整机器人方向
     */
    private void tuningRobotDirection(String markerName) {
        log.info("开始移动时的角度调整...");
        double cosMTR = cosTheta(markerName);
        log.info("与目标方向呈: {}", cosMTR > 0 ? "锐角" : "钝角");
        double angle = diffAngleWithMove(markerName);
        log.info("初始夹角1 -> [{}]rad.", angle);
        angle = MathUtils.turnTo180(angle, cosMTR);
        log.info("初始夹角2 -> [{}]rad.", angle);
        angle = MathUtils.turnToAcuteAngle(angle);
        log.info("初始夹角3 -> [{}]rad.", angle);
        while (Math.abs(angle) > MOVE_MIN_RAD) {
            log.info("开始调整:{}", angle > 0 ? "左转" : "右转");
            moveRobotAndWaitMS(0, angle, 500);
            angle = diffAngleWithMove(markerName);
            log.info("初始夹角1 -> [{}]rad.", angle);
            angle = MathUtils.turnTo180(angle, cosMTR);
            log.info("初始夹角2 -> [{}]rad.", angle);
            angle = MathUtils.turnToAcuteAngle(angle);
            log.info("初始夹角3 -> [{}]rad.", angle);
        }
    }

    private void moveRobotAndWaitMS(double linearVelocity, double angularVelocity, long ms) {
        waterApi.joyControl(linearVelocity, angularVelocity);
        ThreadUtil.sleep(ms);
    }
    /**
     * 微调机器人位置
     */
    private void tuningRobotLocation(String markerName) {
        log.info("位置调整开始...");
        double distance = diffDistance(markerName);
        log.info("初始距离:{}m.", distance);
        int i = 0;
        while (distance > STOP_MIN_DIS) {
            if (i > MOVE_MAX_STEP) {
                log.warn("达到最大的调整次数：{}", MOVE_MAX_STEP);
                break;
            }
            //每次调整位置前都先调整方向，防止由于位置的改变导致方向差距变大
            tuningRobotDirection(markerName);

            double cosMTR = cosTheta(markerName);
            log.info("角度调整后的cos:{}", cosMTR);

            // 调整距离
            distance = diffDistance(markerName);
            log.info("调整前的【距离】为:{}m.", distance);
            if (cosMTR >= 0) {
                moveRobotAndWaitMS(distance, 0, 1000);
            } else {
                moveRobotAndWaitMS(-distance, 0, 1000);
            }
            distance = diffDistance(markerName);
            log.info("调整后的【距离】为:{}m.", distance);
            i++;
        }
    }

    /**
     * 恢复机器人方向
     */
    private void recoveryRobotDirection(String markerName) {
        log.info("开始恢复机器人角度...");
        double angle = diffAngleWithMarker(markerName);
        log.info("初始夹角1 -> [{}]rad.", angle);
        angle = MathUtils.turnToAcuteAngle(angle);
        log.info("初始夹角2 -> [{}]rad.", angle);

        while (Math.abs(angle) > STOP_MIN_RAD) {
//            angle = angle > 1 ? 1 : angle;
//            angle = angle < -1 ? -1 : angle;
            log.info("开始调整:{}", angle > 0 ? "左转" : "右转");
            // joy control
            moveRobotAndWaitMS(0, angle, 1000);

            angle = diffAngleWithMarker(markerName);
            log.info("恢复后的夹角1 -> [{}]rad.", angle);
            angle = MathUtils.turnToAcuteAngle(angle);
            log.info("恢复后的夹角2 -> [{}]rad.", angle);
        }
    }

    /**
     * 机器人坐标向量与减（标记物向量-机器人坐标向量）向量之间夹角的余弦
     * a.b = |a|.|b|.cosTheta
     * a.b > 0   0 < theta < 90
     * a.b < 0  90 < theta < 180
     */
    private double cosTheta(String markerName) {
        RealVector moveVector = robotToMarkerVector(markerName);

        double theta = getRobotTheta();
        RealVector robotVector = getUnitVector(theta);

        return moveVector.dotProduct(robotVector) / (moveVector.getNorm() * robotVector.getNorm());
    }

    private double getRobotTheta() {
        double theta = getRobotStatus().getJSONObject("current_pose").getDoubleValue("theta");
        return theta;
    }


    private RealVector  getUnitVector(double angle) {
        double vec_y = Math.sin(angle);
        double vec_x = Math.cos(angle);
        return new ArrayRealVector(new double[]{vec_x,vec_y});
    }

    /**
     * 机器人与移动向量的角度差
     */
    private double diffAngleWithMove(String markerName) {
        RealVector moveVector = robotToMarkerVector(markerName);

        double moveTheta = Math.atan2(moveVector.toArray()[1], moveVector.toArray()[0]);
        double robotTheta = getRobotTheta();

        double moveThetaP = MathUtils.turnTo0_360(moveTheta);
        double robotThetaP = MathUtils.turnTo0_360(robotTheta);

        double minusRad = moveThetaP - robotThetaP;

        minusRad = minusRad > Math.PI ? minusRad - Math.PI * 2 : minusRad;
        minusRad = minusRad < -Math.PI ? minusRad + Math.PI * 2 : minusRad;

        return minusRad;
    }

    public JSONObject getRobotStatus() {
        return JSON.parseObject(waterApi.robotStatus()).getJSONObject("results");
    }

    public JSONObject getMarkerList() {
        return JSON.parseObject(waterApi.markerList()).getJSONObject("results");
    }

    /**
     * 坐标向量减机器人向量
     */
//    private RealVector robotToMarkerVector(String markerName) {
//        JSONObject rbPos = getRobotStatus().getJSONObject(Constants.CURRENT_POSE);
//        JSONObject mkPos = getMarkerList().getJSONObject(markerName).getJSONObject(Constants.POSE).getJSONObject(Constants.POSITION);
//        return VectorUtils.assemblyVector(mkPos, rbPos);
//    }

    /**
     * 坐标向量减机器人向量
     */
    private RealVector robotToMarkerVector(String markerName) {
        JSONObject rbPos = getRobotStatus().getJSONObject(Constants.CURRENT_POSE);
        double robotX = rbPos.getDoubleValue("x");
        double robotY = rbPos.getDoubleValue("y");
        JSONObject mkPos = getMarkerList().getJSONObject(markerName).getJSONObject(Constants.POSE).getJSONObject(Constants.POSITION);
        double mkX = mkPos.getDoubleValue("x");
        double mkY = mkPos.getDoubleValue("y");

        return new ArrayRealVector(new double[]{(mkX-robotX), (mkY-robotY)});
    }

    /**
     * 计算机器人与标记点的方位差
     */
    private double diffAngleWithMarker(String markerName) {
        JSONObject robotStatus = getRobotStatus();
        double robotRad = robotStatus.getJSONObject(Constants.CURRENT_POSE).getDoubleValue(Constants.THETA);
        JSONObject markerStatus = getMarkerList();
        JSONObject marker = markerStatus.getJSONObject(markerName).getJSONObject(Constants.POSE).getJSONObject(Constants.ORIENTATION);
        double markerRad = MathUtils.quadruplesRad(marker.getDoubleValue(Constants.Z), marker.getDoubleValue(Constants.W));

        double minusRad = markerRad - robotRad;
        minusRad = minusRad > Math.PI ? minusRad - Math.PI * 2 : minusRad;
        minusRad = minusRad < -Math.PI ? minusRad + Math.PI * 2 : minusRad;

        return minusRad;
    }

    /**
     * 计算机器人与标记点的距离差
     */
    private double diffDistance(String markerName) {
        JSONObject rbPos = getRobotStatus().getJSONObject(Constants.CURRENT_POSE);
        RealVector robotVector = VectorUtils.newVector(rbPos.getDoubleValue(Constants.X), rbPos.getDoubleValue(Constants.Y));

        JSONObject markerStatus = getMarkerList();
        JSONObject mkPos = markerStatus.getJSONObject(markerName).getJSONObject(Constants.POSE).getJSONObject(Constants.POSITION);
        RealVector markerVector = VectorUtils.newVector(mkPos.getDoubleValue(Constants.X), mkPos.getDoubleValue(Constants.Y));

        return robotVector.getDistance(markerVector);
    }

    public List<String> getMapList() {
        String s = waterApi.mapList();
        JSONObject jsonObject = JSON.parseObject(s);
        JSONObject results = jsonObject.getJSONObject("results");
        if (results == null) {
            return Collections.EMPTY_LIST;
        }

        List<String> ret = new ArrayList<>();
        for(String mapName : results.keySet()) {
            JSONArray jsonArray = results.getJSONArray(mapName);
            for (Object floor : jsonArray) {
                ret.add(mapName + "," + floor);
            }
        }
        return ret;
    }

    public void setCurrentMap(String mapName, int floor) {
        waterApi.setCurrentMap(mapName, floor);
    }

    public Tuple getCurrentMap() {
        String json = waterApi.getCurrentMap();
        JSONObject jsonObject = JSON.parseObject(json);
        JSONObject results = jsonObject.getJSONObject("results");
        if (results == null) {
            return null;
        }

        return Tuple.of(results.getString("map_name"), results.getInteger("floor"));
    }

    public boolean isConnected() {
        if (waterApi == null) {
            return false;
        }
        return waterApi.getClient().isConnected();
    }

    public static void main(String[] args) {
        BaseControl baseControl = new BaseControl();
        baseControl.init("192.168.10.10", 31001);

//        baseControl.moveAndTuningToMarker("test1");
//        baseControl.tuningRobotDirection("test1");
//        double test1 = baseControl.diffAngleWithMove("test1");
//        System.out.println(test1);
//
//        double test11 = baseControl.cosTheta("test1");
//        System.out.println(test11);

//        baseControl.recoveryRobotDirection("test1");
//        baseControl.waterApi.joyControl(0, 1);
//        while (true) {
//            double test1 = baseControl.diffAngleWithMarker("test1");
//            System.out.println(test1);
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

//        baseControl.moveAndTuningToMarker("test1");

//        baseControl.tuningRobotLocation("test1");
//        baseControl.tuningRobot("test1");
//        baseControl.moveAndTuningToMarker("marker1");
//        baseControl.getWaterApi().deleteMarker("marker2");
        baseControl.getWaterApi().deleteMarker("marker1");
        ThreadUtil.sleep(1000);
        baseControl.getWaterApi().insertMarker("marker1", "0");
    }
}

