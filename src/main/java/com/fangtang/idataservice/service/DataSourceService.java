package com.fangtang.idataservice.service;

import com.alibaba.fastjson.JSONObject;
import com.fangtang.idataservice.pojo.DataSet;
import com.fangtang.idataservice.pojo.DataSource;

import java.util.ArrayList;
import java.util.Map;


public interface DataSourceService {

    //新增数据源
    void addDataSource(JSONObject jsonObject,DataSource dataSource);

    //获取数据源下拉内容
    String getDataSourceTables(String companyId);

    //新增数据集
    void addDataSet(Map<String,Object> map);

    //获取数据集列表（条件查询、分页）
    String getDataSetInfoList(Map<String, String> map);

    //获取数据集详情
    String getDataSetDetails(Map<String, String> map);

    //获取数据集下表结构
    String getDataSetTableDetails(Map<String, String> map);

    //修改排序及主外键
    void updateDataSetTableFiled(Map<String, Object> map);

    //获取数据集名称下拉
    String getDataSetList();

    //获取数据源及其下面的库表
    String getDataSources(Map<String, String> map);

    //校验数据库是否已存在
    int checkDataBase(DataSource dataSource);

    //获取第三方信息下拉
    String getThirdPartyList();

    //获取数据集下的表结构
    String getTableFiledList(Map<String, Object> map);

    //删除数据集
    void deleteDataSet(String dataSetId);

    //保存第三方信息
    String saveClientInfo(Map<String, Object> map);

    //输出参数
    String outputParameters(ArrayList<Map<String, Object>> list);
}
