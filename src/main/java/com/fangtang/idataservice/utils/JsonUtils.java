package com.fangtang.idataservice.utils;

import com.alibaba.fastjson.JSON;

import java.io.*;

public class JsonUtils {
    public static String bean2Json(Object obj) {
        return JSON.toJSONString(obj);
    }

    public static <T> T json2Bean(String jsonStr, Class<T> objClass) {
        return JSON.parseObject(jsonStr, objClass);
    }

    public static void bean2JsonFile(String objPayload, String filename){
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            //String objPayload = bean2Json(obj);
//            System.out.println(objPayload);
            out.write(objPayload);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T JsonFile2Bean(String filename, Class<T> objClass){
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String jsonStr = in.readLine();
            return json2Bean(jsonStr, objClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
