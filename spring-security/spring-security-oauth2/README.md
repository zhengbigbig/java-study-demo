# Spring Security Oauth2

## 1. 实现授权码模式认证服务器

- 通过上文spring social，主要实现的是客户端
- 除了客户端，还有资源服务器、授权服务器
- 资源服务器和授权服务器可以是在一个应用上，也可以是在不同服务器上

### 1.1 引入依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.security.oauth</groupId>
    <artifactId>spring-security-oauth2</artifactId>
    <version>2.3.6.RELEASE</version>
</dependency>
```

### 1.2 认证服务器

