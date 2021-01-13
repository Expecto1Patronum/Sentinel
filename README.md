<img src="https://user-images.githubusercontent.com/9434884/43697219-3cb4ef3a-9975-11e8-9a9c-73f4f537442d.png" alt="Sentinel Logo" width="50%">

### 规则持久化的方式

此处使用的是推模式，使用到了一个数据源（此处使用的是Nacos配置中心数据源）

其工作原理图：

![Sentinel规则持久化推模式](https://user-images.githubusercontent.com/9434884/53381986-a0b73f00-39ad-11e9-90cf-b49158ae4b6f.png)

用户在Dashboard上进行流控规则修改后，Dashboard会将修改后的数据推送到Nacos配置中心中，Nacos配置中心会将修改的流控规则推送给用户

![sentinel流控规则在nacos的配置](./images/image-20210113152122689.png)

当前支持的持久化配置：

- 流控规则
- 降级规则
- 热点规则
- 系统规则
- 授权规则

使用方式

```shell
cd sentinel-dashboard
mvn clean package -Dmaven.test.skip=true
java -jar sentinel-dashboard.jar
```

### 客户端使用的方式

1.导入依赖

```xml
<!-- sentinel 整合成功会暴露/actuator/sentinel端点 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
<!-- 整合sentinel持久化到nacos -->
<dependency>
<groupId>com.alibaba.csp</groupId>
<artifactId>sentinel-datasource-nacos</artifactId>
</dependency>
```

2.修改配置

```yaml
spring:
  cloud:
    sentinel:
      transport:
        # 指定sentinel控制台的地址
        dashboard: localhost:8080
      datasource:
        flow:
          nacos:
            server-addr: localhost:8848
            dataId: ${spring.application.name}-flow-rules
            groupId: SENTINEL_GROUP
            # 规则类型，取值见：
            # org.springframework.cloud.alibaba.sentinel.datasource.RuleType
            rule-type: flow
        degrade:
          nacos:
            server-addr: localhost:8848
            dataId: ${spring.application.name}-degrade-rules
            groupId: SENTINEL_GROUP
            rule-type: degrade
        system:
          nacos:
            server-addr: localhost:8848
            dataId: ${spring.application.name}-system-rules
            groupId: SENTINEL_GROUP
            rule-type: system
        authority:
          nacos:
            server-addr: localhost:8848
            dataId: ${spring.application.name}-authority-rules
            groupId: SENTINEL_GROUP
            rule-type: authority
        param-flow:
          nacos:
            server-addr: localhost:8848
            dataId: ${spring.application.name}-param-flow-rules
            groupId: SENTINEL_GROUP
            rule-type: param-flow
      eager: true
```

### 监控数据持久化

目前持久化到MySQL数据库中，默认查询半个小时内的监控数据

// TODO 1.页面上添加时间范围筛选框 2.使用更加效率的存储组件而非MySQL 3.是否需要定时清除历史的监控数据

使用方式：（目前代码中写死了测试环境，后续可以根据打包时指定环境来读取对应的配置）

- 在配置中心中新建一个配置 sentinel-dashboard-dev

  Data ID:sentinel-dashboard-dev

  Group: SENTINEL_GROUP

  配置内容:{"url":"jdbc:mysql://数据库地址:数据库端口/sentinel_dashboard?useUnicode=true&characterEncoding=utf-8&useSSL=FALSE","username":"数据库用户名","password":"数据库用户密码"}

- 启动项目

