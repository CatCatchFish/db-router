package cn.cat.middleware.dbrouter.config;

import cn.cat.middleware.dbrouter.DBRouterConfig;
import cn.cat.middleware.dbrouter.dynamic.pool.ConnectPool;
import cn.cat.middleware.dbrouter.dynamic.pool.factory.ConnectPoolFactory;
import cn.cat.middleware.dbrouter.dynamic.DynamicDataSource;
import cn.cat.middleware.dbrouter.type.PoolType;
import cn.cat.middleware.dbrouter.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Cat
 * @description 解析数据源配置
 */
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
     * 路由字段
     */
    private String routerKey;

    /**
     * 默认数据源
     */
    private String defaultDb;

    /**
     * 默认数据源配置
     */
    private Map<String, Object> defaultDataSourceConfig;

    /**
     * 将DB的信息注入到spring中，供后续获取
     *
     * @return 数据源配置
     */

    @Bean
    public DBRouterConfig dbRouterConfig() {
        return new DBRouterConfig(dbCount, tbCount, routerKey);
    }

    @Bean
    public DataSource dataSource() {
        Map<Object, Object> targetDataSources = new HashMap<>();

        // 分库分表数据源配置
        for (String dbInfo : dataSourceMap.keySet()) {
            DataSource dataSource = poolSourceInit(dbInfo, dataSourceMap.get(dbInfo));
            targetDataSources.put(dbInfo, dataSource);
        }

        // 设置分库分表数据源
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setTargetDataSources(targetDataSources);
        // 设置默认数据源
        DataSource defaultDataSource = poolSourceInit(defaultDb, defaultDataSourceConfig);
        dynamicDataSource.setDefaultTargetDataSource(defaultDataSource);
        return dynamicDataSource;
    }

    private DataSource poolSourceInit(String dbInfo, Map<String, Object> objMap) {
        String poolType = null == objMap.get("pool-type") ? PoolType.DEFAULT.getValue() : objMap.get("pool-type").toString();
        PoolType poolTypeEnum = PoolType.getPoolType(poolType);
        assert poolTypeEnum != null;
        logger.info("数据源配置：{}，连接池类型：{}", dbInfo, poolTypeEnum.getValue());

        ConnectPool connectPool = ConnectPoolFactory.getConnectPool(poolType);
        DataSource dataSource = (DataSource) connectPool.init(objMap);
        logger.info("数据源：{}，初始化成功", dbInfo);
        return dataSource;
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
        routerKey = environment.getProperty(prefix + "routerKey");

        String dataSources = environment.getProperty(prefix + "list");

        assert dataSources != null;
        // 分库分表数据源配置
        for (String dbInfo : dataSources.split(",")) {
            Map<String, Object> dataSourceProps = PropertyUtil.handle(environment, prefix + dbInfo, Map.class);
            dataSourceMap.put(dbInfo, dataSourceProps);
        }

        // 默认数据源配置
        // 默认数据源
        defaultDb = environment.getProperty(prefix + "default");
        defaultDataSourceConfig = PropertyUtil.handle(environment, prefix + defaultDb, Map.class);
    }

}
