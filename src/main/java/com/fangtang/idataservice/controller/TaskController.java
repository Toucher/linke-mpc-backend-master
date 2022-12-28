package com.fangtang.idataservice.controller;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.fangtang.idataservice.IdataserviceApplication;
import com.fangtang.idataservice.mapper.TaskMapper;
import com.fangtang.idataservice.pojo.Result;
import com.fangtang.idataservice.pojo.Task;
import com.fangtang.idataservice.service.TaskService;
import com.fangtang.idataservice.utils.JsonUtils;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Map;

/**
 * *
 *
 * @Author Mr.JIA
 * @Date 2022/12/10 17:19
 **/
@RestController
@RequestMapping("/task")
public class TaskController {

    private final static Logger logger = LoggerFactory.getLogger(IdataserviceApplication.class);
    
    @Resource
    private TaskService taskService;

    @ApiOperation(value = "获取任务信息列表（条件查询、分页）")
    @PostMapping("/getTaskInfoList")
    @CrossOrigin
    public String getTaskInfoList(@RequestBody Map<String,Object> map){
        return taskService.getTaskInfoList(map);
    }

    @ApiOperation(value = "创建任务")
    @PostMapping("/addTask")
    @CrossOrigin
    public String addTask(@RequestBody Map<String,Object> map){
        try {
            taskService.addTask(map);
            return JSON.toJSONString(Result.success(200,"新增成功"));
        }catch (Exception e){
            logger.error("创建任务异常：{}",e.toString());
            return JSON.toJSONString(Result.success(201,"失败"));
        }

    }

    @ApiOperation(value = "获取任务详情")
    @PostMapping("/getTaskDetails")
    @CrossOrigin
    public String getTaskDetails(@RequestBody Map<String,Object> map){
        return taskService.getTaskDetails(map);
    }

    @ApiOperation(value = "获取服务端日志记录")
    @PostMapping("/getTaskServiceLogList")
    @CrossOrigin
    public String getTaskServiceLogList(@RequestBody Map<String,String> map){
        return taskService.getTaskServiceLogList(map);
    }

    @ApiOperation(value = "保存客户端日志记录")
    @PostMapping("/saveClientLog")
    @CrossOrigin
    public String saveClientLog(@RequestBody Map<String,Object> map){
        return taskService.saveClientLog(map);
    }

    @ApiOperation(value = "获取客户端日志记录")
    @PostMapping("/getTaskClientLogList")
    @CrossOrigin
    public String getTaskClientLogList(@RequestBody Map<String,String> map){
        return taskService.getTaskClientLogList(map);
    }

    @ApiOperation(value = "获取任务结果")
    @PostMapping("/getTaskResult")
    @CrossOrigin
    public String getTaskResult(@RequestBody Map<String,Object> map){
        return taskService.getTaskResult(map);
    }

    @CrossOrigin
    @ApiOperation(value = "运行任务")
    @PostMapping("/runTask")
    public String runTask(@RequestBody Map<String,Object> map){
        return taskService.runTask(map);
    }

    @CrossOrigin
    @ApiOperation(value = "删除任务信息")
    @PostMapping("/deleteTask")
    public String deleteTask(@RequestBody Map<String,String> map){
        try {
            if (map.get("taskId") == "" || map.get("taskId") == null){
                return JSON.toJSONString(Result.success(201,"参数为空"));
            }
            taskService.deleteTask(map.get("taskId"));
            return JSON.toJSONString(Result.success(200,"删除成功"));
        }catch (Exception e){
            System.out.println("删除任务信息异常："+e.toString());
            return JSON.toJSONString(Result.error(201,"删除失败"));
        }
    }
}
