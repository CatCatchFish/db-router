## 数据库路由组件

#### 如何使用？

本组件提供了近乎平常开发的体验，简单易上手。

1、在`application.yaml`中配置如下（支持数据连接池配置，`hikari`、`c3p0`、`druid`）

```yaml
# 路由配置
mini-db-router:
  jdbc:
    datasource:
      # 分库分表数，二者之和需为2的n次方（散列均匀）
      dbCount: 2
      tbCount: 4
      # db_0d 格式
      list: db01,db02
      # 默认数据库
      default: db00
      routerKey: userId
      db00:
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://127.0.0.1:3306/bugstack?useUnicode=true
        username: root
        password: 123456
        # 需要指明连接池类型
        pool-type: hikari
        pool:
          minimum-idle: 15 #最小空闲连接数量
          idle-timeout: 180000 #空闲连接存活最大时间，默认600000（10分钟）
          maximum-pool-size: 25 #连接池最大连接数，默认是10
          auto-commit: true  #此属性控制从池返回的连接的默认自动提交行为,默认值：true
          max-lifetime: 1800000 #此属性控制池中连接的最长生命周期，值0表示无限生命周期，默认1800000即30分钟
          connection-timeout: 30000 #数据库连接超时时间,默认30秒，即30000
          connection-test-query: SELECT 1
      db01:
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://127.0.0.1:3306/bugstack_01?useUnicode=true
        username: root
        password: 123456
        pool-type: hikari
        pool:
          minimum-idle: 15 #最小空闲连接数量
          idle-timeout: 180000 #空闲连接存活最大时间，默认600000（10分钟）
          maximum-pool-size: 25 #连接池最大连接数，默认是10
          auto-commit: true  #此属性控制从池返回的连接的默认自动提交行为,默认值：true
          max-lifetime: 1800000 #此属性控制池中连接的最长生命周期，值0表示无限生命周期，默认1800000即30分钟
          connection-timeout: 30000 #数据库连接超时时间,默认30秒，即30000
          connection-test-query: SELECT 1
      db02:
        # 支持不配做数据库
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://127.0.0.1:3306/bugstack_02?useUnicode=true
        username: root
        password: 123456
```

2、编写`xml`，针对分表的`xml`语句，仅需编写表名即可，无需添加"`_0d`"

用"`user_01`"表举例：

```xml
<select id="queryUserInfoByUserId" parameterType="cn.cat.spring.dbrouter.test.infrastructure.po.User"
            resultType="cn.cat.spring.dbrouter.test.infrastructure.po.User">
    SELECT id, userId, userNickName, userHead, userPassword, createTime
    FROM user
    where userId = #{userId}
</select>
```



#### 如何支持分库分表策略？

| 策略     | `@DBRouterSplit` | `@DBRouter` |
| -------- | ---------------- | ----------- |
| 仅分库   | `false`          | `true`      |
| 仅分表   | `true`           | `false`     |
| 分库分表 | `true`           | `true`      |



#### 执行`SQL`语句流程

![流程梳理](img/流程梳理.png)



### 如何解决同一个事务中切换数据导致事务失效？

原因：`AbstractRoutingDataSource`是基于`ThreadLocal`方式实现动态切换数据源的，这种切换会影响当前线程的数据库连接，相当于断开数据库连接，再去连接另一个数据库，而`MyBatis`的默认事务管理方式是基于`JDBC`的`Connection`的，所以同一事务下切换数据源会导致事务失效。

如何解决：使用`Spring`的编程式事务（`TransactionTemplate`）

步骤：

1. 去掉`DAO`方法层的`@DBRouter`注解；
2. 使用`DBRouterStrategy`计算出路由
3. 使用`TransactionTemplate`管理本次事务

示例：

```java
@Test
public void test_queryUserInfoByUserId_default() {
    User user = new User("cat");
    // 分表路由
    userDao.insertUser(user);
    // 默认路由
    User userQuery = userDefaultDao.queryUserInfoByUserId(new User("admin"));
    logger.info("测试结果：{}", JSON.toJSONString(userQuery));
}
```

修改：

```java
@Test
public void test_queryUserInfoByUserId_default() {
    User user = new User("cat");
    try {
        dbRouterStrategy.doRouter(user.getUserId());
        transactionTemplate.execute(status -> {
            try {
                // 分表路由
                userDao.insertUser(user);
                // 默认路由
                User userQuery = userDefaultDao.queryUserInfoByUserId(new User("admin"));
                logger.info("测试结果：{}", JSON.toJSONString(userQuery));
                return 1;
            } catch (DuplicateKeyException e) {
                status.setRollbackOnly();
                logger.error("插入重复数据", e);
                throw e;
            }
        });
    } finally {
        dbRouterStrategy.clear();
    }
}
```

**最后要调用`clear()`方法是因为使用`ThreadLocal`可能会导致内存泄漏**