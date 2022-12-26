package com.fangtang.idataservice;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class IdataserviceApplicationTests {

    @Test
    void contextLoads() throws IOException {

        int pages = 0;
        int num1 = 120 % 10;
        if (num1 == 0){
            pages = 120 / 10;
        }else {
            pages = 120 / 9 + 1;
        }
        System.out.println(pages);
    }
}
