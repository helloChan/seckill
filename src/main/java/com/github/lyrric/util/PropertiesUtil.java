package com.github.lyrric.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class PropertiesUtil {
    private static final Logger log = LoggerFactory.getLogger(PropertiesUtil.class);

    /**
     * @param fileName jdbc.properties
     * @return
     */
    public synchronized static Properties getProperties(String fileName) {
        Properties props;
        props = new Properties();
        InputStream in = null;
        try {
            // 第一种，通过类加载器进行获取properties文件流
            in = PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName);
            // 第二种，通过类进行获取properties文件流
            //in = PropertyUtil.class.getResourceAsStream("/jdbc.properties");
            props.load(in);
        } catch (FileNotFoundException e) {
            log.error("{}文件未找到", fileName);
        } catch (IOException e) {
            log.error("IO异常：{}", e.getMessage(), e);
        } finally {
            try {
                if (null != in) {
                    in.close();
                }
            } catch (IOException e) {
                log.error("{}文件流关闭出现异常", fileName);
            }
        }
        return props;
    }

    public static String getProperty(Properties props, String key) {
        if (null == props) {
            throw new RuntimeException("properties is null");
        }
        return props.getProperty(key);
    }

    public static String getProperty(Properties props, String key, String defaultValue) {
        if (null == props) {
            throw new RuntimeException("properties is null");
        }
        return props.getProperty(key, defaultValue);
    }

    public static Set<String> allPropertyName(Properties properties) {
        return properties.stringPropertyNames();
    }

    public static Set<String> containsPrefixPropertyName(Properties properties, String prefix) {
        Set<String> strings = PropertiesUtil.allPropertyName(properties);
        Set<String> result = new HashSet<>();
        strings.forEach(key -> {
            if (key.startsWith(prefix)) {
                result.add(key);
            }
        });
        return result;
    }

    /**
     * 返回无前缀的key
     *
     * @param properties
     * @param prefix
     * @return
     */
    public static Set<String> containsPrefixPropertyNameWithoutPrefix(Properties properties, String prefix) {
        Set<String> strings = PropertiesUtil.allPropertyName(properties);
        Set<String> result = new HashSet<>();
        strings.forEach(key -> {
            if (key.startsWith(prefix)) {
                result.add(key.substring(prefix.length() + 1));
            }
        });
        return result;
    }

    /**
     * 优化
     * 按首个key对key分组，返回
     * 如
     * system.a=9
     * system.b=9
     * system.b=10
     * 返回
     *
     * @param keySet
     * @return
     */
    public static Map<String, List<String>> groupByFirstKey(Set<String> keySet) {
        Map<String, List<String>> result = new HashMap<>(6);
        Set<String> temp = new HashSet<>();
        keySet.forEach(key -> {
            temp.add(key.substring(0, key.indexOf(".")));
        });
        temp.forEach(key -> {
            List<String> keyList = new ArrayList<>();
            keySet.forEach(keyAll -> {
                if (keyAll.contains(key+".")) {
                    keyList.add(keyAll);
                }
            });
            result.put(key, keyList);
        });
        return result;
    }
}
