package cn.cat.middleware.dbrouter;

import cn.cat.middleware.dbrouter.annotation.DBRouter;
import cn.cat.middleware.dbrouter.dynamic.strategy.IDBRouterStrategy;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @author Cat
 * @description : 数据路由切面，通过自定义注解的方式，拦截被切面的方法，进行数据库路由
 */
@Aspect
public class DBRouterJoinPoint {
    private static final Logger logger = LoggerFactory.getLogger(DBRouterJoinPoint.class);
    private final DBRouterConfig dbRouterConfig;
    private final IDBRouterStrategy dbRouterStrategy;

    public DBRouterJoinPoint(DBRouterConfig dbRouterConfig, IDBRouterStrategy dbRouterStrategy) {
        this.dbRouterConfig = dbRouterConfig;
        this.dbRouterStrategy = dbRouterStrategy;
    }


    @Pointcut("@annotation(cn.cat.middleware.dbrouter.annotation.DBRouter)")
    public void aopPoint() {
    }

    @Around("aopPoint() && @annotation(dbRouter)")
    public Object doRouter(ProceedingJoinPoint jp, DBRouter dbRouter) throws Throwable {
        // 获取路由key
        String dbKey = dbRouter.key();
        if (StringUtils.isBlank(dbKey) && StringUtils.isBlank(dbRouterConfig.getRouterKey()))
            throw new RuntimeException("annotation DBRouter key is null！");

        dbKey = StringUtils.isBlank(dbKey) ? dbRouterConfig.getRouterKey() : dbKey;
        // 路由属性
        String dbKeyAttr = getAttrValue(dbKey, jp.getArgs());
        dbRouterStrategy.doRouter(dbKeyAttr);
        // 执行切点方法
        logger.info("数据库路由 method：{} dbIdx：{} tbIdx：{}", getMethod(jp).getName(), DBContextHolder.getDBKey(), DBContextHolder.getTBKey());
        try {
            return jp.proceed();
        } finally {
            dbRouterStrategy.clear();
        }
    }

    private Method getMethod(JoinPoint jp) throws NoSuchMethodException {
        Signature sig = jp.getSignature();
        MethodSignature methodSignature = (MethodSignature) sig;
        return jp.getTarget().getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
    }

    public String getAttrValue(String attr, Object[] args) {
        if (1 == args.length) {
            Object arg = args[0];
            if (arg instanceof String) {
                return arg.toString();
            }
        }

        String filedValue = null;
        for (Object arg : args) {
            try {
                if (StringUtils.isNotBlank(filedValue)) {
                    break;
                }
                filedValue = BeanUtils.getProperty(arg, attr);
            } catch (Exception e) {
                logger.error("获取路由属性值失败 attr：{}", attr, e);
            }
        }
        return filedValue;
    }

}
