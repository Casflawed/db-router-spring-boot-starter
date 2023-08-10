package com.flameking.middleware.db.router.deploy;

public class MyRouter {
    public void doRouter(){
        //读取配置文件的数据库配置

        //实例化我们的动态路由数据源

        //每次操作数据库后清楚记得清除掉ThreadLocal中的key

        //mapper方法作为切点

        //调用路由策略根据key计算数据库索引和表索引

        //获取路由key的值

        //保存库索引和表索引到ThreadLocal

        //mybatis执行sql前进行拦截替换掉对应的sql语句

    }
}
