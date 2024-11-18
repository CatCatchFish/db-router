package cn.cat.middleware.dbrouter.dynamic.pool.impl;

import cn.cat.middleware.dbrouter.dynamic.pool.ConnectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.Map;

public class DefaultConnectPool implements ConnectPool {
    private static final Logger logger = LoggerFactory.getLogger(DefaultConnectPool.class);

    @Override
    public Object init(Map<String, Object> connectionProperties) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName((String) connectionProperties.get("driver-class-name"));
        dataSource.setUrl(connectionProperties.get("url").toString());
        dataSource.setUsername(connectionProperties.get("username").toString());
        dataSource.setPassword(connectionProperties.get("password").toString());
        logger.info("使用默认连接池初始化数据源");
        return dataSource;
    }

    @Override
    public void close() {

    }
}
