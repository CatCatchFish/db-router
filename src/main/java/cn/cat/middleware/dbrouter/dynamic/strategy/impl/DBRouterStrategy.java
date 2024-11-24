package cn.cat.middleware.dbrouter.dynamic.strategy.impl;

import cn.cat.middleware.dbrouter.DBContextHolder;
import cn.cat.middleware.dbrouter.DBRouterConfig;
import cn.cat.middleware.dbrouter.dynamic.strategy.IDBRouterStrategy;

public class DBRouterStrategy implements IDBRouterStrategy {
    private final DBRouterConfig dbRouterConfig;

    public DBRouterStrategy(DBRouterConfig dbRouterConfig) {
        this.dbRouterConfig = dbRouterConfig;
    }

    @Override
    public void doRouter(String dbKeyAttr) {
        // 计算路由
        int size = dbRouterConfig.getDbCount() * dbRouterConfig.getTbCount();
        // 路由算法
        int idx = (size - 1) & (dbKeyAttr.hashCode() ^ (dbKeyAttr.hashCode() >>> 16));
        // 表库索引
        int dbIdx = idx / dbRouterConfig.getTbCount() + 1;
        int tbIdx = idx - dbRouterConfig.getTbCount() * (dbIdx - 1);
        // 设置路由信息
        DBContextHolder.setDBKey(String.format("%02d", dbIdx));
        DBContextHolder.setTBKey(String.format("%02d", tbIdx));
    }

    @Override
    public void setDBKey(int dbIdx) {
        DBContextHolder.setDBKey(String.format("%02d", dbIdx));
    }

    @Override
    public void setTBKey(int tbIdx) {
        DBContextHolder.setTBKey(String.format("%02d", tbIdx));
    }

    @Override
    public int dbCount() {
        return dbRouterConfig.getDbCount();
    }

    @Override
    public int tbCount() {
        return dbRouterConfig.getTbCount();
    }

    @Override
    public void clear() {
        DBContextHolder.clearDBKey();
        DBContextHolder.clearTBKey();
    }
}

