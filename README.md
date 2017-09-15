# Access Limit

![Version](https://img.shields.io/badge/Version-1.0.0-blue.svg)

![jdk    ](https://img.shields.io/badge/Jdk-1.7+-blue.svg)

![Spring ](https://img.shields.io/badge/Spring-4.2.6.RELEASE-blue.svg)

基于访问次数的访问控制，支持多维度自定义控制。

maven:
```xml
<dependency>
    <groupId>com.github.blackshadowwalker.spring</groupId>
    <artifactId>access-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

spring:
```xml
<aop:aspectj-autoproxy proxy-target-class="true"/>

<context:component-scan base-package="com.blackshadowwalker.spring.access"/>
```

## 1. 使用方法1

基于入参控制访问频率。

```java
@AccessLimits({
        @AccessLimit(value = "CONCAT:A_10M", key = "#p0", limit = 2, unitTime = 600, errorMsg = "请求频繁，请10分钟后重试"),
        @AccessLimit(value = "CONCAT:A_1H", key = "#p0", limit = 5, unitTime = 3600, errorMsg = "请求频繁，请1小时后重试"),
        @AccessLimit(value = "CONCAT:AB_24H", key = "#p0 + '_' + #p1", limit = 10, unitTime = 3600 * 24, errorMsg = "请求频繁，请稍后重试[403]"),
        @AccessLimit(value = "CONCAT:AB_24H", key = "#p0 + '_' + #p1", limit = 10, unitTime = 3600 * 24,
        errorMsg = "请求频繁，请稍后重试[403]"),
})
public String myControllerMethod(String a, Integer b) {
    log.info("call myConcat, a:{} b:{}", a, b);
    return a + "_" + b;
}
```

## 2. 使用方法2

当访问达到频率限制(命中访问限制)后，如果用户继续请求，则延迟`命中访问限制结果`key的存活期。

如访问命中访问限制后，如果再次请求，每请求一次，则延期`60s`

```java
@AccessLimit(value = "CONCAT:A_10M", key = "#p0", limit = 2, unitTime = 600, errorMsg = "请求频繁，请10分钟后重试",
        delayExpireOnHit = true, delayExpireStep = 60
)
```


