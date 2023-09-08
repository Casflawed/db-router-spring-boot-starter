package com.flameking.middleware.db.router.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "mini-db-router.jdbc.datasource")
public class DBRouterProperties {
    // 分库数量
    private Integer dbCount;
    // 分表数量
    private Integer tbCount;
    // 默认库
    private String dbDefault;
    // 路由键
    private String routerKey;
    // 分库列表
    private List<String> dbList;

    public Integer getDbCount() {
        return dbCount;
    }

    public void setDbCount(Integer dbCount) {
        this.dbCount = dbCount;
    }

    public Integer getTbCount() {
        return tbCount;
    }

    public void setTbCount(Integer tbCount) {
        this.tbCount = tbCount;
    }

    public String getDbDefault() {
        return dbDefault;
    }

    public void setDbDefault(String dbDefault) {
        this.dbDefault = dbDefault;
    }

    public String getRouterKey() {
        return routerKey;
    }

    public void setRouterKey(String routerKey) {
        this.routerKey = routerKey;
    }

    public List<String> getDbList() {
        return dbList;
    }

    public void setDbList(List<String> dbList) {
        this.dbList = dbList;
    }


}
