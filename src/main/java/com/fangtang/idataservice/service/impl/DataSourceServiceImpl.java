package com.fangtang.idataservice.service.impl;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fangtang.idataservice.mapper.DataSourceMapper;
import com.fangtang.idataservice.pojo.DataSet;
import com.fangtang.idataservice.pojo.DataSource;
import com.fangtang.idataservice.pojo.Result;
import com.fangtang.idataservice.service.DataSourceService;
import com.fangtang.idataservice.utils.IdRandomUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * *
 *
 * @Author Mr.JIA
 * @Date 2022/12/10 14:25
 **/
@Service
public class DataSourceServiceImpl implements DataSourceService {

    private final static Logger logger = LoggerFactory.getLogger(DataSourceServiceImpl.class);

    @Resource
    private DataSourceMapper dataSourceMapper;

    @Value("${server-port}")
    private String serverPort;

    @Value("${client-port}")
    private String clientPort;

    /**
     * @Author Mr.JIA
     * @Description //TODO 新增数据源
     * @Date 14:29 2022/12/10
     * @Param [map]
     * @return java.lang.String
     **/
    @Override
    @Transactional
    public void addDataSource(JSONObject jsonObject,DataSource dataSource) {
        if (Integer.parseInt(jsonObject.get("code").toString()) == 200){
            String dataSourceId = "ds"+ IdRandomUtils.getRandomID().toString();
            dataSource.setDataSourceId(dataSourceId);
            dataSourceMapper.addDataSource(dataSource);
            JSONArray jsonArray = (JSONArray) jsonObject.get("data");
            for (Object o : jsonArray) {
                Map<String, String> map = (Map<String, String>) o;
                map.put("dataSourceId",dataSourceId);
                map.put("tableId","tb"+IdRandomUtils.getRandomID().toString());
                dataSourceMapper.saveDataSourceRelationTable(map);
            }
        }
    }

    /**
     * @Author Mr.JIA
     * @Description //TODO 获取数据源下拉内容
     * @Date 15:13 2022/12/10
     * @Param []
     * @return java.lang.String
     **/
    @Override
    public String getDataSourceTables(String companyId) {
        try {
            ArrayList<Map<String,Object>> dataSourceTables = dataSourceMapper.getDataSourceTables(companyId);
            return JSON.toJSONString(Result.success(200,"",dataSourceTables));
        }catch (Exception e){
            logger.error("获取数据源下拉内容异常："+e.toString());
            return JSON.toJSONString(Result.success(201,"获取数据源下拉内容异常"));
        }
    }

    /**
     * @Author Mr.JIA
     * @Description //TODO 新增数据集
     * @Date 15:35 2022/12/10
     * @Param [dataSet]
     * @return java.lang.String
     **/
    @Override
    @Transactional
    public void addDataSet(Map<String,Object> map) {
        String dataSetId = "res" + IdRandomUtils.getRandomID().toString();
        map.put("resourceId",dataSetId);
        map.put("isDeleted",0);
        ArrayList<String> keyWordList = (ArrayList<String>) map.get("keyWordList");
        String keywords = "";
        for (int i = 0; i < keyWordList.size(); i++) {
            if (i == keyWordList.size() - 1){
                keywords += keyWordList.get(i);
            }else {
                keywords += keyWordList.get(i)+"、";
            }
        }
        map.put("keywords",keywords);

        ArrayList<String> memberIdList = (ArrayList<String>) map.get("memberIdList");
        String memberId = "";
        for (int i = 0; i < memberIdList.size(); i++) {
            if (i == memberIdList.size() - 1){
                memberId += memberIdList.get(i);
            }else {
                memberId += memberIdList.get(i)+"、";
            }
        }
        map.put("memberList",memberId);
        dataSourceMapper.addDataSet(map);//保存数据集
        if ("数据库".equals(map.get("resourceType").toString())){
            ArrayList<String> tableIds = (ArrayList<String>) map.get("tableIds");
            for (String tableId : tableIds) {
                String dataSourceId = dataSourceMapper.getDataSourceId(tableId);//获取表格对应的数据源编号
                dataSourceMapper.saveDataSetRelationTable(dataSetId,dataSourceId,tableId);//保存数据集与表格关联关系
            }
        }

        JSONObject jsonObject = new JSONObject();
        if ("mysql".equals(map.get("resource_type"))){
            Map<String,Object> map1 = new HashMap<>();
            map1.put("role","");
            map1.put("dataset_type","");
            map1.put("host","");
            map1.put("user","");
            map1.put("password","");
            map1.put("database","");
            map1.put("table","");
            map1.put("target_fields","");
            map1.put("excluding_fields","");
            map1.put("row_threshold","");
            map1.put("column_threshold","");
            jsonObject.put("mysql_params",map1);
            jsonObject.put("data_source","mysql");
        }else if ("csv".equals(map.get("resource_type"))){
            Map<String,Object> map2 = new HashMap<>();
            map2.put("role","");
            map2.put("abs_path","");
            map2.put("dataset_type","");
            map2.put("delimiter","");
            map2.put("has_header","");
            map2.put("row_threshold","");
            map2.put("column_threshold","");
            jsonObject.put("csv_params",map2);
            jsonObject.put("data_source","csv");
        }else if ("excel".equals(map.get("resource_type"))){
            Map<String,Object> map3 = new HashMap<>();
            map3.put("role","");
            map3.put("abs_path","");
            map3.put("dataset_type","");
            map3.put("has_header","");
            map3.put("row_threshold","");
            map3.put("column_threshold","");
            jsonObject.put("excel_params",map3);
            jsonObject.put("data_source","excel");
        }else if ("json".equals(map.get("resource_type"))){
            Map<String,Object> map4 = new HashMap<>();
            map4.put("role","");
            map4.put("abs_path","");
            map4.put("dataset_type","");
            map4.put("data_field","");
            map4.put("row_threshold","");
            map4.put("column_threshold","");
            jsonObject.put("json_params",map4);
            jsonObject.put("data_source","json");
        }else if ("api".equals(map.get("resource_type"))){
            Map<String,Object> map5 = new HashMap<>();
            map5.put("role","");
            map5.put("url","");
            map5.put("dataset_type","");
            map5.put("key_name","");
            map5.put("key_value","");
            map5.put("data_field","");
            map5.put("row_threshold","");
            map5.put("column_threshold","");
            jsonObject.put("api_params",map5);
            jsonObject.put("data_source","api");
        }
        String result1 =
                HttpRequest.post("http://10.10.10.81:15001/dataio/load_dataset")
                        .body(jsonObject.toString(), "application/json")
                        .execute()
                        .body();
        logger.info("调用结果：{}",result1);

    }

    /**
     * @Author Mr.JIA
     * @Description //TODO 获取数据集列表（条件查询、分页）
     * @Date 15:57 2022/12/10
     * @Param [map]
     * @return java.lang.String
     **/
    @Override
    public String getDataSetInfoList(Map<String, String> map) {
        try {
            Integer currentPage = Integer.parseInt(map.get("currentPage"));
            Integer pagesize = Integer.parseInt(map.get("pageSize"));
            if (currentPage <= 0) {
                currentPage = 1;
            }
            if (pagesize <= 0) {
                pagesize = 10;
            }
            Map<String,Object> resultMap = new HashMap<>();
            if (Integer.parseInt(map.get("ifLocalhost").toString()) == 0){
                PageHelper.startPage(currentPage, pagesize);
                Page<DataSet> dataSetInfoList =dataSourceMapper.getDataSetInfoList(map);
                long total = dataSetInfoList.getTotal();
                List<DataSet> result = dataSetInfoList.getResult();
                resultMap.put("size",pagesize);
                resultMap.put("current",currentPage);
                long pages = 0;
                long num1 = total % pagesize;
                if (num1 == 0){
                    pages = total / pagesize;
                }else {
                    pages = total / pagesize + 1;
                }
                resultMap.put("pages",pages);
                resultMap.put("total",total);
                resultMap.put("records",result);
            }else {
                PageHelper.startPage(currentPage, pagesize);
                Page<DataSet> dataSetInfoList =dataSourceMapper.getDataSetInfoListOut(map);
                long total = dataSetInfoList.getTotal();
                List<DataSet> result = dataSetInfoList.getResult();
                resultMap.put("size",pagesize);
                resultMap.put("current",currentPage);
                long pages = 0;
                long num1 = total % pagesize;
                if (num1 == 0){
                    pages = total / pagesize;
                }else {
                    pages = total / pagesize + 1;
                }
                resultMap.put("pages",pages);
                resultMap.put("total",total);
                resultMap.put("records",result);
            }
            return JSON.toJSONString(Result.success(200,"成功",resultMap));
        }catch (Exception e){
            logger.error("获取数据集列表（条件查询、分页）异常：{}",e.toString());
            return JSON.toJSONString(Result.error(201,"失败"));
        }
    }

    /**
     * @Author Mr.JIA
     * @Description //TODO 获取数据集详情
     * @Date 16:15 2022/12/10
     * @Param [map]
     * @return java.lang.String
     **/
    @Override
    public String getDataSetDetails(Map<String, String> map) {
        try {
            String dataSetId = map.get("dataSetId");
            Map<String,Object> dataSetInfo = dataSourceMapper.getDataSetDetails(dataSetId);
            ArrayList<Map<String,Object>> sourceInfo = dataSourceMapper.getDataSourceAndTables(dataSetId);
            for (Map<String, Object> stringObjectMap : sourceInfo) {
                ArrayList<Map<String,Object>> tableList = (ArrayList<Map<String, Object>>) stringObjectMap.get("tableList");
                for (Map<String, Object> objectMap : tableList) {
                    ArrayList<String> tableFiledList = (ArrayList<String>) objectMap.get("tableFiledList");
                    String[] split = tableFiledList.get(0).split("#");
                    objectMap.put("tableFiledList",split);
                }
            }
            dataSetInfo.put("dataSourceList",sourceInfo);
            return JSON.toJSONString(Result.success(200,"成功",dataSetInfo));
        }catch (Exception e){
            logger.error("获取数据集详情异常：{}",e.toString());
            return JSON.toJSONString(Result.error(201,"失败"));
        }

    }

    /**
     * @Author Mr.JIA
     * @Description //TODO 获取数据集下表结构
     * @Date 16:33 2022/12/10
     * @Param [map]
     * @return java.lang.String
     **/
    @Override
    public String getDataSetTableDetails(Map<String, String> map) {
        try {
            String tableId = map.get("tableId");
            String dataSetId = map.get("dataSetId");
            String filedStr = dataSourceMapper.getTableFiled(tableId);
            Map<String,Object> resultMap = new HashMap<>();
            if (filedStr != null && filedStr != ""){
                String[] fileds = filedStr.split("#");
                resultMap.put("fileds",fileds);
            }
            ArrayList<Map<String,Object>> sortFileds = dataSourceMapper.getSortFiled(dataSetId,tableId);
            ArrayList<Map<String,Object>> keyFileds = dataSourceMapper.getKeyFiled(dataSetId,tableId);

            resultMap.put("sortFileds",sortFileds);
            resultMap.put("keyFileds",keyFileds);
            return JSON.toJSONString(Result.success(200,"成功",resultMap));
        }catch (Exception e){
            logger.error("获取数据集下表结构异常：{}",e.toString());
            return JSON.toJSONString(Result.error(201,"失败"));
        }

    }

    /**
     * @Author Mr.JIA
     * @Description //TODO 修改排序及主外键
     * @Date 16:56 2022/12/10
     * @Param [map]
     * @return java.lang.String
     **/
    @Override
    @Transactional
    public void updateDataSetTableFiled(Map<String, Object> map) {
        String tableId = (String) map.get("tableId");
        String dataSetId = (String) map.get("dataSetId");
        ArrayList<Map<String,Object>> sortList = (ArrayList<Map<String, Object>>) map.get("sort");
        dataSourceMapper.deleteSortFiled(dataSetId,tableId);
        for (Map<String, Object> objectMap : sortList) {
            objectMap.put("dataSetId",dataSetId);
            objectMap.put("tableId",tableId);
            dataSourceMapper.saveSortFiled(objectMap);
        }
    }

    /**
     * @Author Mr.JIA
     * @Description //TODO 获取数据集名称下拉
     * @Date 20:22 2022/12/10
     * @Param []
     * @return java.lang.String
     **/
    @Override
    public String getDataSetList() {
        try {
            ArrayList<Map<String,Object>> dataSetList = dataSourceMapper.getDataSetList();
            return JSON.toJSONString(Result.success(200,"成功",dataSetList));
        }catch (Exception e){
            logger.error("获取数据集名称下拉异常：{}",e.toString());
            return JSON.toJSONString(Result.error(201,"失败"));
        }

    }

    /**
     * @Author Mr.JIA
     * @Description //TODO 获取数据集下的表结构
     * @Date 9:46 2022/12/15
     * @Param [map]
     * @return java.lang.String
     **/
    @Override
    public String getTableFiledList(Map<String, Object> map) {
        try {
            ArrayList<Map<String,Object>> tableList = dataSourceMapper.getTableFiledList(map.get("resourceId").toString());
            for (Map<String, Object> map1 : tableList) {
                ArrayList<String> tableFiledList = (ArrayList<String>) map1.get("tableFiledList");
                String[] split = tableFiledList.get(0).split("#");
                tableFiledList.clear();
                for (String s : split) {
                    tableFiledList.add(s);
                }
            }
            return JSON.toJSONString(Result.success(200,"成功",tableList));
        }catch (Exception e){
            logger.error("获取数据集下的表结构异常：{}",e.toString());
            return JSON.toJSONString(Result.error(201,"失败"));
        }

    }

    /**
     * @Author Mr.JIA
     * @Description //TODO 删除数据集
     * @Date 14:36 2022/12/17
     * @Param [dataSetId]
     * @return void
     **/
    @Override
    @Transactional
    public void deleteDataSet(String dataSetId) {
        dataSourceMapper.deleteDataSetInfo(dataSetId);//删除数据集基本信息
        dataSourceMapper.deleteDataSetRelationTable(dataSetId);//删除数据集与表的关联信息
    }

    /**
     * @Author Mr.JIA
     * @Description //TODO 保存第三方信息
     * @Date 21:43 2022/12/17
     * @Param [map]
     * @return java.lang.String
     **/
    @Override
    public String saveClientInfo(Map<String, Object> map) {
        try {
            int num = dataSourceMapper.checkClientInfo(map);
            if (num == 0){
                map.put("serverPort", serverPort);
                map.put("clientPort", clientPort);
                //这个端口写死了，实际业务场景需要配置用户数据库端口，不能在这里固定成一样的
                map.put("configName",map.get("serverIp").toString()+"_12001_linkempc");
                dataSourceMapper.saveClientInfo(map);
            }
            return JSON.toJSONString(Result.success(200,"成功"));
        }catch (Exception e){
            logger.error("保存第三方信息异常：{}",e.toString());
            return JSON.toJSONString(Result.error(201,"失败"));
        }

    }

    /**
     * @Author Mr.JIA
     * @Description //TODO 输出参数
     * @Date 18:18 2022/12/28
     * @Param [list]
     * @return java.lang.String
     **/
    @Override
    public String outputParameters(ArrayList<Map<String, Object>> list) {
        logger.info("list===> {}",list);
        for (Map<String, Object> stringObjectMap : list) {
            System.out.println("task_id====>"+stringObjectMap.get("task_id"));
            ArrayList<Map<String,Object>> list1 = (ArrayList<Map<String, Object>>) stringObjectMap.get("analysis_res_pics");
            for (Map<String, Object> objectMap : list1) {
                System.out.println("name====>"+objectMap.get("name"));
                System.out.println("content====>"+objectMap.get("content"));
            }
        }
        return null;
    }

    /**
     * @Author Mr.JIA
     * @Description //TODO 获取数据源及其下面的库表
     * @Date 14:08 2022/12/11
     * @Param [map]
     * @return java.lang.String
     **/
    @Override
    public String getDataSources(Map<String, String> map) {
        try {
            String dataSourceId = map.get("dataSourceId");
            ArrayList<Map<String,Object>> dataSources = dataSourceMapper.getDataSources(dataSourceId);
            return JSON.toJSONString(Result.success(200,"成功",dataSources));
        }catch (Exception e){
            logger.error("获取数据源及其下面的库表异常：{}",e.toString());
            return JSON.toJSONString(Result.error(201,"失败"));
        }

    }

    /**
     * @Author Mr.JIA
     * @Description //TODO 校验数据库是否已存在
     * @Date 15:15 2022/12/11
     * @Param [dataSource]
     * @return int
     **/
    @Override
    public int checkDataBase(DataSource dataSource) {
        return dataSourceMapper.checkDataBase(dataSource);
    }

    /**
     * @Author Mr.JIA
     * @Description //TODO 获取第三方信息下拉
     * @Date 14:16 2022/12/14
     * @Param []
     * @return java.lang.String
     **/
    @Override
    public String getThirdPartyList() {
        try {
            ArrayList<Map<String,Object>> thirdPartyList = dataSourceMapper.getThirdPartyList();
            return JSON.toJSONString(Result.success(200,"成功",thirdPartyList));
        }catch (Exception e){
            logger.error("获取第三方信息下拉异常：{}",e.toString());
            return JSON.toJSONString(Result.error(201,"失败"));
        }

    }
}
