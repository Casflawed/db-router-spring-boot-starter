package com.flameking.middleware.db.router.strategy;

public interface IDBRouterStrategy {
    /**
     * 执行分库分表路由
     *
     * @param param 路由算法入参
     */
    void doRouter(String param);

    /**
     * 手动设置分库路由
     *
     * @param dbIdx 路由库，需要在配置范围内
     */
    void setDbKey(int dbIdx);

    /**
     * 手动设置分表路由
     *
     * @param tbIdx 路由表，需要在配置范围内
     */
    void setTbKey(int tbIdx);

    /**
     * 获取分库数
     *
     * @return 数量
     */
    int dbCount();

    /**
     * 获取分表数
     *
     * @return 数量
     */
    int tbCount();

}
