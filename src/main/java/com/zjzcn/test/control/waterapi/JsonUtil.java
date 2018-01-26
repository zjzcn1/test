package com.zjzcn.test.control.waterapi;

import com.alibaba.fastjson.JSON;

public class JsonUtil {

    public static boolean isValidJson(String json) {
        try {
            JSON.parseObject(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
