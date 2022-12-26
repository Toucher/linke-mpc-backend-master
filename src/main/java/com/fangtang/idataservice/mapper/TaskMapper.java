package com.fangtang.idataservice.mapper;

import com.fangtang.idataservice.pojo.Task;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * *
 *
 * @Author Mr.JIA
 * @Date 2022/12/10 17:22
 **/
public interface TaskMapper {

    //获取任务信息列表（条件查询、分页）
    Page<Task> getTaskInfoList(Map<String, Object> map);

    //保存任务信息
    void saveTaskInfo(Map<String, Object> map);

    //
    void saveCalculationId(Map<String,Object> map);

    void saveDataSourceId(@Param("taskId") String taskId,
                          @Param("companyId") String dataSourceId,
                            @Param("status") String status);

    void saveOutputId(@Param("taskId") String taskId,
                      @Param("company_id") String company_id);


    //获取获取任务信息
    Map<String, Object> getTaskInfo(String taskId);

    //获取表json服务器信息
    Map<String, Object> getSourceInfo(@Param("companyId") String companyId,@Param("dataSetId") String dataSetId,@Param("tableId") String tableId);

    //保存任务日志
    void saveTaskLog(@Param("taskId") String taskId,
                     @Param("logContent") String logContent);

    //获取任务日志记录
    ArrayList<Map<String, Object>> getTaskServiceLogList(Map<String, String> map);

    Map<String, String> getSendIpAddress(Object companyId);

    //保存客户端日志
    int saveClientLog(@Param("taskId") String taskId,@Param("companyId") String companyId,@Param("logs") List<String> logs);

    //获取客户端日志记录
    ArrayList<Map<String, Object>> getTaskClientLogList(Map<String, String> map);

    //获取计算法IP信息及运行顺序
    Map<String, Object> getClientSort(@Param("companyId") String companyId,@Param("taskId") String taskId);

    //删除任务基本信息
    void deleteTaskInfo(String taskId);

    //删除任务资源方
    void deleteTaskSource(String taskId);

    //删除任务输出方
    void deleteTaskOutInput(String taskId);

    //删除任务计算方
    void deleteTaskCalculation(String taskId);

    //删除任务客户端日志
    void deleteTaskClientLog(String taskId);

    //删除任务服务端日志
    void deleteTaskServiceLog(String taskId);

    //获取任务计算方companyId
    ArrayList<String> getCompanyIds(String taskId);

    ArrayList<Map<String, Object>> getJiSuanInfo(String taskId);

    ArrayList<Map<String, Object>> getCalczulationInfo(String taskId);

    ArrayList<Map<String, Object>> getOutPutInfo(String taskId);

    ArrayList<Map<String, Object>> getDataSourceInfo(String taskId);

    //修改任务状态
    void updateTaskStatus(@Param("taskId") String taskId,@Param("status") String status);
}
