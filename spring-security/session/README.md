# Spring Security & session

## 1. spring security中的session管理
在继承```WebSecurityConfigurerAdapter```并重写```configure```中，```HttpSecurity```的配置
，后面以```http.```均为其中配置，不再赘述
```java
http
    .sessionManagement()
    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
```

### 1.1 ```sessionCreationPolicy``` session的创建策略

- ```always```: 如果当前请求没有```session```存在，Spring Security创建一个session
- ```never```: Spring Security将永远不会主动创建```session```，但是如果```session```已经存在，它将使用该```session```
- ```ifRequired```(默认): Spring Security 在需要时才创建```session```
- ```stateless```: Spring Security不会创建或使用任何```session```。适合于接口型的无状态应用，改方式节省资源。

### 1.2 会话超时配置

#### 1.2.1 在```.yml```或```.properties```中配置超时时间
- springboot 提供的超时时间配置
```yaml
   server.servlet.session.timeout=15m 
```
- 集成了spring session的配置
```yaml
    spring.session.timeout=15m
```

配置时间最小是一分钟，若不足，则按照一分钟

#### 1.2.2 在```HttpSecurity```中配置，超时之后的处理

```java
        http
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .invalidSessionUrl("/login.html"); // 非法session
```
超时之后再访问其他界面，将返回登录界面

### 1.3 session保护

```java
        http
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .invalidSessionUrl("/login.html") // 非法session
                .sessionFixation().migrateSession();
```

- 默认情况下，Spring Security启用了migrationSession保护方式。即对于同一
个cookies的SESSIONID用户，每次登录验证将创建一个新的HTTP会话，旧的HTTP会话
将无效，并且旧会话的属性将被复制。
- 设置为```none```时，原始会话不会无效
- 设置```newSession```后，将创建一个干净的会话，而不会复制旧会话中的任何属性
- 设置```changeSessionId```后，只会更换一下JSESSIONID

### 1.4 Cookie的安全

实际上保证Cookie的安全，也是保护session的安全
- httpOnly：如果为true，则浏览器脚本将无法访问cookie
- secure： 如果为true，则仅通过HTTPS连接发送cookie，HTTP无法携带cookie

```yaml
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true
```

## 2. 限制最大登录用户数量

```java
        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .invalidSessionUrl("/login.html") // 非法session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .expiredSessionStrategy(customExpiredSessionStrategy);
```
- ```maximumSessions``` 限制同一用户最大登录数量
- ```maxSessionsPreventsLogin``` 登录数量上限之后，是否允许后面的登录可登录，```true```为阻止，反之允许
- ```expiredSessionStrategy``` 自定义失效session后的处理
