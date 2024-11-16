package cn.cat.middleware.dbrouter.dynamic;

import cn.cat.middleware.dbrouter.DBContextHolder;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @author Cat
 * @description 动态数据源, 获取线程绑定的数据源
 */
public class DynamicDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return "db" + DBContextHolder.getDBKey();
    }
}
