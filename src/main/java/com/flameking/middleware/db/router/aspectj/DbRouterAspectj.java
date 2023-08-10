package com.flameking.middleware.db.router.aspectj;

import com.flameking.middleware.db.router.annotation.DbRouter;
import com.flameking.middleware.db.router.config.DbRouterConfigureProperties;
import com.flameking.middleware.db.router.strategy.IDbRouterStrategy;
import com.flameking.middleware.db.router.support.DataSourceContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;

@Aspect
public class DbRouterAspectj {
    private final DbRouterConfigureProperties dbRouterConfigureProperties;
    private final IDbRouterStrategy dbRouterStrategy;

    public DbRouterAspectj(DbRouterConfigureProperties dbRouterConfigureProperties, IDbRouterStrategy dbRouterStrategy) {
        this.dbRouterConfigureProperties = dbRouterConfigureProperties;
        this.dbRouterStrategy = dbRouterStrategy;
    }

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
        //获取路由key的值
        String routerKeyValue = getAttrValue(routerKey, jp.getArgs());

        //调用表库索引计算策略
        dbRouterStrategy.doRouter(routerKeyValue);

        //放行orm执行sql
        Object returnValue = jp.proceed();

        //清空ThreadLocal
        DataSourceContextHolder.clear();

        return returnValue;
    }

    public String getAttrValue(String attr, Object[] args) {
        if (1 == args.length) {
            Object arg = args[0];
            if (arg instanceof String) {
                return arg.toString();
            }
        }

        String filedValue = null;
        try {
            for (Object arg : args) {
                if (!StringUtils.isEmpty(filedValue)) {
                    break;
                }
                // filedValue = BeanUtils.getProperty(arg, attr);
                // fix: 使用lombok时，uId这种字段的get方法与idea生成的get方法不同，会导致获取不到属性值，改成反射获取解决
                filedValue = String.valueOf(this.getValueByName(arg, attr));

            }
        } catch (Exception e) {
//                logger.error("获取路由属性值失败 attr：{}", attr, e);
        }
        return filedValue;
    }

    /**
     * 获取对象的特定属性值
     *
     * @author tang
     * @param obj 对象
     * @param name 属性名
     * @return 属性值
     */
    private Object getValueByName(Object obj, String name) {
        try {
            Field field = getFieldByName(obj, name);
            if (field == null) {
                return null;
            }
            field.setAccessible(true);
            Object o = field.get(obj);
            field.setAccessible(false);
            return o;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /**
     * 根据名称获取方法，该方法同时兼顾继承类获取父类的属性
     *
     * @author tang
     * @param item 对象
     * @param name 属性名
     * @return 该属性对应方法
     */
    private Field getFieldByName(Object item, String name) {
        try {
            Field field;
            try {
                field = item.getClass().getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                field = item.getClass().getSuperclass().getDeclaredField(name);
            }
            return field;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}
