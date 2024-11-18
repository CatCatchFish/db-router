package cn.cat.middleware.dbrouter.dynamic.pool.impl;

import cn.cat.middleware.dbrouter.dynamic.pool.ConnectPool;
import com.alibaba.fastjson.JSON;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyVetoException;
import java.util.Map;

public class C3p0ConnectPool implements ConnectPool {
    private static final Logger logger = LoggerFactory.getLogger(C3p0ConnectPool.class);
    private ComboPooledDataSource dataSource;

    @SuppressWarnings("unchecked")
    @Override
    public Object init(Map<String, Object> connectionProperties) {
        try {
            dataSource = new ComboPooledDataSource();
            // 基础属性配置
            dataSource.setDriverClass((String) connectionProperties.get("driver-class-name"));
            dataSource.setJdbcUrl((String) connectionProperties.get("url"));
            dataSource.setUser((String) connectionProperties.get("username"));
            dataSource.setPassword((String) connectionProperties.get("password"));
            // 连接池属性配置
            Map<String, Object> poolProperties = (Map<String, Object>) connectionProperties.get("pool");
            dataSource.setMinPoolSize((int) poolProperties.get("min-pool-size"));
            dataSource.setMaxPoolSize((int) poolProperties.get("max-pool-size"));
            dataSource.setMaxIdleTime((int) poolProperties.get("max-idle-time"));
            dataSource.setCheckoutTimeout((int) poolProperties.get("checkout-timeout"));
            dataSource.setAcquireIncrement((int) poolProperties.get("acquire-increment"));
            dataSource.setIdleConnectionTestPeriod((int) poolProperties.get("idle-connection-test-period"));
            dataSource.setTestConnectionOnCheckout((boolean) poolProperties.get("test-connection-on-checkout"));
            logger.info("C3p0 连接池配置: {}", JSON.toJSONString(poolProperties));
            return dataSource;
        } catch (PropertyVetoException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void close() {
        dataSource.close();
    }
}
