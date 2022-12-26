package com.fangtang.idataservice.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Map;

/**
 * *
 * 任务
 * @Author Mr.JIA
 * @Date 2022/12/10 17:24
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Task {

    private String taskId;
    private String taskName;
    private String taskDataType;
    private String createtime;
    private String createUser;
    private String taskStatus;
    private String taskDescribe;
    private ArrayList<Map<String,Object>> calculationList;
}
