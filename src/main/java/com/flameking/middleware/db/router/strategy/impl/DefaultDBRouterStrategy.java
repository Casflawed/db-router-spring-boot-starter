package com.flameking.middleware.db.router.strategy.impl;

import com.flameking.middleware.db.router.config.DBRouterProperties;
import com.flameking.middleware.db.router.strategy.IDBRouterStrategy;
import com.flameking.middleware.db.router.support.DataSourceContextHolder;

public class DefaultDBRouterStrategy implements IDBRouterStrategy {
    private final DBRouterProperties properties;

    public DefaultDBRouterStrategy(DBRouterProperties properties) {
        this.properties = properties;
    }

    public void doRouter(String param){
        Integer dbCount = properties.getDbCount();
        Integer tbCount = properties.getTbCount();
        int size = dbCount * tbCount;

        // 扰动函数；16正好是32的一半，而32位数的高位和低位进行异或运算能够让低位呈现出更多的高位的特征，使得计算出来的hash值更不容易碰撞
        int idx = (size - 1) & (param.hashCode() ^ (param.hashCode() >>> 16));

        // 计算库索引和表索引
        int dbKey = idx / tbCount + 1;
        int tbKey = idx - (dbKey - 1) * tbCount;

        // 绑定分库分表路由到当前线程
        DataSourceContextHolder.setDbKey(String.format("%02d", dbKey));
        DataSourceContextHolder.setTbKey(String.format("%03d", tbKey));
    }

    @Override
    public void setDbKey(int dbIdx) {
        DataSourceContextHolder.setDbKey(String.format("%02d", dbIdx));
    }

    @Override
    public void setTbKey(int tbIdx) {
        DataSourceContextHolder.setTbKey(String.format("%03d", tbIdx));
    }

    @Override
    public int dbCount() {
        return properties.getDbCount();
    }

    @Override
    public int tbCount() {
        return properties.getTbCount();
    }
}
