package com.fangtang.idataservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.fangtang.idataservice.mapper")
public class IdataserviceApplication {

    public static void main(String[] args){
        SpringApplication.run(IdataserviceApplication.class, args);
    }
}
