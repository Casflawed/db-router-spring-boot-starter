package com.flameking.middleware.db.router.config;

import com.flameking.middleware.db.router.aspectj.DBRouterAspectj;
import com.flameking.middleware.db.router.strategy.IDBRouterStrategy;
import com.flameking.middleware.db.router.strategy.impl.DefaultDBRouterStrategy;
import com.flameking.middleware.db.router.support.DynamicDataSource;
import com.flameking.middleware.db.router.support.DynamicMybatisPlugin;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(DBRouterProperties.class)
public class DBRouterAutoConfig implements EnvironmentAware {
    // 默认数据源配置
    private final Map<String, String> defaultTargetDataSourceConfig = new HashMap<>();
    // 动态多数据源配置（配置上下文环境）
    private final Map<String, Map<String, String>> targetDataSourceConfig = new HashMap<>();
    // 动态数据库路由属性配置
    private final DBRouterProperties properties;

    public DBRouterAutoConfig(DBRouterProperties properties) {
        this.properties = properties;
    }

    @Bean(name = "db-router-point")
    //如果容器中不存在这个 Bean（组件），则触发指定行为（Bean 初始化并交给 Spring 管理），这个注解的作用就是保证当前Bean在容器中只有一个。
    @ConditionalOnMissingBean
    public DBRouterAspectj point(DBRouterProperties dbRouterProperties, IDBRouterStrategy dbRouterStrategy) {
        return new DBRouterAspectj(dbRouterProperties, dbRouterStrategy);
    }

    /**
     * 路由策略实现（实际上唯一的作用就是生成分库分表路由）
     */
    @Bean
    public IDBRouterStrategy dbRouterStrategy(DBRouterProperties properties) {
        return new DefaultDBRouterStrategy(properties);
    }

    /**
     * mybatis自定义拦截器，用于处理分表
     */
    @Bean
    public Interceptor plugin() {
        return new DynamicMybatisPlugin();
    }

    /**
     * 动态数据源（数据源上下文切换，用于实现数据库路由）
     */
    @Bean
    public DynamicDataSource dataSource(DBRouterProperties properties){
        DynamicDataSource dataSource = new DynamicDataSource();
        DriverManagerDataSource defaultDataSource = new DriverManagerDataSource(this.defaultTargetDataSourceConfig.get("url"), this.defaultTargetDataSourceConfig.get("username"), this.defaultTargetDataSourceConfig.get("password"));
        dataSource.setDefaultTargetDataSource(defaultDataSource);

        HashMap<Object, Object> dataSourceContext = new HashMap<>();
        List<String> dbList = properties.getDbList();
        dbList.forEach(key -> {
            Map<String, String> value = this.targetDataSourceConfig.get(key);
            dataSourceContext.put(key, new DriverManagerDataSource(value.get("url"), value.get("username"), value.get("password")));
        });
        dataSource.setTargetDataSources(dataSourceContext);

        return dataSource;
    }

    /**
     * 事务管理器，因为使用的是Mybatis（需要指定数据源），所以用的是DataSourceTransactionManager的实现
     */
    @Bean
    public PlatformTransactionManager platformTransactionManager(DataSource dataSource) {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager.setDataSource(dataSource);
        return transactionManager;
    }

    /**
     * 事务管理器模板类，用于简化事务操作<br>
     * 比如像回滚、提交这类操作，在事务操作中是存在重复使用的情况，事务管理器模板就进行了处理。
     */
    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(transactionManager);
        transactionTemplate.setPropagationBehaviorName("PROPAGATION_REQUIRED");
        return transactionTemplate;
    }

    @Override
    public void setEnvironment(Environment environment) {
        // 获取配置前缀
        Class<? extends DBRouterProperties> clazz = DBRouterProperties.class;
        Annotation[] declaredAnnotations = clazz.getDeclaredAnnotations();
        String prefix = ((ConfigurationProperties)declaredAnnotations[0]).prefix() + ".";

        // 获取默认数据源配置
        String defaultDataSourcePrefix = prefix + this.properties.getDbDefault() + ".";
        getDataSourceConfig(environment, defaultDataSourcePrefix, this.defaultTargetDataSourceConfig);

        // 获取数据源上下文配置
        List<String> dbList = this.properties.getDbList();
        dbList.forEach(db -> {
            HashMap<String, String> dataSourceConfigMap = new HashMap<>();
            String dataSourcePrefix = prefix + db + ".";
            getDataSourceConfig(environment, dataSourcePrefix, dataSourceConfigMap);

            this.targetDataSourceConfig.put(db, dataSourceConfigMap);
        });

    }

    private void getDataSourceConfig(Environment environment, String prefix, Map<String, String> dataSourceConfig) {
        String driverClassName = environment.getProperty(prefix + "driver-class-name");
        String url = environment.getProperty(prefix + "url");
        String username = environment.getProperty(prefix + "username");
        String password = environment.getProperty(prefix + "password");
        dataSourceConfig.put("driver-class-name", driverClassName);
        dataSourceConfig.put("url", url);
        dataSourceConfig.put("username", username);
        dataSourceConfig.put("password", password);
    }
}
