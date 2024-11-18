package cn.cat.middleware.dbrouter.dynamic.pool.factory;

import cn.cat.middleware.dbrouter.dynamic.pool.ConnectPool;
import cn.cat.middleware.dbrouter.dynamic.pool.impl.C3p0ConnectPool;
import cn.cat.middleware.dbrouter.dynamic.pool.impl.DefaultConnectPool;
import cn.cat.middleware.dbrouter.dynamic.pool.impl.DruidConnectPool;
import cn.cat.middleware.dbrouter.dynamic.pool.impl.HikariConnectPool;
import cn.cat.middleware.dbrouter.type.PoolType;

import java.util.HashMap;
import java.util.Map;

public class ConnectPoolFactory {
    private static final Map<String, ConnectPool> connectPoolMap = new HashMap<>();

    static {
        connectPoolMap.put(PoolType.C3P0.getValue(), new C3p0ConnectPool());
        connectPoolMap.put(PoolType.HIKARI.getValue(), new HikariConnectPool());
        connectPoolMap.put(PoolType.DRUID.getValue(), new DruidConnectPool());
        connectPoolMap.put(PoolType.DEFAULT.getValue(), new DefaultConnectPool());
    }

    public static ConnectPool getConnectPool(String poolType) {
        return connectPoolMap.get(poolType);
    }

    public static void clear() {
        connectPoolMap.forEach((k, v) -> v.close());
    }
}
