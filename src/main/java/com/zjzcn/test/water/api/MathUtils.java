package com.zjzcn.test.water.api;

/**
 * @Author: leon
 * @Date: 2017/12/6 下午3:26
 */
public class MathUtils {

    public static double quadruplesRad(double y, double x) {
        double pi = 3.1416;
        double yaw = 2 * Math.atan2(y, x);
        if (yaw > pi && yaw <= 2 * pi) {
            yaw -= 2 * pi;
        }
        if (yaw < -pi && yaw >= -2 * pi) {
            yaw += 2 * pi;
        }
        return yaw;
    }

    /**
     * 讲角度转换成0～360
     * 如果rad<0，则将负弧度转换成正弧度
     */
    public static double turnTo0_360(double rad) {
        rad = rad < 0 ? Math.PI * 2 + rad : rad;
        rad = rad >= Math.PI * 2 ? rad - Math.PI * 2 : rad;
        return rad;
    }

    /**
     * 把反向向量调成正向
     */
    public static double turnTo180(double angle, double angleCos) {
        return angleCos < 0 ? angle - Math.PI : angle;
    }

    /**
     * 调整角度为锐角
     */
    public static double turnToAcuteAngle(double angle) {
        angle = angle > Math.PI ? angle - Math.PI * 2 : angle;
        angle = angle < -Math.PI ? angle + Math.PI * 2 : angle;
        return angle;
    }
}
