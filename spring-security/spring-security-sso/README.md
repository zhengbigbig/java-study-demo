# spring-security sso

- 单点登录（Single Sign On），简称为SSO
- 单点登陆本质上也是OAuth2的使用，所以其开发依赖于授权认证服务

## 1. 单点客户端配置 @EnableOAuth2Sso

- 基于```spring-security-oauth2```新增依赖

```xml
<!-- https://mvnrepository.com/artifact/org.springframework.security.oauth.boot/spring-security-oauth2-autoconfigure -->
<dependency>
    <groupId>org.springframework.security.oauth.boot</groupId>
    <artifactId>spring-security-oauth2-autoconfigure</artifactId>
    <version>2.2.5.RELEASE</version>
</dependency>
```

配置
```java
@Configuration
@EnableOAuth2Sso
public class ClientSecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  public void configure(HttpSecurity http) throws Exception {
      http.authorizeRequests()
              .antMatchers("/","/error","/login").permitAll()
              .anyRequest().authenticated()
              .and()
              .csrf().disable();
  }
}

```

## 3. application.yml 配置

配置解析：
- security.oauth2.client.user-authorization-uri = /oauth/authorize 请求认证的地址，即获取code 码
- security.oauth2.client.access-token-uri = /oauth/token 请求令牌的地址
- security.oauth2.resource.jwt.key-uri = /oauth/token_key 解析jwt令牌所需要密钥的地址,服务启动时会调用 授权服务该接口获取jwt key，所以务必保证授权服务正常
- security.oauth2.client.client-id = client1     clientId 信息
- security.oauth2.client.client-secret = 123456   clientSecret 信息

```yaml
security:
  oauth2:
    client:
      user-authorization-uri: ${auth-server}/oauth/authorize #请求认证的地址
      access-token-uri: ${auth-server}/oauth/token #请求令牌的地址
      client-id: client1
      client-secret: 123456
      scope: all
    resource:
      token-info-uri: ${auth-server}/oauth/check_token
    sso:
      login-path: /login #指向登录页面的路径，即OAuth2授权服务器触发重定向到客户端的路径 ，默认为 /login

server:
  servlet:
    session:
      cookie:
        name: OAUTH2CLIENTSESSION  # 解决  Possible CSRF detected - state parameter was required but no state could be found  问题


```

## 4. @EnableOAuth2Sso注解源码解析

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableOAuth2Client
@EnableConfigurationProperties(OAuth2SsoProperties.class)
@Import({ OAuth2SsoDefaultConfiguration.class, OAuth2SsoCustomConfiguration.class,
  	ResourceServerTokenServicesConfiguration.class })
public @interface EnableOAuth2Sso {

}

```

主要4个配置文件的引用： ResourceServerTokenServicesConfiguration 、OAuth2SsoDefaultConfiguration 、 OAuth2SsoProperties 和 @EnableOAuth2Client：

- OAuth2SsoDefaultConfiguration 单点登陆的核心配置，内部创建了 SsoSecurityConfigurer 对象， SsoSecurityConfigurer 内部 主要是配置 OAuth2ClientAuthenticationProcessingFilter 这个单点登陆核心过滤器之一。
- ResourceServerTokenServicesConfiguration  内部读取了我们在 yml 中配置的信息
- OAuth2SsoProperties 配置了回调地址url ，这个就是 security.oauth2.sso.login-path=/login  匹配的
- @EnableOAuth2Client   标明单点客户端，其内部 主要 配置了  OAuth2ClientContextFilter 这个单点登陆核心过滤器之一

过滤器```OAuth2ClientContextFilter```主要内容：
- 记录当前地址(currentUri)到HttpServletRequest
- 判断当前异常 UserRedirectRequiredException 对象 是否为空
- 重定向访问 授权服务 /oauth/authorize

过滤器```OAuth2ClientAuthenticationProcessingFilter```主要内容：
- 获取到的code码调用
- 授权服务 /oauth/token 接口获取 token 信息
- 将获取到的token 信息解析成 OAuth2Authentication 认证对象

测试：
访问客户端受限资源 》 到授权服务器登录 》 可以访问







