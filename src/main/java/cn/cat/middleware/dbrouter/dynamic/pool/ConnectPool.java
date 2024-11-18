package cn.cat.middleware.dbrouter.dynamic.pool;

import java.util.Map;

public interface ConnectPool {
    Object init(Map<String, Object> connectionProperties);

    void close();
}
