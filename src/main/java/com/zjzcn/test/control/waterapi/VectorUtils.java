package com.zjzcn.test.control.waterapi;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 * @Author: leon
 * @Date: 2017/12/8 下午4:02
 */
public class VectorUtils {

    public static RealVector assemblyVector(JSONObject v1, JSONObject v2) {
        return new ArrayRealVector(new double[]{v1.getDoubleValue(Constants.X) - v2.getDoubleValue(Constants.X),
                v2.getDoubleValue(Constants.Y) - v2.getDoubleValue(Constants.Y)});
    }

    public static RealVector newVector(double x, double y) {
        return new ArrayRealVector(new double[]{x, y});
    }
}
