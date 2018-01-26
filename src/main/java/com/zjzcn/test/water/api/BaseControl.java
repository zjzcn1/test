package com.zjzcn.test.water.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.math3.linear.RealVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @Author: leon
 * @Date: 2017-12-06
 */

public class BaseControl {

    private final Logger log = LoggerFactory.getLogger(this.getClass());


    private static final int MOVE_MAX_STEP = 40; // 移动的最大步数
    private static final double MOVE_MIN_RAD = 0.3; // 弧度
    private static final double STOP_MIN_RAD = 0.04; // 弧度
    private static final double STOP_MIN_DIS = 0.03; // 米

    private WaterApi waterApi;

    public BaseControl() {
        waterApi = new WaterApi("192.168.10.10", 31001);
    }



    public void moveAndTuningToMarker(String markerName) {
        waterApi.moveToMarker(markerName);

        while (true) {
            JSONObject robotStatus = getRobotStatus();

            String move_status = robotStatus.getString("move_status");
            String move_target = robotStatus.getString("move_target");
            if ("succeeded".equals(move_status) && markerName.equals(move_target)) {
                break;
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        //机器人自动移动后进行微调
        tuningRobot(markerName);
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
            waterApi.joyControl(0, angle);
            angle = diffAngleWithMove(markerName);
            log.info("初始夹角1 -> [{}]rad.", angle);
            angle = MathUtils.turnTo180(angle, cosMTR);
            log.info("初始夹角2 -> [{}]rad.", angle);
            angle = MathUtils.turnToAcuteAngle(angle);
            log.info("初始夹角3 -> [{}]rad.", angle);
        }
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
            if (cosMTR >= 0) {
                waterApi.joyControl(distance, 0);
            } else {
                waterApi.joyControl(-distance, 0);
            }
            distance = diffDistance(markerName);
            log.info("调整后的距离为:{}m.", distance);
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
            waterApi.joyControl(0, angle);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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

        JSONObject rbPos = getRobotStatus().getJSONObject(Constants.CURRENT_POSE);
        RealVector robotVector = VectorUtils.newVector(rbPos.getDoubleValue(Constants.X), rbPos.getDoubleValue(Constants.Y));

        return moveVector.dotProduct(robotVector) / (moveVector.getNorm() * robotVector.getNorm());
    }

    /**
     * 机器人与移动向量的角度差
     */
    private double diffAngleWithMove(String markerName) {
        RealVector moveVector = robotToMarkerVector(markerName);

        double moveTheta = Math.atan2(moveVector.toArray()[1], moveVector.toArray()[0]);
        double robotTheta = getRobotStatus().getJSONObject(Constants.CURRENT_POSE).getDoubleValue(Constants.THETA);

        double moveThetaP = MathUtils.turnTo0_360(moveTheta);
        double robotThetaP = MathUtils.turnTo0_360(robotTheta);

        double minusRad = moveThetaP - robotThetaP;

        minusRad = minusRad > Math.PI ? minusRad - Math.PI * 2 : minusRad;
        minusRad = minusRad < -Math.PI ? minusRad + Math.PI * 2 : minusRad;

        return minusRad;
    }

    private JSONObject getRobotStatus() {
        return JSON.parseObject(waterApi.robotStatus()).getJSONObject("results");
    }
    private JSONObject getMarkerList() {
        return JSON.parseObject(waterApi.markerList()).getJSONObject("results");
    }

    /**
     * 坐标向量减机器人向量
     */
    private RealVector robotToMarkerVector(String markerName) {
        JSONObject rbPos = getRobotStatus().getJSONObject(Constants.CURRENT_POSE);
        JSONObject mkPos = getMarkerList().getJSONObject(markerName).getJSONObject(Constants.POSE).getJSONObject(Constants.POSITION);
        return VectorUtils.assemblyVector(mkPos, rbPos);
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

    public static void main(String[] args) {
        BaseControl moveControl = new BaseControl();
//        moveControl.moveAndTuningToMarker("test1");
//        moveControl.tuningRobotDirection("test1");
//        double test1 = moveControl.diffAngleWithMove("test1");
//        System.out.println(test1);
//
//        double test11 = moveControl.cosTheta("test1");
//        System.out.println(test11);

//        moveControl.recoveryRobotDirection("test1");
//        moveControl.waterApi.joyControl(0, 1);
//        while (true) {
//            double test1 = moveControl.diffAngleWithMarker("test1");
//            System.out.println(test1);
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

//        moveControl.moveAndTuningToMarker("test1");

//        moveControl.tuningRobotLocation("test1");
//        moveControl.tuningRobot("test1");
        moveControl.moveAndTuningToMarker("test4");
    }
}

