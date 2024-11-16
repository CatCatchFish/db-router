package cn.cat.middleware.dbrouter.config;

import cn.cat.middleware.dbrouter.DBRouterConfig;
import cn.cat.middleware.dbrouter.dynamic.DynamicDataSource;
import cn.cat.middleware.dbrouter.util.PropertyUtil;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;


import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Cat
 * @description 解析数据源配置
 */
@Configuration
public class DataSourceAutoConfig implements EnvironmentAware {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceAutoConfig.class);

    /**
     * 数据源配置组
     * value：数  据源详细信息
     */
    private final Map<String, Map<String, Object>> dataSourceMap = new HashMap<>();

    /**
     * 分库数量
     */
    private int dbCount;

    /**
     * 分表数量
     */
    private int tbCount;

    /**
     * 将DB的信息注入到spring中，供后续获取
     *
     * @return
     */
    @Bean
    public DBRouterConfig dbRouterConfig() {
        return new DBRouterConfig(dbCount, tbCount);
    }

    @Bean
    public DataSource dataSource() {
        Map<Object, Object> targetDataSources = new HashMap<>();
        for (String dbInfo : dataSourceMap.keySet()) {
            Map<String, Object> objMap = dataSourceMap.get(dbInfo);
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(objMap.get("driver-class-name").toString());
            dataSource.setUrl(objMap.get("url").toString());
            dataSource.setUsername(objMap.get("username").toString());
            dataSource.setPassword(objMap.get("password").toString());
            targetDataSources.put(dbInfo, dataSource);
            logger.info("load datasource: " + JSON.toJSONString(dataSource));
        }

        // 设置数据源
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setTargetDataSources(targetDataSources);
        return dynamicDataSource;
    }

    /**
     * 解析数据源配置
     *
     * @param environment 配置信息
     */
    @Override
    public void setEnvironment(Environment environment) {
        String prefix = "mini-db-router.jdbc.datasource.";

        dbCount = Integer.parseInt(Objects.requireNonNull(environment.getProperty(prefix + "dbCount")));
        tbCount = Integer.parseInt(Objects.requireNonNull(environment.getProperty(prefix + "tbCount")));

        String dataSources = environment.getProperty(prefix + "list");

        assert dataSources != null;
        for (String dbInfo : dataSources.split(",")) {
            Map<String, Object> dataSourceProps = PropertyUtil.handle(environment, prefix + dbInfo, Map.class);
            dataSourceMap.put(dbInfo, dataSourceProps);
        }
    }

}
