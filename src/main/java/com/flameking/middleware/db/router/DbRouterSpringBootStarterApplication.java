package com.flameking.middleware.db.router;

import com.alibaba.fastjson.JSON;
import com.flameking.middleware.db.router.config.DbRouterAutoConfig;
import com.flameking.middleware.db.router.config.DbRouterConfigureProperties;
import com.flameking.middleware.db.router.config.DynamicDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DbRouterSpringBootStarterApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DbRouterSpringBootStarterApplication.class, args);
        DynamicDataSource dataSource = context.getBean(DynamicDataSource.class);
        System.out.println(JSON.toJSONString(dataSource));
    }

}
