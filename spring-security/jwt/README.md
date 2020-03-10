# Spring Security JWT

## 1. JWT介绍

### 1.1 基于Session的应用存在的问题（不适用的场景）

cookies和sessionid是服务端和浏览器端自行维护，编码层面开发者只能感知session的存取

- 无浏览器的应用
- 集群应用

当然集群应用也可以通过，共享session集群来实现，譬如redis共享session等

### 1.2 JWT（JSON web tokens）认证

- 客户端登录服务端申请JWT令牌，服务端生成令牌并返回给客户端
- 客户端再次访问后续需要鉴权接口时，请求时在请求头中携带JWT
- 服务端校验客户端请求的JWT，通过则授权访问，响应资源请求结果

### 1.3 JWT结构

- Header，通常用于说明JWT使用的算法信息
- payload，通常用户携带自定义的信息，一般采用对等加密解密，可以进行明文解码
- signature，对前两部分数据的签名，防止被篡改。需要制定secret，进行签名和解签

## 2. JWT在Spring Security中的认证流程

1. 定义JWT工具类，用于令牌的生成，校验，刷新等
2. 定义接口，```/authentication```用于登录验证，通过后生成JWT返回客户端，
```refreshtoken```用于刷新JWT，更新JWT有效期，避免用户使用时过期，避免不好的体验
3. 定义服务类```JwtAuthService```，登录、刷新相关的实现
4. 定义Token的过滤器：```JwtAuthenticationTokenFilter```，对需要鉴权的url进行鉴权

## 3. 在上一节代码基础上进行实现

1. JWT完全使用JSON接口，没有使用表单提交，去除```formLogin```登录模式
2. JWT用于开发前后端分离的无状态应用，所以项目中去掉与session相关的内容，去掉页面视图相关的内容
3. 引入JWT工具包并定义工具类```JwtTokenUtil```

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.0</version>
</dependency>
```

4. 实现```JwtAuthService```，核心业务逻辑

5. 实现
