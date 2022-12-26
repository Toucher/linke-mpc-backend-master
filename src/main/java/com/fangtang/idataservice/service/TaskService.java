package com.fangtang.idataservice.service;

import java.util.ArrayList;
import java.util.Map;

public interface TaskService {

    //获取任务信息列表（条件查询、分页）
    String getTaskInfoList(Map<String, Object> map);

    //创建任务
    void addTask(Map<String, Object> map);

    //获取任务详情
    String getTaskDetails(Map<String, Object> map);

    //获取任务日志记录
    String getTaskServiceLogList(Map<String, String> map);

    //保存客户端日志
    String saveClientLog(Map<String, Object> map);

    //获取客户端日志记录
    String getTaskClientLogList(Map<String, String> map);

    //获取任务结果
    String getTaskResult(Map<String, Object> map);

    //运行任务
    String runTask(Map<String, Object> map);

    //删除任务
    void deleteTask(String taskId);
}
