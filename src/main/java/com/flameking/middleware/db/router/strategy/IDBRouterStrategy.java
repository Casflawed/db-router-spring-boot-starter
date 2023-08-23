package com.flameking.middleware.db.router.strategy;

public interface IDBRouterStrategy {
    /**
     * 执行分库分表路由
     *
     * @param param 路由算法入参
     */
    void doRouter(String param);
}
