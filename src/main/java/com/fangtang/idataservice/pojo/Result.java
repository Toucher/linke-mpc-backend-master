package com.fangtang.idataservice.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * *
 * 返回值实体类
 * @Author Mr.JIA
 * @Date 2022/08/10 13:48
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {

    private int code;
    private String message;
    private Object data;

    /**
     * @Author Mr.JIA
     * @Description 成功返回结果（无数据）
     * @Date 13:51 2021/12/28
     * @Param [msg]
     * @return com.example.testdemo.pojo.Result
     **/
    public static Result success(int code,String message){
        return new Result(code,message,null);
    }

    /**
     * @Author Mr.JIA
     * @Description 成功返回结果（有数据）
     * @Date 13:52 2021/12/28
     * @Param [msg, obj]
     * @return com.example.testdemo.pojo.Result
     **/
    public static Result success(int code,String message,Object data){
        return new Result(code,message,data);
    }

    /**
     * @Author Mr.JIA
     * @Description 失败返回结果
     * @Date 13:53 2021/12/28
     * @Param [msg]
     * @return com.example.testdemo.pojo.Result
     **/
    public static Result error(int code,String message){
        return new Result(code,message,null);
    }
}
