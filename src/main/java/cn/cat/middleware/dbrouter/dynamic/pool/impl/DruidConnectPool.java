package cn.cat.middleware.dbrouter.dynamic.pool.impl;

import cn.cat.middleware.dbrouter.dynamic.pool.ConnectPool;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Map;

public class DruidConnectPool implements ConnectPool {
    private static final Logger logger = LoggerFactory.getLogger(DruidConnectPool.class);
    private DruidDataSource dataSource;

    @SuppressWarnings("unchecked")
    @Override
    public Object init(Map<String, Object> connectionProperties) {
        try {
            dataSource = new DruidDataSource();
            // 基础属性配置
            dataSource.setDriverClassName((String) connectionProperties.get("driver-class-name"));
            dataSource.setUrl((String) connectionProperties.get("url"));
            dataSource.setUsername((String) connectionProperties.get("username"));
            dataSource.setPassword((String) connectionProperties.get("password"));
            // 连接池属性配置
            Map<String, Object> poolProperties = (Map<String, Object>) connectionProperties.get("pool-properties");
            dataSource.setInitialSize((int) poolProperties.get("initial-size"));
            dataSource.setMinIdle((int) poolProperties.get("min-idle"));
            dataSource.setMaxActive((int) poolProperties.get("max-active"));
            dataSource.setMaxWait((long) poolProperties.get("max-wait"));
            dataSource.setTimeBetweenConnectErrorMillis((long) poolProperties.get("time-between-eviction-runs-millis"));
            dataSource.setMinEvictableIdleTimeMillis((long) poolProperties.get("min-evictable-idle-time-millis"));
            dataSource.setTestWhileIdle((boolean) poolProperties.get("test-while-idle"));
            dataSource.setTestOnBorrow((boolean) poolProperties.get("test-on-borrow"));
            dataSource.setTestOnReturn((boolean) poolProperties.get("test-on-return"));
            dataSource.setPoolPreparedStatements((boolean) poolProperties.get("pool-prepared-statements"));
            dataSource.setMaxPoolPreparedStatementPerConnectionSize((int) poolProperties.get("max-pool-prepared-statement-per-connection-size"));
            dataSource.setFilters((String) poolProperties.get("filters"));
            dataSource.setConnectionProperties((String) poolProperties.get("connection-properties"));
            logger.info("Druid 连接池配置: {}", JSON.toJSONString(poolProperties));
            return dataSource;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void close() {
        dataSource.close();
    }
}
