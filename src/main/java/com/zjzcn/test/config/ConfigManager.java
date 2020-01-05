package com.zjzcn.test.config;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ConfigManager {

    private static Map<String, List<ConfigListener>> configListeners = new ConcurrentHashMap<>();
    private static Map<String, Config> configs = new ConcurrentHashMap<>();

    public void addConfig(Config config) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(config.getKey());

        Config oldConfig = configs.get(config.getKey());
        if (oldConfig == null || config.getVersion() > oldConfig.getVersion()) {
            // put config
            configs.put(config.getKey(), config);
            // notify listeners
            List<ConfigListener> listeners = configListeners.get(config.getKey());
            if (listeners != null) {
                for(ConfigListener listener : listeners) {
                    try {
                        listener.onUpdate(config.getKey(), config.getValue());
                    } catch (Throwable e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    public void removeConfig(String key) {
        Objects.requireNonNull(key);

        if (configs.containsKey(key)) {
            // delete config
            configs.remove(key);
            // notify listeners
            List<ConfigListener> listeners = configListeners.get(key);
            if (listeners != null) {
                for(ConfigListener listener : listeners) {
                    try {
                        listener.onDelete(key);
                    } catch (Throwable e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    public void addListener(String key, ConfigListener configListener) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(configListener);
        // first use, invoke and watch this key
        String value = get(key);
        if (value != null) {
            try {
                configListener.onUpdate(key, value);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        // listen this key
        List<ConfigListener> listeners = configListeners.computeIfAbsent(key, k -> new ArrayList<>());
        listeners.add(configListener);
    }

    private String get(String key) {
        Objects.requireNonNull(key);

        Config config = configs.get(key);
        if (config == null) {
            return null;
        }

        return config.getValue();
    }

    public String getString(String key) {
        return get(key);
    }

    public Boolean getBoolean(String key) {
        String value = get(key);
        if (value == null) {
            return null;
        }
        return Boolean.valueOf(value);
    }

    public Short getShort(String key) {
        String value = get(key);
        if (value == null) {
            return null;
        }
        return Short.valueOf(value);
    }

    public Integer getInt(String key) {
        String value = get(key);
        if (value == null) {
            return null;
        }
        return Integer.valueOf(value);
    }

    public Long getLong(String key) {
        String value = get(key);
        if (value == null) {
            return null;
        }
        return Long.valueOf(value);
    }

    public Float getFloat(String key) {
        String value = get(key);
        if (value == null) {
            return null;
        }
        return Float.valueOf(value);
    }

    public Double getDouble(String key) {
        String value = get(key);
        if (value == null) {
            return null;
        }
        return Double.valueOf(value);
    }

}
