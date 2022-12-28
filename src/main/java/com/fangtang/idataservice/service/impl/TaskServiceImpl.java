package com.fangtang.idataservice.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fangtang.idataservice.IdataserviceApplication;
import com.fangtang.idataservice.mapper.TaskMapper;
import com.fangtang.idataservice.pojo.Result;
import com.fangtang.idataservice.pojo.Task;
import com.fangtang.idataservice.service.TaskService;
import com.fangtang.idataservice.utils.JsonUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * *
 *
 * @Author Mr.JIA
 * @Date 2022/12/10 17:20
 **/
@Service
public class TaskServiceImpl implements TaskService {

    private final static Logger logger = LoggerFactory.getLogger(IdataserviceApplication.class);

    @Value("${sparksql.db.host}")
    private String sparkSqlDbHost;
    @Value("${sparksql.db.port:3306}")
    private String sparkSqlDbPort;
    @Value("${sparksql.db.schema}")
    private String sparkSqlDbSchema;
    @Value("${sparksql.db.user}")
    private String sparkSqlDbUser;
    @Value("${sparksql.db.password}")
    private String sparkSqlDbPassword;
    @Value("${sparksql.jar}")
    private String sparkSqlJar;
    @Value("${sparksql.config.dir}")
    private String sparkSqlConfigDir;

    @Resource
    private TaskMapper taskMapper;

    /**
     * @Author Mr.JIA
     * @Description //TODO 获取任务信息列表（条件查询、分页）
     * @Date 17:29 2022/12/10
     * @Param [map]
     * @return java.lang.String
     **/
    @Override
    public String getTaskInfoList(Map<String, Object> map) {
        try {
            Integer currentPage = Integer.parseInt(map.get("currentPage").toString());
            Integer pagesize = Integer.parseInt(map.get("pageSize").toString());
            if (currentPage <= 0) {
                currentPage = 1;
            }
            if (pagesize <= 0) {
                pagesize = 10;
            }
            PageHelper.startPage(currentPage, pagesize);
            Page<Task> dataSetInfoList =taskMapper.getTaskInfoList(map);
            long total = dataSetInfoList.getTotal();
            List<Task> result = dataSetInfoList.getResult();
            for (Task task : result) {
                ArrayList<Map<String,Object>> calculationList =taskMapper.getJiSuanInfo(task.getTaskId());
                task.setCalculationList(calculationList);
            }
            Map<String,Object> resultMap = new HashMap<>();
            resultMap.put("total",total);
            resultMap.put("taskInfoList",result);
            return JSON.toJSONString(Result.success(200,"成功",resultMap));
        }catch (Exception e){
            logger.error("获取任务信息列表（条件查询、分页）异常：{}",e.toString());
            return JSON.toJSONString(Result.error(201,"失败"));
        }

    }

    /**
     * @Author Mr.JIA
     * @Description //TODO 创建任务
     * @Date 17:42 2022/12/10
     * @Param [map]
     * @return java.lang.String
     **/
    @Override
    @Transactional
    public void addTask(Map<String, Object> map) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
        logger.info("开始时间====>"+df.format(new Date()));
        map.put("taskStatus","未开始");
        map.put("taskDataType","数据库");
        String taskId = map.get("taskId").toString();
        taskMapper.saveTaskInfo(map);
        logger.info("-----------------新增任务信息成功--------------------");
        ArrayList<Map<String,Object>> calculationIds = (ArrayList<Map<String,Object>>) map.get("calculationIds");
        ArrayList<Map<String,Object>> dataSourceIds = (ArrayList<Map<String, Object>>) map.get("dataSourceIds");
        ArrayList<Map<String,Object>> outputIds = (ArrayList<Map<String, Object>>) map.get("outputIds");
        ArrayList<String> allTableList = new ArrayList<>();
        for (Map<String,Object> dataSourceMap : dataSourceIds) {
            taskMapper.saveDataSourceId(taskId,dataSourceMap.get("companyId").toString(),"未授权");
            Map<String,Object> sourceInfo = taskMapper.getSourceInfo(dataSourceMap.get("companyId").toString(),dataSourceMap.get("dataSetId").toString(),dataSourceMap.get("tableId").toString());
            dataSourceMap.put("sourceInfo",sourceInfo);
            ArrayList<String> tableList = (ArrayList<String>) dataSourceMap.get("tableNames");
            allTableList.addAll(tableList);
            this.getTableJson(sourceInfo.get("ipAddress").toString(),sourceInfo.get("config_name").toString(),tableList,taskId,map.get("sqlWords").toString());
        }
        logger.info("------------------获取表json文件成功-------------------------");
        boolean jarFlag = true;
        try {
            jarFlag = this.runJar(taskId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("------------------调用jar包成功-------------------------");
        Map<String, Object> stringObjectMap = new HashMap<>();
        if (jarFlag){
            taskMapper.saveTaskLog(taskId,"执行完成");
            try {
                stringObjectMap = this.encapsulateCalculationContent(allTableList, taskId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!ObjectUtil.isEmpty(stringObjectMap)){
            JSONObject jsonObject = new JSONObject();
            for (Map<String, Object> calculationId : calculationIds) {
                String[] ipAddresses = calculationId.get("ipAddress").toString().split(":");
                if ("1".equals(calculationId.get("roleLevel"))){
                    jsonObject.put("first_name",calculationId.get("companyName"));
                    jsonObject.put("first_ip_address",ipAddresses[0]);
                    jsonObject.put("first_port",ipAddresses[1]);
                }else if ("2".equals(calculationId.get("roleLevel"))){
                    jsonObject.put("second_name",calculationId.get("companyName"));
                    jsonObject.put("second_ip_address",ipAddresses[0]);
                    jsonObject.put("second_port",ipAddresses[1]);
                }else if ("3".equals(calculationId.get("roleLevel"))){
                    jsonObject.put("third_name",calculationId.get("companyName"));
                    jsonObject.put("third_ip_address",ipAddresses[0]);
                    jsonObject.put("third_port",ipAddresses[1]);
                }
            }

            for (Map<String, Object> outputId : outputIds) {
                String[] ipAddresses = outputId.get("ipAddress").toString().split(":");
                jsonObject.put("output_name",outputId.get("companyName"));
                jsonObject.put("output_ip_address",ipAddresses[0]);
                jsonObject.put("output_port",ipAddresses[1]);
            }

            ArrayList<Map<String,Object>> inputPartyInfo = new ArrayList<>();
            ArrayList<Map<String,Object>> inputTableInfo = new ArrayList<>();
            for (Map<String, Object> dataSourceId : dataSourceIds) {
                Map<String,Object> sourceInfo = (Map<String, Object>) dataSourceId.get("sourceInfo");
                String[] ipAddresses = sourceInfo.get("ipAddress").toString().split(":");
                Map<String,Object> inputPartyMap = new HashMap<>();
                inputPartyMap.put("party_name",sourceInfo.get("company_name"));
                inputPartyMap.put("ip_address",ipAddresses[0]);
                inputPartyMap.put("port",ipAddresses[1]);
                inputPartyInfo.add(inputPartyMap);

                ArrayList<String> tableList = (ArrayList<String>) dataSourceId.get("tableNames");
                for (String tableName : tableList) {
                    Map<String,Object> inputTableMap = new HashMap<>();
                    inputTableMap.put("table",tableName);
                    inputTableMap.put("input_party_name",sourceInfo.get("company_name"));
                    inputTableInfo.add(inputTableMap);
                }

            }
            jsonObject.put("input_party_info",inputPartyInfo);
            jsonObject.put("input_table_info",inputTableInfo);

            stringObjectMap.put("testParam",jsonObject);
            for (Map<String,Object> calculationId : calculationIds) {
                calculationId.put("taskId",taskId);
                Map<String, Object> finalStringObjectMap = stringObjectMap;
                if (calculationId.get("roleLevel").equals("1")){
                    taskMapper.saveCalculationId(calculationId);
                    String calculationIpAddress = calculationId.get("ipAddress").toString();
                    finalStringObjectMap.put("isInputParty","false");
                    finalStringObjectMap.put("companyId",calculationId.get("companyId"));
                    finalStringObjectMap.put("companyName",calculationId.get("companyName"));
                    Map<String,String> sendIpAddress = taskMapper.getSendIpAddress(calculationId.get("companyId"));
                    String ipAddress = calculationIpAddress.split(":")[0]+":"+sendIpAddress.get("client_port");
                    for (Map<String, Object> dataSourceId : dataSourceIds) {
                        Map<String,Object> sourceInfo = (Map<String, Object>) dataSourceId.get("sourceInfo");
                        String sourceIpAddress = sourceInfo.get("ipAddress").toString();
                        if (ipAddress.equals(sourceIpAddress)){
                            finalStringObjectMap.put("isInputParty","true");
                        }
                    }
                    finalStringObjectMap.put("isOutputParty","false");
                    for (Map<String, Object> outputId : outputIds) {
                        String outputIpAddress = outputId.get("ipAddress").toString();
                        if (calculationIpAddress.equals(outputIpAddress)){
                            finalStringObjectMap.put("isOutputParty","true");
                        }
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            finalStringObjectMap.put("databaseName",sendIpAddress.get("config_name"));
                            finalStringObjectMap.put("order","first");
                            finalStringObjectMap.put("isComputeParty","true");
                            String path = "http://"+ipAddress+"/idataclient/clientComputing";
                            logger.info(ipAddress+"====>"+finalStringObjectMap);
                            String result1 =
                                    HttpRequest.post(path)
                                            .body(JSON.toJSONString(finalStringObjectMap), "application/json")
                                            .execute()
                                            .body();
                            logger.info(ipAddress+"=======接收客户端返回结果===>"+result1);
                        }
                    }).start();
                    try {
                        Thread.sleep(3000);
                        break;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }

            for (Map<String,Object> calculationId : calculationIds) {
                calculationId.put("taskId",taskId);
                Map<String, Object> finalStringObjectMap = stringObjectMap;
                if (calculationId.get("roleLevel").equals("2")){
                    taskMapper.saveCalculationId(calculationId);
                    String calculationIpAddress = calculationId.get("ipAddress").toString();
                    finalStringObjectMap.put("isInputParty","false");
                    finalStringObjectMap.put("companyId",calculationId.get("companyId"));
                    finalStringObjectMap.put("companyName",calculationId.get("companyName"));
                    Map<String,String> sendIpAddress = taskMapper.getSendIpAddress(calculationId.get("companyId"));
                    String ipAddress = calculationIpAddress.split(":")[0]+":"+sendIpAddress.get("client_port");
                    for (Map<String, Object> dataSourceId : dataSourceIds) {
                        Map<String,Object> sourceInfo = (Map<String, Object>) dataSourceId.get("sourceInfo");
                        String sourceIpAddress = sourceInfo.get("ipAddress").toString();
                        if (ipAddress.equals(sourceIpAddress)){
                            finalStringObjectMap.put("isInputParty","true");
                        }
                    }
                    finalStringObjectMap.put("isOutputParty","false");
                    for (Map<String, Object> outputId : outputIds) {
                        String outputIpAddress = outputId.get("ipAddress").toString();
                        if (calculationIpAddress.equals(outputIpAddress)){
                            finalStringObjectMap.put("isOutputParty","true");
                        }
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            finalStringObjectMap.put("databaseName",sendIpAddress.get("config_name"));
                            finalStringObjectMap.put("order","second");
                            finalStringObjectMap.put("isComputeParty","true");
                            String path = "http://"+ipAddress+"/idataclient/clientComputing";
                            logger.info(ipAddress+"====>"+finalStringObjectMap);
                            String result1 =
                                    HttpRequest.post(path)
                                            .body(JSON.toJSONString(finalStringObjectMap), "application/json")
                                            .execute()
                                            .body();
                            logger.info(ipAddress+"=======接收客户端返回结果===>"+result1);
                        }
                    }).start();
                    try {
                        Thread.sleep(3000);
                        break;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
            for (Map<String,Object> calculationId : calculationIds) {
                calculationId.put("taskId",taskId);
                Map<String, Object> finalStringObjectMap = stringObjectMap;
                if (calculationId.get("roleLevel").equals("3")){
                    taskMapper.saveCalculationId(calculationId);
                    String calculationIpAddress = calculationId.get("ipAddress").toString();
                    finalStringObjectMap.put("isInputParty","false");
                    finalStringObjectMap.put("companyId",calculationId.get("companyId"));
                    finalStringObjectMap.put("companyName",calculationId.get("companyName"));
                    Map<String,String> sendIpAddress = taskMapper.getSendIpAddress(calculationId.get("companyId"));
                    String ipAddress = calculationIpAddress.split(":")[0]+":"+sendIpAddress.get("client_port");
                    for (Map<String, Object> dataSourceId : dataSourceIds) {
                        Map<String,Object> sourceInfo = (Map<String, Object>) dataSourceId.get("sourceInfo");
                        String sourceIpAddress = sourceInfo.get("ipAddress").toString();
                        if (ipAddress.equals(sourceIpAddress)){
                            finalStringObjectMap.put("isInputParty","true");
                        }
                    }
                    finalStringObjectMap.put("isOutputParty","false");
                    for (Map<String, Object> outputId : outputIds) {
                        String outputIpAddress = outputId.get("ipAddress").toString();
                        if (calculationIpAddress.equals(outputIpAddress)){
                            finalStringObjectMap.put("isOutputParty","true");
                        }
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            finalStringObjectMap.put("databaseName",sendIpAddress.get("config_name"));
                            finalStringObjectMap.put("order","third");
                            finalStringObjectMap.put("isComputeParty","true");
                            String path = "http://"+ipAddress+"/idataclient/clientComputing";
                            logger.info(ipAddress+"====>"+path);
                            String result1 =
                                    HttpRequest.post(path)
                                            .body(JSON.toJSONString(finalStringObjectMap), "application/json")
                                            .execute()
                                            .body();
                            logger.info(ipAddress+"=======接收客户端返回结果===>"+result1);
                        }
                    }).start();
                    break;
                }
            }
        }
        logger.info("--------------------推送成功---------------");
        for (Map<String,Object> outputId : outputIds) {
            taskMapper.saveOutputId(taskId,outputId.get("companyId").toString());
        }
        logger.info("结束时间====>"+df.format(new Date()));
    }

    //获取源方表json数据写入服务器本地文件中
    private void getTableJson(String serviceAddress,String databaseName,ArrayList<String> tableList,String taskId,String sqlStr){
        String taskConfigDir = sparkSqlConfigDir + taskId;
        String tableJsonDir = taskConfigDir + "/tableJson/";
        logger.info("filePath----> {}", tableJsonDir);
        File saveFile = new File(tableJsonDir);
        if(!saveFile.exists()){
            saveFile.mkdirs();
        }
        logger.info("--------------------"+serviceAddress+"开始获取json文件--------------------");
        for (String tableName : tableList) {
            String interfacePath = "http://"+serviceAddress+"/idataclient/getTableJson/"+databaseName+"/"+tableName;
            String result =
                    HttpRequest.get(interfacePath)
                            .execute()
                            .body();
            logger.info(serviceAddress+"====获取json文件=======>"+result);
            JsonUtils.bean2JsonFile(result, taskConfigDir + "/tableJson/" + tableName + ".json");
        }
        logger.info("--------------------"+serviceAddress+"结束获取json文件--------------------");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("serverName", sparkSqlDbHost + ":" + sparkSqlDbPort);
        jsonObject.put("databaseName", sparkSqlDbSchema);
        jsonObject.put("userName", sparkSqlDbUser);
        jsonObject.put("passwd", sparkSqlDbPassword);
        jsonObject.put("tableJsonPath", taskConfigDir + "/tableJson");
        jsonObject.put("outputPath", taskConfigDir + "/MySQL_plan.json");
        jsonObject.put("querysql",sqlStr);
        JsonUtils.bean2JsonFile(jsonObject.toString(), taskConfigDir + "/config.json");
    }

    //运行jar包，获取运行结果
    private boolean runJar(String taskId) throws Exception {
        logger.info("-----------启动jar包输出日志开始----------------------");
        try {
            //运行jar包程序“textencode.jar”，需要运行那个改成那个jar包名称即可
            String jarPath = "java -jar " + sparkSqlJar + " " + sparkSqlConfigDir + taskId + "/config.json";
            Process exec = Runtime.getRuntime().exec(jarPath);
            InputStream inputStream = exec.getInputStream();
            InputStream errorStream = exec.getErrorStream();
            BufferedReader br2 = new BufferedReader(new InputStreamReader(errorStream,"gb2312"));
            String line2 = null;
            while((line2 = br2.readLine()) != null) {
                if (line2.contains("com.mysql.jdbc.Driver")){
                    logger.info(line2);
                    taskMapper.saveTaskLog(taskId,line2);
                    BufferedReader br1 = new BufferedReader(new InputStreamReader(inputStream,"gb2312"));
                    String line1 = null;
                    while((line1 = br1.readLine()) != null) {
                        logger.info(line1);
                        taskMapper.saveTaskLog(taskId,line1);
                    }
                }else {
                    logger.info(line2);
                    taskMapper.saveTaskLog(taskId,line2);
                }
            }
        } catch (IOException e) {
            logger.error("运行失败");
            e.printStackTrace();
        }
        logger.info("-----------启动jar包输出日志结束----------------------");
        BufferedReader br=null;
        String MySQL_planfpath = sparkSqlConfigDir + taskId + "/MySQL_plan.json";
        File MySQL_planfile = new File(MySQL_planfpath);
        String configStr = "";
        FileInputStream MySQL_planfis = new FileInputStream(MySQL_planfile);
        InputStreamReader MySQL_planisr = new InputStreamReader(MySQL_planfis);//避免中文乱码
        br = new BufferedReader(MySQL_planisr);
        String MySQL_planstr_line="";
        //逐行读取文本
        while ((MySQL_planstr_line=br.readLine())!=null){
            configStr += MySQL_planstr_line;
        }
        JSONArray jsonArray = JSONArray.parseArray(configStr);
        for (Object o : jsonArray) {
            Map<String,Object> map = (Map<String, Object>) o;
            if (map.containsKey("flag")){
                taskMapper.saveTaskLog(taskId,"jar包运行失败");
                return false;
            }
        }
        return true;
    }

    //封装推送数据
    private Map<String,Object> encapsulateCalculationContent(ArrayList<String> allTableList,String taskId) throws Exception {
        Map<String,Object> resultMap = new HashMap<>();
        ArrayList<Map<String,String>> tableList1 = new ArrayList<>();
        BufferedReader br=null;
        for (String tableName : allTableList) {
            Map<String,String> jsonMap = new HashMap<>();
            String jsonStr = "";
            String fpath = sparkSqlConfigDir + taskId + "/tableJson/" + tableName + ".json";
            //String fpath = "D:\\Tables\\tableJson\\"+tableName+".json";
            File file1 = new File(fpath);
            FileInputStream fis = new FileInputStream(file1);
            InputStreamReader isr = new InputStreamReader(fis);//避免中文乱码
            br = new BufferedReader(isr);
            String str_line="";
            //逐行读取文本
            while ((str_line=br.readLine())!=null){
                jsonStr += str_line;
            }
            jsonMap.put("tableName",tableName);
            jsonMap.put("jsonStr",jsonStr);
            tableList1.add(jsonMap);
        }

        BufferedReader br1=null;
        String MySQL_planfpath = sparkSqlConfigDir + taskId + "/MySQL_plan.json";
        File MySQL_planfile = new File(MySQL_planfpath);
        String configStr = "";
        FileInputStream MySQL_planfis = new FileInputStream(MySQL_planfile);
        InputStreamReader MySQL_planisr = new InputStreamReader(MySQL_planfis);//避免中文乱码
        br1 = new BufferedReader(MySQL_planisr);
        String MySQL_planstr_line="";
        //逐行读取文本
        while ((MySQL_planstr_line=br1.readLine())!=null){
            configStr += MySQL_planstr_line;
        }
        resultMap.put("config",configStr);
        resultMap.put("tableList",tableList1);
        resultMap.put("taskId",taskId);
        return resultMap;
    }

    /**
     * @Author Mr.JIA
     * @Description //TODO 获取任务详情
     * @Date 20:51 2022/12/10
     * @Param [map]
     * @return java.lang.String
     **/
    @Override
    public String getTaskDetails(Map<String, Object> map) {
        try {
            String taskId = (String) map.get("taskId");
            Map<String,Object> taskInfo = taskMapper.getTaskInfo(taskId);
            ArrayList<Map<String,Object>> calculationInfo = taskMapper.getCalczulationInfo(taskId);
            ArrayList<Map<String,Object>> outPutInfo = taskMapper.getOutPutInfo(taskId);
            ArrayList<Map<String,Object>> dataSourceInfo = taskMapper.getDataSourceInfo(taskId);
            taskInfo.put("calculationInfo",calculationInfo);
            taskInfo.put("dataSourceInfo",dataSourceInfo);
            taskInfo.put("outPutInfo",outPutInfo);
            return JSON.toJSONString(Result.success(200,"成功",taskInfo));
        }catch (Exception e){
            logger.error("获取任务详情异常：{}",e.toString());
            return JSON.toJSONString(Result.error(201,"失败"));
        }

    }

    /**
     * @Author Mr.JIA
     * @Description //TODO 获取任务日志记录
     * @Date 21:44 2022/12/13
     * @Param [map]
     * @return java.lang.String
     **/
    @Override
    public String getTaskServiceLogList(Map<String, String> map) {
        try {
            ArrayList<Map<String,Object>> logList = taskMapper.getTaskServiceLogList(map);
            if (logList.size() > 0){
                if ("执行完成".equals(logList.get(logList.size() - 1).get("log_content")) || "jar包运行失败".equals(logList.get(logList.size() - 1).get("log_content"))){
                    return JSON.toJSONString(Result.success(202,"",logList));
                }
            }
            return JSON.toJSONString(Result.success(200,"成功",logList));
        }catch (Exception e){
            logger.error("获取任务日志记录异常：{}",e.toString());
            return JSON.toJSONString(Result.error(201,"失败"));
        }

    }

    /**
     * @Author Mr.JIA
     * @Description //TODO 保存客户端日志
     * @Date 20:47 2022/12/14
     * @Param [map]
     * @return java.lang.String
     **/
    @Override
    public String saveClientLog(Map<String, Object> map) {
    try {
        String companyId = (String) map.get("companyId");

        String taskId = (String) map.get("taskId");
        ArrayList<String> logs = (ArrayList<String>) map.get("logs");

        int start = 0;
        while (start < logs.size()) {
            int end = start + 50;
            if (end > logs.size()) {
                end = logs.size();
            }
            List<String> subList = logs.subList(start, end);
            int num = taskMapper.saveClientLog(taskId, companyId, logs.subList(start, end));
            start = end;
        }
        return JSON.toJSONString(Result.success(200,"成功"));
    }catch (Exception e){
        logger.error("保存客户端日志异常：{}",e.toString());
        return JSON.toJSONString(Result.error(201,"失败"));
    }

    }

    /**
     * @Author Mr.JIA
     * @Description //TODO 获取客户端日志记录
     * @Date 20:56 2022/12/14
     * @Param [map]
     * @return java.lang.String
     **/
    @Override
    public String getTaskClientLogList(Map<String, String> map) {
        try {
            ArrayList<Map<String,Object>> logList = taskMapper.getTaskClientLogList(map);
            if (logList.size() > 0){
                if ("执行完成".equals(logList.get(logList.size() - 1).get("log_content")) || "日志获取错误".equals(logList.get(logList.size() - 1).get("log_content")) ){
                    return JSON.toJSONString(Result.success(202,"",logList));
                }
            }
            if (logList.size() == 0){
                Map<String,Object> map1 = new HashMap<>();
                map1.put("createtime",map.get("startTime"));
                logList.add(map1);
            }

            return JSON.toJSONString(Result.success(200,"成功",logList));
        }catch (Exception e){
            logger.error("获取客户端日志记录异常：{}",e.toString());
            return JSON.toJSONString(Result.error(201,"失败"));
        }

    }

    /**
     * @Author Mr.JIA
     * @Description //TODO 获取任务结果
     * @Date 16:49 2022/12/16
     * @Param [map]
     * @return java.lang.String
     **/
    @Override
    public String getTaskResult(Map<String, Object> map) {
        try {
            Map<String,String> sendIpAddress = taskMapper.getSendIpAddress(map.get("companyId"));
            Map<String,Object> sendInfo = new HashMap<>();
            sendInfo.put("taskId",map.get("taskId"));
            String result1 =
                    HttpRequest.post("http://"+sendIpAddress.get("server_ip")+":"+sendIpAddress.get("client_port")+"/idataclient/getResult")
                            .body(JSON.toJSONString(sendInfo), "application/json")
                            .execute()
                            .body();
            JSONObject jsonObject = JSONObject.parseObject(result1);
            if (jsonObject.get("code").toString().equals("200")){
                taskMapper.updateTaskStatus(map.get("taskId").toString(),"已完成");
            }else {
                taskMapper.updateTaskStatus(map.get("taskId").toString(),"异常");
            }
            return result1;
        }catch (Exception e){
            logger.error("获取任务结果异常：{}",e.toString());
            return JSON.toJSONString(Result.error(201,"失败"));
        }

    }

    /**
     * @Author Mr.JIA
     * @Description //TODO 运行任务
     * @Date 12:58 2022/12/17
     * @Param [map]
     * @return java.lang.String
     **/
    @Override
    public String runTask(Map<String, Object> map) {
        try {
            String taskId = map.get("taskId").toString();
            ArrayList<String> companyIds = taskMapper.getCompanyIds(taskId);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (String companyId : companyIds) {
                        Map<String,Object> companyInfo = taskMapper.getClientSort(companyId,taskId);
                        if ("1".equals(companyInfo.get("role_level")+"")){
                            Map<String,Object> sendInfo = new HashMap<>();
                            sendInfo.put("taskId",map.get("taskId"));
                            sendInfo.put("companyId",companyId);
                            String result1 =
                                    HttpRequest.post("http://"+companyInfo.get("server_ip")+":"+companyInfo.get("client_port")+"/idataclient/runClientJarAndGetResult")
                                            .body(JSON.toJSONString(sendInfo), "application/json")
                                            .execute()
                                            .body();
                            logger.info(companyInfo.get("server_ip")+"成功jar运行"+result1);
                        }
                    }
                }
            }).start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (String companyId : companyIds) {
                        Map<String,Object> companyInfo = taskMapper.getClientSort(companyId,taskId);
                        if ("2".equals(companyInfo.get("role_level")+"")){
                            Map<String,Object> sendInfo = new HashMap<>();
                            sendInfo.put("taskId",map.get("taskId"));
                            sendInfo.put("companyId",companyId);
                            String result1 =
                                    HttpRequest.post("http://"+companyInfo.get("server_ip")+":"+companyInfo.get("client_port")+"/idataclient/runClientJarAndGetResult")
                                            .body(JSON.toJSONString(sendInfo), "application/json")
                                            .execute()
                                            .body();
                            logger.info(companyInfo.get("server_ip")+"成功jar运行"+result1);
                        }
                    }
                }
            }).start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (String companyId : companyIds) {
                        Map<String,Object> companyInfo = taskMapper.getClientSort(companyId,taskId);
                        if ("3".equals(companyInfo.get("role_level")+"")){
                            Map<String,Object> sendInfo = new HashMap<>();
                            sendInfo.put("taskId",map.get("taskId"));
                            sendInfo.put("companyId",companyId);
                            String result1 =
                                    HttpRequest.post("http://"+companyInfo.get("server_ip")+":"+companyInfo.get("client_port")+"/idataclient/runClientJarAndGetResult")
                                            .body(JSON.toJSONString(sendInfo), "application/json")
                                            .execute()
                                            .body();
                            logger.info(companyInfo.get("server_ip")+"成功jar运行"+result1);
                        }
                    }
                }
            }).start();
            return JSON.toJSONString(Result.success(200,"任务成功启动"));
        }catch (Exception e){
            logger.error("运行任务异常："+e.toString());
            return JSON.toJSONString(Result.error(200,"运行任务失败"));
        }

    }

    /**
     * @Author Mr.JIA
     * @Description //TODO 删除任务信息
     * @Date 14:16 2022/12/17
     * @Param [taskId]
     * @return void
     **/
    @Override
    @Transactional
    public void deleteTask(String taskId) {
        taskMapper.deleteTaskInfo(taskId);//删除任务基本信息
        taskMapper.deleteTaskSource(taskId);//删除任务资源方
        taskMapper.deleteTaskOutInput(taskId);//删除任务输出方
        taskMapper.deleteTaskCalculation(taskId);//删除任务计算方
        taskMapper.deleteTaskClientLog(taskId);//删除任务客户端日志
        taskMapper.deleteTaskServiceLog(taskId);//删除任务服务端日志
    }

}
