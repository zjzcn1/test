package com.zjzcn.test.config;

public interface ConfigListener {

    void onUpdate(String key, String value) throws Exception;

    void onDelete(String key) throws Exception;
}
