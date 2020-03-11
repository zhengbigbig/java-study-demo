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
create table UserConnection (
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
create unique index UserConnectionRank on UserConnection(userId, providerId, rank);
```
- 这张表里面的数据，是通过注册或者绑定操作加入进去的，与认证、鉴权过程无关
- userId 自开发应用的用户唯一标识
- provider 服务提供商，社交媒体平台唯一标识
- providerUserId 服务提供商用户的唯一标识

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