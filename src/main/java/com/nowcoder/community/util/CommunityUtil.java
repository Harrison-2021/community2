package com.nowcoder.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommunityUtil {
    /** 生成随机字符串 */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * MD5加密
     * hello -> abc123def456
     * hello + 3e4a8 -> abc123def456abc
     */
    public static String md5(String key) {
        // 判空
        if(StringUtils.isBlank(key)) {
            return null;
        }
        // 调用Spring的工具类
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    /**
     * 给浏览器返回Json字符串信息,利用fastjsonAPI，获取json字符串
     * @param code  响应的编码信息
     * @param msg   响应的字符串提示信息
     * @param map   响应的数据
     * @return      Json字符串
     */
    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject jsonObject = new JSONObject(); // 封装数据的Json对象，相当于model
        jsonObject.put("code", code);
        jsonObject.put("msg", msg);
        if(map != null) {
            for(String key : map.keySet()) {
                jsonObject.put(key, map.get(key));
            }
        }
        System.out.println(jsonObject);
        return jsonObject.toJSONString();
    }

    // 重载方法
    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "张三");
        map.put("age", 25);
        System.out.println(getJSONString(0, "提交成功", map));
    }

}
