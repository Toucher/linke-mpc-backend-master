package com.fangtang.idataservice.controller;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fangtang.idataservice.IdataserviceApplication;
import com.fangtang.idataservice.pojo.DataSet;
import com.fangtang.idataservice.pojo.DataSource;
import com.fangtang.idataservice.pojo.Result;
import com.fangtang.idataservice.service.DataSourceService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * *
 *
 * @Author Mr.JIA
 * @Date 2022/12/10 14:24
 **/
@RestController
@RequestMapping("/dataSource")
public class DataSourceController {
    private final static Logger logger = LoggerFactory.getLogger(DataSourceController.class);


    @Resource
    private DataSourceService dataSourceService;

    @ApiOperation(value = "新增数据源")
    @PostMapping("/addDataSource")
    @CrossOrigin
    public String addDataSource(@RequestBody DataSource dataSource){
        try {
            int num = dataSourceService.checkDataBase(dataSource);
            if (num == 0){
                String interfacePath = "http://"+dataSource.getInterfaceIp()+":"+dataSource.getInterfacePort()+"/idataclient/getTableStructure?name="+dataSource.getConfigName();
                String result =
                        HttpRequest.get(interfacePath)
                                .execute()
                                .body();
                logger.info("result====>"+result);
                JSONObject jsonObject = JSONObject.parseObject(result);
                dataSourceService.addDataSource(jsonObject,dataSource);
                /*调用方法生成json,存在服务器某个位置*/
                return JSON.toJSONString(Result.success(200,"成功"));
            }else {
                return JSON.toJSONString(Result.success(201,"该数据库结构已存在"));
            }
        }catch (Exception e){
            logger.error("新增数据源异常：{}",e.toString());
            return JSON.toJSONString(Result.success(201,"失败"));
        }
    }

    @ApiOperation(value = "获取数据源下拉内容")
    @GetMapping("/getDataSourceTables")
    @CrossOrigin
    public String getDataSourceTables(@RequestParam String companyId){
        return dataSourceService.getDataSourceTables(companyId);
    }

    @ApiOperation(value = "新增数据集")
    @PostMapping("/addDataSet")
    @CrossOrigin
    public String addDataSet(@RequestBody Map<String,Object> map){
        try {
            dataSourceService.addDataSet(map);
            return JSON.toJSONString(Result.success(200,"成功"));
        }catch (Exception e){
            logger.error("新增数据集异常：{}",e.toString());
            return JSON.toJSONString(Result.success(201,"失败"));
        }
    }



    @ApiOperation(value = "获取数据集列表（条件查询、分页）")
    @PostMapping("/getDataSetInfoList")
    @CrossOrigin
    public String getDataSetInfoList(@RequestBody Map<String,String> map){
        return dataSourceService.getDataSetInfoList(map);
    }

    @ApiOperation(value = "获取数据集详情")
    @PostMapping("/getDataSetDetails")
    @CrossOrigin
    public String getDataSetDetails(@RequestBody Map<String,String> map){
        return dataSourceService.getDataSetDetails(map);
    }

    @ApiOperation(value = "获取数据集下表结构")
    @PostMapping("/getDataSetTableDetails")
    @CrossOrigin
    public String getDataSetTableDetails(@RequestBody Map<String,String> map){
        return dataSourceService.getDataSetTableDetails(map);
    }

    @ApiOperation(value = "修改排序")
    @PostMapping("/updateDataSetTableFiled")
    @CrossOrigin
    public String updateDataSetTableFiled(@RequestBody Map<String,Object> map){
        try {
            dataSourceService.updateDataSetTableFiled(map);
            return JSON.toJSONString(Result.success(200,"修改成功"));
        }catch (Exception e){
            logger.error("修改排序异常：{}",e.toString());
            return JSON.toJSONString(Result.error(201,"失败"));
        }

    }

    @ApiOperation(value = "测试连接")
    @PostMapping("/checkLink")
    @CrossOrigin
    public String checkLink(@RequestBody DataSource dataSource){
        int num = dataSourceService.checkDataBase(dataSource);
        if (num == 0){
            String interfacePath = "http://"+dataSource.getInterfaceIp()+":"+dataSource.getInterfacePort()+"/idataclient/checkTableStructure?name="+dataSource.getConfigName();
            String result =
                    HttpRequest.get(interfacePath)
                            .execute()
                            .body();
            if ("200".equals(result)){
                return JSON.toJSONString(Result.success(200,"连接成功"));
            }else {
                return JSON.toJSONString(Result.success(201,"连接失败"));
            }
        }else {
            return JSON.toJSONString(Result.success(201,"该数据库结构已存在"));
        }
    }

    @ApiOperation(value = "获取任务指定数据集内容")
    @GetMapping("/getDataSetList")
    @CrossOrigin
    public String getDataSetList(){
        return dataSourceService.getDataSetList();
    }

    @ApiOperation(value = "获取数据集下的表结构")
    @PostMapping("/getTableFiledList")
    @CrossOrigin
    public String getTableFiledList(@RequestBody Map<String,Object> map){
        return dataSourceService.getTableFiledList(map);
    }

    @ApiOperation(value = "获取数据源及其下面的库表")
    @PostMapping("/getDataSources")
    @CrossOrigin
    public String getDataSources(@RequestBody Map<String,String> map){
        return dataSourceService.getDataSources(map);
    }

    @ApiOperation(value = "获取第三方信息下拉")
    @GetMapping("/getThirdPartyList")
    @CrossOrigin
    public String getThirdPartyList(){
        return dataSourceService.getThirdPartyList();
    }

    @ApiOperation(value = "删除数据集")
    @PostMapping("/deleteDataSet")
    @CrossOrigin
    public String deleteDataSet(@RequestBody Map<String,String> map){
        try {
            if (map.get("dataSetId") == "" || map.get("dataSetId") == null){
                return JSON.toJSONString(Result.success(201,"参数为空"));
            }
            dataSourceService.deleteDataSet(map.get("dataSetId"));
            return JSON.toJSONString(Result.success(200,"删除成功"));
        }catch (Exception e){
            logger.error("删除数据集异常：{}",e.toString());
            return JSON.toJSONString(Result.error(201,"删除失败"));
        }
    }

    @ApiOperation(value = "保存第三方信息")
    @PostMapping("/saveClientInfo")
    @CrossOrigin
    public String saveClientInfo(@RequestBody Map<String,Object> map){
        return dataSourceService.saveClientInfo(map);
    }

    @ApiOperation(value = "输出参数")
    @PostMapping("/outputParameters")
    @CrossOrigin
    public String outputParameters(@RequestBody ArrayList<Map<String,Object>> list){
        return dataSourceService.outputParameters(list);
    }

}
