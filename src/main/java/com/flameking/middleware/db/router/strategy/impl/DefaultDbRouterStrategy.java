package com.flameking.middleware.db.router.strategy.impl;

import com.flameking.middleware.db.router.config.DbRouterConfigureProperties;
import com.flameking.middleware.db.router.strategy.IDbRouterStrategy;
import com.flameking.middleware.db.router.support.DataSourceContextHolder;

public class DefaultDbRouterStrategy implements IDbRouterStrategy {
    private final DbRouterConfigureProperties dbRouterConfigureProperties;

    public DefaultDbRouterStrategy(DbRouterConfigureProperties dbRouterConfigureProperties) {
        this.dbRouterConfigureProperties = dbRouterConfigureProperties;
    }

    public void doRouter(String val){
        Integer dbCount = dbRouterConfigureProperties.getDbCount();
        Integer tbCount = dbRouterConfigureProperties.getTbCount();
        int size = dbCount * tbCount;

        // 扰动函数；16正好是32的一半，而32位数的高位和低位进行异或运算能够让低位呈现出更多的高位的特征，使得计算出来的hash值更不容易碰撞
        int idx = (size - 1) & (val.hashCode() ^ (val.hashCode() >>> 16));

        //计算库索引和表索引
        int dbKey = idx / tbCount + 1;
        int tbKey = idx - (dbKey - 1) * tbCount;

        DataSourceContextHolder.setDbKey(dbKey);
        DataSourceContextHolder.setTbKey(tbKey);
    }
}
