package cn.cat.middleware.dbrouter.dynamic.pool.impl;

import cn.cat.middleware.dbrouter.dynamic.pool.ConnectPool;
import com.alibaba.fastjson.JSON;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class HikariConnectPool implements ConnectPool {
    private static final Logger logger = LoggerFactory.getLogger(HikariConnectPool.class);
    private HikariDataSource dataSource;

    @SuppressWarnings("unchecked")
    @Override
    public Object init(Map<String, Object> connectionProperties) {
        dataSource = new HikariDataSource();
        // 基础属性配置
        dataSource.setDriverClassName((String) connectionProperties.get("driver-class-name"));
        dataSource.setJdbcUrl(connectionProperties.get("url").toString());
        dataSource.setUsername(connectionProperties.get("username").toString());
        dataSource.setPassword(connectionProperties.get("password").toString());
        // 连接池属性配置
        Map<String, Object> poolProperties = (Map<String, Object>) connectionProperties.get("pool");
        dataSource.setMinimumIdle((int) poolProperties.get("minimum-idle"));
        dataSource.setIdleTimeout((int) poolProperties.get("idle-timeout"));
        dataSource.setMaximumPoolSize((int) poolProperties.get("maximum-pool-size"));
        dataSource.setAutoCommit((boolean) poolProperties.get("auto-commit"));
        dataSource.setMaxLifetime((int) poolProperties.get("max-lifetime"));
        dataSource.setConnectionTimeout((int) poolProperties.get("connection-timeout"));
        dataSource.setConnectionTestQuery((String) poolProperties.get("connection-test-query"));
        logger.info("HikariCP 连接池配置: {}", JSON.toJSONString(poolProperties));
        return dataSource;
    }

    @Override
    public void close() {
        dataSource.close();
    }
}
