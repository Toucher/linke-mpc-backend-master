package com.fangtang.idataservice.mapper;

import com.fangtang.idataservice.pojo.DataSet;
import com.fangtang.idataservice.pojo.DataSource;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.Map;

public interface DataSourceMapper {

    //保存数据源信息
    void addDataSource(DataSource dataSource);

    //保存表结构
    void saveDataSourceRelationTable(Map<String, String> map);

    //获取数据源下拉内容
    ArrayList<Map<String, Object>> getDataSourceTables(@Param("companyId") String companyId);

    //保存数据集
    void addDataSet(Map<String,Object> map);

    //获取表格对应的数据源编号
    String getDataSourceId(String tableId);

    //保存数据集与表格关联关系
    void saveDataSetRelationTable(@Param("dataSetId") String dataSetId,
                                  @Param("dataSourceId")String dataSourceId,
                                  @Param("tableId")String tableId);

    //获取数据集列表（条件查询、分页）
    Page<DataSet> getDataSetInfoList(Map<String, String> map);

    //获取数据集详情
    Map<String, Object> getDataSetDetails(String dataSetId);

    //获取表结构
    String getTableFiled(String tableId);

    //获取排序字段
    ArrayList<Map<String, Object>> getSortFiled(@Param("dataSetId") String dataSetId,
                                                @Param("tableId") String tableId);

    //获取主外键字段
    ArrayList<Map<String, Object>> getKeyFiled(@Param("dataSetId") String dataSetId,
                                               @Param("tableId") String tableId);

    //删除排序字段
    void deleteSortFiled(@Param("dataSetId") String dataSetId,
                         @Param("tableId") String tableId);

    //刪除主鍵字段
    void deleteKeyFiled(@Param("dataSetId") String dataSetId,
                        @Param("tableId") String tableId);

    //保存排序字段
    void saveSortFiled(Map<String, Object> objectMap);

    //获取数据集名称下拉
    ArrayList<Map<String, Object>> getDataSetList();

    //获取数据源信息及表格
    ArrayList<Map<String, Object>> getDataSourceAndTables(String dataSetId);

    //获取数据源及其下面的库表
    ArrayList<Map<String, Object>> getDataSources(String dataSourceId);

    //校验数据库是否已存在
    int checkDataBase(DataSource dataSource);

    Page<DataSet> getDataSetInfoListOut(Map<String, String> map);

    //获取第三方信息下拉
    ArrayList<Map<String, Object>> getThirdPartyList();

    //获取数据集下的表结构
    ArrayList<Map<String, Object>> getTableFiledList(String resourceId);

    //删除数据集基本信息
    void deleteDataSetInfo(String dataSetId);

    //删除数据集与表的关联信息
    void deleteDataSetRelationTable(String dataSetId);

    //校验第三方是否存在
    int checkClientInfo(Map<String, Object> map);

    //保存第三方信息
    void saveClientInfo(Map<String, Object> map);
}
