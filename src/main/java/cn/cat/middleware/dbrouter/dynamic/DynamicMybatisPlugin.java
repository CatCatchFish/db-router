package cn.cat.middleware.dbrouter.dynamic;

import cn.cat.middleware.dbrouter.DBContextHolder;
import cn.cat.middleware.dbrouter.annotation.DBRouterSplit;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL 路由拦截器 修改分表信息
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class DynamicMybatisPlugin implements Interceptor {
    private final Pattern pattern = Pattern.compile("(from|into|update)[\\s]{1,}(\\w{1,})", Pattern.CASE_INSENSITIVE);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 获取StatementHandler对象
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = MetaObject.forObject(statementHandler,
                SystemMetaObject.DEFAULT_OBJECT_FACTORY,
                SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY,
                new DefaultReflectorFactory());
        // MappedStatement 包含了SQL语句的相关信息
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");

        // 获取自定义注解判断是否进行分表操作
        String id = mappedStatement.getId();
        String className = id.substring(0, id.lastIndexOf("."));
        Class<?> clazz = Class.forName(className);
        DBRouterSplit dbRouterSplit = clazz.getAnnotation(DBRouterSplit.class);
        if (null == dbRouterSplit || !dbRouterSplit.splitTable()) return invocation.proceed();

        // 获取Sql
        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = boundSql.getSql();

        // eg: 替换表名 user -> user_01
        Matcher matcher = pattern.matcher(sql);
        String tableName = null;
        if (matcher.find()) {
            tableName = matcher.group().trim();
        }
        assert tableName != null;

        // 获取线程变量中存储的分表信息 并替换SQL
        String replaceSql = matcher.replaceAll(tableName + "_" + DBContextHolder.getTBKey());

        // 通过反射修改Sql语句
        Field field = boundSql.getClass().getDeclaredField("sql");
        field.setAccessible(true);
        field.set(boundSql, replaceSql);
        field.setAccessible(false);

        return invocation.proceed();
    }
}
