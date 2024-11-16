package cn.cat.middleware.dbrouter.util;

import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PropertyUtil {
    private static int springBootVersion = 1;

    static {
        try {
            Class.forName("org.springframework.boot.bind.RelaxedPropertyResolver");
        } catch (ClassNotFoundException e) {
            springBootVersion = 2;
        }
    }


    @SuppressWarnings("unchecked")
    public static <T> T handle(final Environment environment, final String prefix, final Class<T> targetClass) {
        switch (springBootVersion) {
            case 1:
                return (T) v1(environment, prefix);
            case 2:
            case 3:
                return (T) v2And3(environment, prefix, targetClass);
            default:
                throw new RuntimeException("Unsupported Spring Boot version: " + springBootVersion);
        }
    }

    // 该方法用于获取指定前缀的子属性，使用Spring的RelaxedPropertyResolver类
    private static Object v1(final Environment environment, final String prefix) {
        try {
            // 获取构造函数
            Class<?> resolverClass = Class.forName("org.springframework.boot.bind.RelaxedPropertyResolver");
            Constructor<?> resolverConstructor = resolverClass.getDeclaredConstructor(PropertyResolver.class);
            // 获取方法、创建实例
            Method getSubPropertiesMethod = resolverClass.getDeclaredMethod("getSubProperties", String.class);
            Object resolverObject = resolverConstructor.newInstance(environment);
            // 处理前缀并调用方法返回结果
            String prefixParam = prefix.endsWith(".") ? prefix : prefix + ".";
            return getSubPropertiesMethod.invoke(resolverObject, prefixParam);
        } catch (final ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
                       | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private static Object v2And3(final Environment environment, final String prefix, final Class<?> targetClass) {
        try {
            // 获取类、方法、创建实例
            Class<?> binderClass = Class.forName("org.springframework.boot.context.properties.bind.Binder");
            Method getMethod = binderClass.getDeclaredMethod("get", Environment.class);
            Method bindMethod = binderClass.getDeclaredMethod("bind", String.class, Class.class);
            Object binderObject = getMethod.invoke(null, environment);
            // 处理前缀并调用方法返回结果
            String prefixParam = prefix.endsWith(".") ? prefix.substring(0, prefix.length() - 1) : prefix;
            Object bindResultObject = bindMethod.invoke(binderObject, prefixParam, targetClass);
            Method resultGetMethod = bindResultObject.getClass().getDeclaredMethod("get");
            return resultGetMethod.invoke(bindResultObject);
        } catch (final ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
                       | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

}
