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
curl -X POST --user client1:123456 http://localhost:8001/oauth/token  -H "content-type: application/x-www-form-urlencoded" -d "code=2gMHpI&grant_type=authorization_code&redirect_uri=http://localhost:8888/callback&scope=all"
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



