package com.flameking.middleware.db.router.support;

import com.flameking.middleware.db.router.annotation.DBRouterStrategy;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mybatis 拦截器，通过对 SQL 语句的拦截处理，修改分表信息
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class DynamicMybatisPlugin implements Interceptor {
    //其中Pattern.CASE_INSENSITIVE是忽略大小写标志，正则表达式学习网站：https://regexlearn.com/zh-cn/learn/regex101
    private final Pattern pattern = Pattern.compile("(from|into|update)[\\s]{1,}(\\w{1,})", Pattern.CASE_INSENSITIVE);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //获取StatementHandler
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        //mybatis的反射工具箱，metaObject中保存了原始的JavaBean对象，可用于获取属性，设置属性等操作
        MetaObject metaObject = MetaObject.forObject(statementHandler, SystemMetaObject.DEFAULT_OBJECT_FACTORY, SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
        //拿到mappedStatement属性，MappedStatement这个类存储了一个sql的所有信息；
        //Mybatis 通过解析 XML 和 mapper 接口上的注解，生成 sql 对应的 MappedStatement 实例，并放入 SqlSessionTemplate 中 configuration 类属性中
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");

        //获取自定义注解判断是否进行分表操作
        String id = mappedStatement.getId();
        //id是方法的全限定名
        String className = id.substring(0, id.lastIndexOf("."));
        //获取mapper接口的对象
        Class<?> clazz = Class.forName(className);
        DBRouterStrategy dbRouterStrategy = clazz.getAnnotation(DBRouterStrategy.class);
        //不分表则直接放行
        if (null == dbRouterStrategy || !dbRouterStrategy.splitTable()){
            return invocation.proceed();
        }

        // 获取SQL
        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = boundSql.getSql();
        // 替换SQL表名 USER 为 USER_03
        Matcher matcher = pattern.matcher(sql);
        String tableName = null;
        //匹配第一次出现的表名，如果要匹配多个表名，则需要循环调用matcher.find()
        if (matcher.find()) {
            //matcher.group() = matcher.group(0)：即匹配到的整个字符串，group(g)用法见：https://blog.csdn.net/chensijum/article/details/106871569
            tableName = matcher.group().trim();
        }
        assert null != tableName;
        String replaceSql = matcher.replaceAll(tableName + "_" + DataSourceContextHolder.getTbKey());

        // 通过反射修改SQL语句
        Field field = boundSql.getClass().getDeclaredField("sql");
        field.setAccessible(true);
        field.set(boundSql, replaceSql);
        field.setAccessible(false);

        return invocation.proceed();
    }

}
