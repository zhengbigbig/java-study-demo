# Spring Social Oauth2

## 前置

- 关于Oauth2基本知识
请看这篇
[OAuth 2.0 的四种方式](http://www.ruanyifeng.com/blog/2019/04/oauth-grant-types.html)

- 关于简单实现sso的demo 在[sso-server](https://github.com/zhengbigbig/java-study-demo/tree/master/sso-server)

- ```Spring Social```对```OAuth2```标准，做了封装，这里使用其来实现第三方登录认证，即我们应用作为客户端

## 1. Spring Social介绍

Spring Social是一个帮助我们连接社交媒体平台，方便在我们自己的应用上开发第三方登录认证等功能的Spring 类库。

- 一般自己手写开发，一般需要实现：用户授权-> 获取授权码->使用授权码调取许可令牌->使用令牌换取所需资源
- 但是```Spring Security```提供了OAuth2Operations接口，其默认的实现类是OAuth2Template，简化了自定义工作，只需要微调即可

### 1.1 OAuth2认证

- 默认实现```OAuth2Template```功能：
认证过程中所有与OAuth2认证服务器交互的工作就全交给```OAuth2Operations```，最后返回给我们一个AccessToken

### 1.2 接口资源鉴权

- 获取```AccessToken```之后，即可请求OAuth2资源服务器里面的资源
- 只需要使用```RestTemplate```，通用的HTTP工具类处理请求与响应，处理各种数据格式JSON、XML的类库，RestTemplate会自行根据环境判断使用哪一个。

### 1.3 自定义业务接口

- 每个平台业务接口都不相同，因此需要自定义开发不同的接口实现APIImpl
- 需要一个统一的父类，包含accessToken和RestTemplate，这样我们的自定义接口实现就可以通过继承这个类获得并使用accessToken和RestTemplate
- 使用这个统一的父类```AbstractOAuth2Binding```，它还帮助我们实现HTTP请求的参数携带，以及请求结果到对象的反序列化工作等

至此，OAuth2Operations 和 自定义接口实现APIImpl，一个负责认证流程请求响应，一个负责资源请求响应。
二者统一被封装为ServiceProvider-服务提供商。
我们自己的应用与社交媒体平台（服务提供商）的HTTP交互过程就已经可以被全部支持了。

### 1.4 确定用户关系
```sql
create table sys_UserConnection (
        userId varchar(255) not null,
	providerId varchar(255) not null,
	providerUserId varchar(255),
	rank int not null,
	displayName varchar(255),
	profileUrl varchar(512),
	imageUrl varchar(512),
	accessToken varchar(512) not null,
	secret varchar(512),
	refreshToken varchar(512),
	expireTime bigint,
	primary key (userId, providerId, providerUserId));
create unique index UserConnectionRank on sys_UserConnection(userId, providerId, rank);
```
- 这张表里面的数据，是通过注册或者绑定操作加入进去的，与认证、鉴权过程无关
- userId 自开发应用的用户唯一标识
- provider 服务提供商，社交媒体平台唯一标识
- providerUserId 服务提供商用户的唯一标识
- 前缀与后续相关

这三个字段体现自开发应用的用户与服务提供商用户之间的关系，从而判定服务提供商的用户是否可以通过OAuth2认证登录我们的应用

从```1.2```中我们拿到了资源信息：

- 获取用户数据User后，因为这个User在不同平台，结构不同，而```spring Social```只认识一种用户的数据结构，那就是```Connection（OAuth2Connection）```
- 因此我们需要实现```ApiAdapter```来适配
- 拿到```Connection```信息后，通过```UsersConnectionRepository```加载自己平台的```userId```，加载到，则说明验证成功

### 1.5 本地应用授权

- 获取到```userId```后，并不意味着你可以访问本地应用中的所有资源，还需要契合```Spring Security```的鉴权流程
- 通过```userId```获取 ```UserDetails```，并授权
- 之前的使用用户名密码登陆的案例中，是通过实现```UserDetailsService```和```UserDetails```接口来实现的，
在社交媒体登录过程中，我们需要实现的接口是```SocialUserDetailsService```和```SocialUserDetails```，原理一样

## 2. Spring Social源码分析

- Spring Social自动配置会在过滤器链中加入一个```SocialAuthenticationFilter```过滤器，该过滤器拦截社交媒体登录请求
- ```SocialAuthenticationFilter```过滤器拦截的社交媒体登录请求的地址是{filterProcessesUrl}/{providerId}。
- filterProcessesUrl的默认值是“/auth”，如果你的服务提供商providerId(自定义)是github，那么你的社交媒体登录按钮请求的地址就应该是“/auth/github”，当然这两个值我们都可以修改。

### 2.1 鉴权流程

1. 当用户点击"github登录"按钮，此时访问/{filterProcessesUrl}/{providerId}被拦截，此时用户没有被认证通过，所以跳转到GitHub授权页面（authorizeUrl）上，用户输入用户密码授权，在浏览器跳回到本地应用，仍然回到/{filterProcessesUrl}/{providerId}再次被拦截。
2. 首先要检测用户是否授权使用第三方平台用户信息，如果没授权就直接抛出异常。如果用户授权了，就去执行OAuth2一系列的请求响应，获取授权码、AccessToken、Connection用户信息。这个过程代码在OAuth2AuthenticationService中被定义。
3. 如果授权失败（该社交平台用户在本地应用中没有对应的用户），则跳转到signUpUrl。该页面是将本系统用户和“服务提供商”用户进行关系绑定页面。

注意：

- Spring Social实现的OAuth2认证鉴权流程中，使用到了session，详细可看源码```SocialAuthenticationFilter```。
- 因此当你的应用是一个无状态应用时，需要对Spring Social进行一定程度的改造。
简单的做法就是：
使用session开发有状态应用，并且session保存的状态信息交给redis集中管理；
或者开发无状态应用之前，确定该应用不需要社交媒体登录功能，比如某企业内网应用。

## 3. 实现QQ登录

### 3.1 配置依赖

```xml
<!-- 非中央仓库 -->
<repositories>
    <repository>
        <id>spring-milestones</id>
        <name>Spring Milestones</name>
        <url>https://repo.spring.io/libs-milestone</url>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
    </repository>
</repositories>

<dependency>
    <groupId>org.springframework.social</groupId>
    <artifactId>spring-social-security</artifactId>
    <version>2.0.0.M4</version>
</dependency>
<dependency>
    <groupId>org.springframework.social</groupId>
    <artifactId>spring-social-config</artifactId>
    <version>2.0.0.M4</version>
</dependency>
```

### 3.2 OAuth2Template

- 如果是标准的Oauth2，则不需要改造，但QQ使用的是```&```分割的字符串
- 定义QQOAuth2Template，对postForAccessGrant进行重写

### 3.3 定义QQ用户信息返回类```QQUser```

### 3.4 定义QQ用户信息获取接口```QQApi```，并实现```QQApiImpl```接口

- 根据accessToken获取openId
- 根据openId获得用户信息

### 3.5  服务提供商ServiceProvider

应用通过OAuth2协议与服务提供商进行交互，主要有两部分

- 认证流程，获取授权码、获取AccessToken，这部分是标准的OAuth2认证流程，各平台差异较小，
由QQOAuth2Template（OAuth2Operations）帮我们完成
- 二是请求接口，获取用户数据，获取openId。这部分每个平台都不一样，需要我们自定义完成，如QQApiImpl（QQAPI）

需要将这两部分内容的封装结果告知ServiceProvider，从而可以被正确调用。

实现
```java
public class QQServiceProvider extends AbstractOAuth2ServiceProvider<QQApi>
```

### 3.6 QQ用户信息适配

- 不同的社交媒体平台（QQ、微信、GitHub）用户数据结构各式各样，但是Spring Social只认识Connection这一种用户信息结构。
- 所以需要将QQUser与Connection进行适配

实现
```java
public class QQApiAdapter implements ApiAdapter<QQApi>
```
主要重写```setConnectionValues```，实现providerUser与本地用户的适配信息

- 自定义OAuth2ConnectionFactory，通过QQServiceProvider发送请求，通过QQApiAdapter将请求结果转换为Connection。

```java
public class QQConnectionFactory extends OAuth2ConnectionFactory<QQApi> {

    public QQConnectionFactory(String providerId, String appId, String appSecret) {
        super(providerId, new QQServiceProvider(appId, appSecret), new QQApiAdapter());
    }

}
```

QQConnectionFactory构造方法的第一个参数是providerId可以随便定义，但是最好要具有服务提供商的唯一性和可读性。比如：qq、wechat。
第二个参数和第三个参数是在服务提供商创建应用申请的APP ID和APP KEY。

### 3.7 Spring Social自动装载配置

```java
@Configuration
@EnableSocial
public class QQAutoConfiguration extends SocialConfigurerAdapter
```

主要作用：
- 连接数据库UserConnection表的持久层封装
- 向Spring Social添加一个ConnectionFactory

### 3.8 配置过滤器

- filterProcessesUrl是用于拦截用户QQ登录请求和认证服务器回调请求的路径
- 如果不做配置默认是“/auth”

```java
@Configuration
public class QQFilterConfigurer extends SpringSocialConfigurer
```
并在```QQAutoConfiguration```中配置

```java
    @Bean
    public SpringSocialConfigurer qqFilterConfig() {
        // filterProcessesUrl是用于拦截用户QQ登录请求和认证服务器回调请求的路径
        QQFilterConfigurer configurer = new QQFilterConfigurer("/login");
        // 用户绑定界面signupUrl
        configurer.signupUrl("/bind.html");
        // 登录成功跳转页面postLoginUrl
        configurer.postLoginUrl("/index");
        return configurer;
    }
```

### 3.9 在SecurityConfig配置

```java
@Resource
private SpringSocialConfigurer qqFilterConfig;


@Override
protected void configure(HttpSecurity http) throws Exception {
    http.apply(qqFilterConfig).and()
    ...
}
```

### 3.10 登录
```html
<a href="/login/qq">QQ登录</a>
```

- 这个登录地址分为两段，login是上文中配置的filterProcessesUrl，qq是上文中配置的providerId
- QQ登录路径的配置一定要与filterProcessesUrl和providerId对应上，否则登录请求无法正确拦截
- 在QQ互联的回调域的配置也必须是http://域名:端口/{filterProcessesUrl}/{providerId},否则用户认证回调无法正确拦截


## 4. 使用

### 4.1 Spring Social依赖于session，所以不要设置无状态模式，否则无法正确跳转。

### 4.2 加载用户信息

- ApiAdapter将QQ平台的用户标准数据结构QQUser转换为Spring Social用户标准的数据结构Connection
- 我们需要通过服务提供商的用户信息Connection，得到我们自己开发的系统的用户信息
- 使用```UserConnectionRepository```获取```userId```

因此需要实现```SocialUserDetails```和```SocialUserDetailsService```


### 4.3 QQ登录用户关系绑定

- 注册Bean，然后实现用户的绑定，可以在Controller中，上文要使用session的原因，也是服务商的信息，保存在了seession中
- 当然也可以在授权完成后，若本地用户也没有注册。可以注册加绑定一起做

```java
providerSignInUtils.doPostSignUp(username, new ServletWebRequest(request));
```
```java
@Bean
public ProviderSignInUtils providerSignInUtils(ConnectionFactoryLocator connectionFactoryLocator) {
    return new ProviderSignInUtils(connectionFactoryLocator,
            getUsersConnectionRepository(connectionFactoryLocator)) {
    };
}
```



