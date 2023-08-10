package com.flameking.middleware.db.router.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(DbRouterConfigureProperties.class)
public class DbRouterAutoConfig implements EnvironmentAware {
    private final DbRouterConfigureProperties dbRouterConfigureProperties;
    //默认数据源配置
    private final Map<String, Object> defaultTargetDataSourceConfig = new HashMap<>();
    //上下文数据源配置
    private final Map<String, Map<String, Object>> targetDataSourceConfig = new HashMap<>();

    public DbRouterAutoConfig(DbRouterConfigureProperties dbRouterConfigureProperties) {
        this.dbRouterConfigureProperties = dbRouterConfigureProperties;
    }

    @Bean
    public DynamicDataSource initDynamicDataSource(){
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
