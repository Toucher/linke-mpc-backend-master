package com.fangtang.idataservice.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.ArrayList;

/**
 * *
 * 数据集对象
 * @Author Mr.JIA
 * @Date 2022/12/10 15:26
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DataSet {

    private int id;//数据集编号
    private String resourceId;//资源id
    private String resourceName;//数据名称
    private String resourceDesc;//资源简介
    private String resourceInfo;//资源信息
    private String resourceType;//资源类型
    private String companyName;//所属公司
    private String companyId;//公司id
    private String keywords;//关键词
    private ArrayList<String> keyWordList;//关键词集合
    private String memberList;//资源可见性
    private ArrayList<String> memberIdList;//人员编码集合
    private String createUser;//创建者
    private String createTime;//创建时间
    private String updateUser;//修改人
    private String updateTime;//修改时间
    private int isDeleted;//伪删除标记
    private int projectNum;//项目数
    private String resourcePath;//资源路径
    private String publicLevel;//资源可见性
    private String useScene;//使用场景
    private String authorizationMethod;//授权方式
    private ArrayList<String> tableIds;//表格编号集合
}
