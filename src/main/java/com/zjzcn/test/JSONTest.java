package com.zjzcn.test;

public class JSONTest {
    public static void main(String[] args) {

        double x = 0;
        double y = 0;
        double z = 0.6721;
        double w = 0.7404;
        double yaw = Math.atan2(2*(w * z + x * y), 1 - 2 * (y * y + z * z));
        System.out.println(yaw);
    }
}
