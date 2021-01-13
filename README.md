<img src="https://user-images.githubusercontent.com/9434884/43697219-3cb4ef3a-9975-11e8-9a9c-73f4f537442d.png" alt="Sentinel Logo" width="50%">

### 规则持久化的方式

此处使用的是推模式，使用到了一个数据源（此处使用的是Nacos配置中心数据源）

其工作原理图：

![Sentinel规则持久化推模式](https://user-images.githubusercontent.com/9434884/53381986-a0b73f00-39ad-11e9-90cf-b49158ae4b6f.png)

用户在Dashboard上进行流控规则修改后，Dashboard会将修改后的数据推送到Nacos配置中心中，Nacos配置中心会将修改的流控规则推送给用户

![sentinel流控规则在nacos的配置](./images/image-20210113152122689.png)

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

