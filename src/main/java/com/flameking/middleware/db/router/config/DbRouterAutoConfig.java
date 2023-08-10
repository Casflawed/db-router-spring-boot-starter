package com.flameking.middleware.db.router.config;

import com.flameking.middleware.db.router.aspectj.DbRouterAspectj;
import com.flameking.middleware.db.router.strategy.IDbRouterStrategy;
import com.flameking.middleware.db.router.strategy.impl.DefaultDbRouterStrategy;
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
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(DbRouterConfigureProperties.class)
public class DbRouterAutoConfig implements EnvironmentAware {
    private DbRouterConfigureProperties dbRouterConfigureProperties;
    //默认数据源配置
    private final Map<String, Object> defaultTargetDataSourceConfig = new HashMap<>();
    //上下文数据源配置
    private final Map<String, Map<String, Object>> targetDataSourceConfig = new HashMap<>();

    public DbRouterAutoConfig(DbRouterConfigureProperties dbRouterConfigureProperties) {
        this.dbRouterConfigureProperties = dbRouterConfigureProperties;
    }

    @Bean(name = "db-router-point")
    @ConditionalOnMissingBean
    public DbRouterAspectj point(DbRouterConfigureProperties dbRouterConfigureProperties, IDbRouterStrategy dbRouterStrategy) {
        return new DbRouterAspectj(dbRouterConfigureProperties, dbRouterStrategy);
    }

    @Bean
    public IDbRouterStrategy dbRouterStrategy(DbRouterConfigureProperties dbRouterConfigureProperties) {
        return new DefaultDbRouterStrategy(dbRouterConfigureProperties);
    }

    @Bean
    public Interceptor plugin() {
        return new DynamicMybatisPlugin();
    }

    @Bean
    public TransactionTemplate transactionTemplate(DataSource dataSource) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);

        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(dataSourceTransactionManager);
        transactionTemplate.setPropagationBehaviorName("PROPAGATION_REQUIRED");
        return transactionTemplate;
    }

    @Bean
    public DynamicDataSource dataSource(){
        DynamicDataSource dataSource = new DynamicDataSource();
        DriverManagerDataSource defaultDataSource = new DriverManagerDataSource(defaultTargetDataSourceConfig.get("url").toString(), defaultTargetDataSourceConfig.get("username").toString(), defaultTargetDataSourceConfig.get("password").toString());
        dataSource.setDefaultTargetDataSource(defaultDataSource);

        HashMap<Object, Object> dataSourceContext = new HashMap<>();
        List<String> dbList = dbRouterConfigureProperties.getDbList();
        dbList.forEach(key -> {
            Map<String, Object> value = targetDataSourceConfig.get(key);
            dataSourceContext.put(key, new DriverManagerDataSource(value.get("url").toString(), value.get("username").toString(), value.get("password").toString()));
        });
        dataSource.setTargetDataSources(dataSourceContext);

        return dataSource;
    }

    @Override
    public void setEnvironment(Environment environment) {
        //获取配置前缀
        Class<? extends DbRouterConfigureProperties> dbRouterConfigurePropertiesClass = dbRouterConfigureProperties.getClass();
        ConfigurationProperties annotation = dbRouterConfigurePropertiesClass.getAnnotation(ConfigurationProperties.class);
        String prefix = annotation.prefix();

        //获取默认数据源配置
        String defaultDataSourcePrefix = prefix + "." + dbRouterConfigureProperties.getDbDefault();
        getDataSourceConfig(environment, defaultDataSourcePrefix, defaultTargetDataSourceConfig);

        //获取数据源上下文配置
        List<String> dbList = dbRouterConfigureProperties.getDbList();
        dbList.forEach(db -> {
            HashMap<String, Object> dataSourceConfigMap = new HashMap<>();
            String dataSourcePrefix = prefix + "." + db;
            getDataSourceConfig(environment, dataSourcePrefix, dataSourceConfigMap);

            targetDataSourceConfig.put(db, dataSourceConfigMap);
        });

    }

    private void getDataSourceConfig(Environment environment, String prefix, Map configMap) {
        String driverClassName = environment.getProperty(prefix + "." + "driver-class-name");
        String url = environment.getProperty(prefix + "." + "url");
        String username = environment.getProperty(prefix + "." + "username");
        String password = environment.getProperty(prefix + "." + "password");
        configMap.put("driver-class-name", driverClassName);
        configMap.put("url", url);
        configMap.put("username", username);
        configMap.put("password", password);
    }
}
