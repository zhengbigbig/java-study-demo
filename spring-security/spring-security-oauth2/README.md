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

### 1.2 认证服务器（授权码模式）

```java

@Configuration
public class SecurityConfig  extends WebSecurityConfigurerAdapter {

    @Resource
    MyUserDetailsService myUserDetailsService;

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(myUserDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}

@Configuration
@EnableAuthorizationServer
public class OAuth2AuthorizationServer extends AuthorizationServerConfigurerAdapter {

    @Resource
    PasswordEncoder passwordEncoder;

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("client1").secret(passwordEncoder.encode("123456")) // Client 账号、密码。
                .redirectUris("http://localhost:8888/callback") // 配置回调地址，选填。
                .authorizedGrantTypes("authorization_code") // 授权码模式
                .scopes("all"); // 可授权的 Scope
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        oauthServer
                .tokenKeyAccess("permitAll()")
                .checkTokenAccess("permitAll()")
                .allowFormAuthenticationForClients();
    }

}
```

### 1.3 测试

#### 1.3.1 获取授权码(授权码模式)

```shell script
http://localhost:8001/oauth/authorize?client_id=client1&redirect_uri=http://localhost:8888/callback&response_type=code&scope=all
```

- /oauth/authorize为获取授权码的地址，由Spring Security OAuth项目提供
- client_id即我们认证服务器中配置的client
- redirect_uri即回调地址，授权码的发送地址该地址为第三方客户端应用的地址
- response_type=code表示希望获取的响应内容为授权码
- scope表示申请的权限范围

#### 1.3.2 根据授权码换取AccessToken(授权码模式)

两种方式测试：
1. CURL
```shell script
curl -X POST --user client1:123456 http://localhost:8001/oauth/token  -H "content-type: application/x-www-form-urlencoded" -d "code=23462r&grant_type=authorization_code&redirect_uri=http://localhost:8888/callback&scope=all"
```
2. 通过PostMan发送请求
- content-type: application/x-www-form-urlencoded
- 将上面参数全部带上

3. 换取access_token后实现其他业务逻辑

## 2. 其它三种模式认证服务器

### 2.1 密码模式

在```authorizedGrantTypes```增加password
```java
@Configuration
@EnableAuthorizationServer // 开启认证服务器功能
public class OAuth2AuthorizationServer extends AuthorizationServerConfigurerAdapter {
    
    // ...

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("client1").secret(passwordEncoder.encode("123456")) // Client 账号、密码。
                .redirectUris("http://localhost:8888/callback") // 配置回调地址，选填。
                .authorizedGrantTypes("authorization_code","password") // 授权码模式
                .scopes("all"); // 可授权的 Scope
    }
    
    // ...
}
```

- 支持password授权模式的话，还需要额外支持用户登录认证。
- 这个模式需要额外处理，首先经过filter认证通过，然后进入```TokenEndpoint```，会根据不同认证方式获取token，其中password比较特别，需要走用户账号密码认证
- 如果是密码模式，将匹配到```ResourceOwnerPasswordTokenGranter```，必须提供用户密码的认证
需要在```OAuth2AuthorizationServer```配置

```java
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authenticationManager);
    }
```

如果是password模式，要走用户账号密码认证，就需要配置如上

为什么要这么配置？？？

- allowFormAuthenticationForClients，将优先走过滤器```ClientCredentialsTokenEndpointFilter```，因而是支持client_id和client_secret作为用户密码登录
- 这样就不支持普通认证了，但是用户名密码登录需要认证，必须如上配置。
- 之前在```ResourceOwnerPasswordTokenGranter```这里指定了userDetailsService为clientDetailsUserDetailsService


测试：

- 通过账号密码直接获取token

```shell script
curl -X POST --user client1:123456 http://localhost:8001/oauth/token -H "accept:application/json" -H "content-type:application/x-www-form-urlencoded" -d "grant_type=password&username=admin&password=123456&scope=all"
```
返回
```shell script
{"access_token":"06726f2d-f71e-4603-8ec2-e5ce429be2f4","token_type":"bearer","expires_in":43199,"scope":"all"}
```

### 2.2 简化模式

- 简化模式是授权码模式的“简化”，所以只需要在以上配置的基础上，为authorizedGrantTypes加上implicit配置即可。
- 与授权码模式相比，少了获取code那步

测试：
```shell script
http://localhost:8001/oauth/authorize?client_id=client1&redirect_uri=http://localhost:8888/callback&response_type=token
```
返回结果
```shell script
http://localhost:8888/callback#access_token=2302706d-1279-42bb-a347-70bd2dd0eae3&token_type=bearer&expires_in=43199&scope=all
```

### 2.3 客户端模式

- 客户端模式实际上是密码模式的简化，无需配置或使用资源拥有者账号。因为它没有用户的概念，直接与授权服务器交互，通过 Client 的编号(client_id)和密码(client_secret)来保证安全性。
- 配置方式为authorizedGrantTypes加上client_credentials配置即可。

测试
```shell script
curl -X POST "http://localhost:8001/oauth/token"  --user client1:123456  -d "grant_type=client_credentials&scope=all"
```

返回：
```shell script
{"access_token":"2e25e838-f47b-44b3-847e-d96ed01a81af","token_type":"bearer","expires_in":43199,"scope":"all"}%
```

## 3. AccessToken令牌的刷新

- AccessToken是有有效期的
- 防止令牌过期，造成用户频繁登录，体验不佳，因此Spring Security OAuth也提供了刷新AccessToken的方法

### 3.1 配置令牌刷新

- 配置方式为authorizedGrantTypes加上```refresh_token```配置
- 为OAuth2AuthorizationServer配置类加入UserDetailsService,刷新令牌的时候需要用户信息

```java
@Override
public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
    endpoints.authenticationManager(authenticationManager)
             .userDetailsService(myUserDetailsService);
}
```
### 3.2 配置令牌有效时间

```java
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("client1").secret(passwordEncoder.encode("123456")) // Client 账号、密码。
                .redirectUris("http://localhost:8888/callback") // 配置回调地址，选填。
                .authorizedGrantTypes("authorization_code", "password", "implicit", "client_credentials", "refresh_token") // 授权码模式
                .scopes("all") // 可授权的 Scope
                .accessTokenValiditySeconds(7 * 24 * 60 * 60)
                .refreshTokenValiditySeconds(30 * 24 * 60 * 60);
    }
```

### 3.3 测试

使用上面授权码模式和客户端模式获取将会多出一条refresh_token，如下
```shell script
{
    "access_token": "63de6c71-672f-418c-80eb-0c9abc95b67c",
    "token_type": "bearer",
    "refresh_token": "8495d597-0560-4598-95ef-143c0855363c",
    "expires_in": 43199,
    "scope": "select"
}

```

使用refresh_token刷新access_token
```shell script
http://localhost:8001/oauth/token?grant_type=refresh_token&refresh_token=8495d597-0560-4598-95ef-143c0855363c&client_id=client1&client_secret=123456

```

## 4. 实现资源服务器

- 授权服务器和资源服务器在一块，Token信息存在内存中，二者都可以使用，认证服务器发放AccessToken，并将其保存在内存里面；资源服务器可以获取内存中的AccessToken，进行资源的访问鉴权。
- 若俩则分开，则需要通过其他的手段使得共享Token

### 4.1 配置资源服务器

```java
@Configuration
@EnableResourceServer // 针对OAuth2的ResourceServer资源服务器的配置
public class OAuth2ResourceServer extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
             .anyRequest().authenticated()
             .and()
             .requestMatchers()
             .antMatchers("/api/**"); // 对任何“/api/**”接口的访问，都必须经过OAuth2认证服务器认证
    }

}
```

### 4.2 授权后访问资源

#### 使用AccessToken访问资源

```shell script
curl -X GET http://localhost:8001/api/hello -H "authorization: Bearer f8ca9609-2c86-476e-a336-1c69d4b3fd2a"
```

- 通过HTTP协议的请求头携带AccessToken
- HTTP请求头的Key是authorization，固定写法。
- AccessToken默认的类型是Bearer ，可自定义。

返回：
- 正确：Hello Oauth2 Resource Server
- 错误： {"error":"invalid_token","error_description":"Invalid access token: 305f9638-95b0-404f-83d3-b88800e"}

### 4.3 分离认证服务器和资源服务器

- 分离之后Token不再共享，因此需要资源服务器向认证服务器认证，两种方案

1. 资源服务器在每一次接收到资源请求的时候，都向认证服务器发送一个请求，由认证服务器验证AccessToken的正确性，并返回验证结果。
2. 资源服务器在每一次接收到资源请求的时候，都从数据库里面去查询AccessToken，并自己验证正确性。前提是：认证服务器已经在登录认证的成功之后，将AccessToken保存到数据库里面。

#### 4.3.1 RemoteTokenServices（对应第一种方式）

- 在每一次客户端向资源服务器请求资源的时候，资源服务器都会向认证服务器发送一个HTTP请求到“/oauth/check_token”，用来判断客户端提交的AccessToken的合法性
- 这种实现方式的缺点显而易见：增加了网络资源的消耗，增加了接口资源的访问时长

1. 在```OAuth2ResourceServer```中配置：

```java
@Primary
@Bean
public RemoteTokenServices tokenServices() {
    final RemoteTokenServices tokenService = new RemoteTokenServices();
    tokenService.setCheckTokenEndpointUrl("http://localhost:8001/oauth/check_token");
    tokenService.setClientId("client1");
    tokenService.setClientSecret("123456");
    return tokenService;
}

@Override
public void configure(ResourceServerSecurityConfigurer resources) {
    resources.tokenServices(tokenServices());
}
```

2. 修改独立的认证服务器OAuth2AuthorizationServer配置代码，将checkTokenAccess的权限设置为isAuthenticated，认证通过才可以访问。
```java
@Override
public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
    oauthServer.tokenKeyAccess("permitAll()")
            .checkTokenAccess("isAuthenticated()")
            .allowFormAuthenticationForClients();
}
```

#### 4.3.2 TokenStore

- 与RemoteTokenServices相对应的是DefaultTokenServices ，如果不做配置DefaultTokenServices 是默认的TokenServices。
- 其中最关键的信息就是TokenStore，TokenStore决定了Token该如何集中存储。
- 可以通过如下方式去修改它的默认行为，DefaultTokenServices 还有很多参数可以设置
```java
    DefaultTokenServices tokenServices = new DefaultTokenServices();
    tokenServices.setTokenStore("关键参数");
```
Spring Security OAuth2集中存储token值的方式，即TokenStore：

- InMemoryTokenStore:token存储内存之中（默认，不适合认证资源服务分离部署）
- JdbcTokenStore:token存储在关系型数据库之中
- JwtTokenStore:token不会存储到任何介质中，使用JWT令牌作为AccessToken，在请求发起者和服务提供者之间网络传输
- RedisTokenStore:token存储在Redis数据库之中

下面来简要说说主要的：

1. JdbcTokenStore

- 在application全局配置中，已经配置了spring.datasource相关的关系型数据库配置，如mysql
- 数据库里面需要新建以下的两张表，用于集中保存token信息

```sql
DROP TABLE IF EXISTS `oauth_access_token`;
CREATE TABLE `oauth_access_token` (
  `token_id` varchar(256) DEFAULT NULL,
  `token` blob,
  `authentication_id` varchar(256) DEFAULT NULL,
  `user_name` varchar(256) DEFAULT NULL,
  `client_id` varchar(256) DEFAULT NULL,
  `authentication` blob,
  `refresh_token` varchar(256) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `oauth_refresh_token`;
CREATE TABLE `oauth_refresh_token` (
  `token_id` varchar(256) DEFAULT NULL,
  `token` blob,
  `authentication` blob
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
```

配置资源服务器：
```java
@Configuration
@EnableResourceServer
public class OAuth2ResourceServer extends ResourceServerConfigurerAdapter {

    @Resource
    private DataSource dataSource;

    @Bean
    public TokenStore tokenStore() {
        return new JdbcTokenStore(dataSource);
    }

    @Bean
    @Primary
    public DefaultTokenServices tokenServices() {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore());
        return defaultTokenServices;
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources)  {
        resources.tokenServices(tokenServices());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .requestMatchers()
                .antMatchers("/api/**");
    }

}
```

在认证服务器端OAuth2AuthorizationServer代码中同样要配置tokenStore。

```java
    @Resource
    private DataSource dataSource;

    @Bean
    public TokenStore tokenStore() {
        return new JdbcTokenStore(dataSource);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authenticationManager)
                .userDetailsService(userDetailsService)
                .tokenStore(tokenStore());
    }
```
注意：资源服务器与认证服务器使用同一个tokenStore、同一个数据源保存Token数据，这样才能在认证和校验过程中做到Token的共享

2. RedisTokenStore（授权服务器和资源服务器同一套）

- 引入依赖

```xml
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-pool2</artifactId>
    </dependency>
```

- 配置```application.yml```
```yaml
spring:
  redis:
    database: 0 # Redis 数据库索引（默认为 0）
    host: localhost # Redis 服务器地址
    port: 6379 # Redis 服务器连接端口
    password:  # Redis 服务器连接密码（默认为空）
    lettuce:
      pool:
        max-active: 8 # 连接池最大连接数（使用负值表示没有限制） 默认 8
        max-wait: -1 # 连接池最大阻塞等待时间（使用负值表示没有限制） 默认 -1
        max-idle: 8 # 连接池中的最大空闲连接 默认 8
        min-idle: 0 # 连接池中的最小空闲连接 默认 0
```

```java
@Resource
private RedisConnectionFactory connectionFactory;

@Bean
public TokenStore tokenStore() {
    RedisTokenStore redis = new RedisTokenStore(connectionFactory);
    return redis;
}
```

配置完成




