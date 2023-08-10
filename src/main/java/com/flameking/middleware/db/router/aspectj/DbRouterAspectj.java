package com.flameking.middleware.db.router.aspectj;

import com.flameking.middleware.db.router.annotation.DbRouter;
import com.flameking.middleware.db.router.config.DbRouterConfigureProperties;
import com.flameking.middleware.db.router.support.DataSourceContextHolder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Aspect
@Component
public class DbRouterAspectj {
    @Autowired
    private DbRouterConfigureProperties dbRouterConfigureProperties;

    @Pointcut("@annotation(com.flameking.middleware.db.router.annotation.DbRouter)")
    public void pointCut() {
    }

    @Around("pointCut() && @annotation(dbRouter)")
    public Object doRouter(ProceedingJoinPoint jp, DbRouter dbRouter) throws Throwable {
        //从注解中获取到计算库索引和表索引的key
        String routerKey = dbRouter.key();
        if (StringUtils.isEmpty(routerKey)){
            routerKey = dbRouterConfigureProperties.getRouterKey();
        }
        //调用表库索引计算策略

        //放行orm执行sql
        Object returnValue = jp.proceed();

        //清空ThreadLocal
        DataSourceContextHolder.clear();

        return returnValue;
    }
}
