package com.fangtang.idataservice.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * *
 * 数据源对象
 * @Author Mr.JIA
 * @Date 2022/12/10 14:31
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DataSource {

    private String dataSourceId;//数据源编号
    private String dataSourceName;//数据源名称
    private String dataLibraryType;//数据库类型
    private String dataLibraryName;//数据库名称
    private String interfaceIp;//接口IP
    private String interfacePort;//接口端口
    private String companyName;//机构名称
    private String companyId;//机构id
    private String userId;//用户id
    private String configName;//


}
