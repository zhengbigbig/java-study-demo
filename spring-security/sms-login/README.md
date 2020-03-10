# 短信验证码登录 & 用户多次登录失败账户锁定

## 1. 开发流程

1. 定义```SmsController```，获取短信验证码，将短信验证码保存到session，并将短信验证码下发给用户
2. 自定义短信验证码过滤器```SmsValidateFilter```对用户输入进行校验，
过滤通过后应该仿照用户登录授权来实现一套短信验证码寿宴
3. 自定义短信验证码登录的过滤器```SmsAuthenticationFilter```进行权限校验，并实现相应的provider

## 2. 实现(根据业务实际需求，可在注册时要求填写手机号或者后续做绑定，再用作短信登录)

- 这里直接修改为在注册时增加手机号

### 2.1 在User表增加phone字段、增加User实体的成员变量并修改sql语句
```sql
alter table USER
    add phone varchar(100) unique ;

```
将短信发送相关接口也```PermitAll``

### 2.2 实现```SmsValidateFilter```，实际与图片验证码的过滤器相似

### 2.3 仿照```UsernamePasswordAuthenticationToken```，实现传递的```SmsCodeAuthenticationToken```

### 2.4 仿照```UsernamePasswordAuthenticationFilter```，实现权限过滤器```SmsCodeAuthenticationFilter```

### 2.5 继承```AuthenticationProvider```，实现```SmsCodeAuthenticationProvider```

## 3 对配置进行组装和抽离
```java
@Component
public class SmsCodeSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    @Resource
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Resource
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Resource
    private CustomUserDetailsService customUserDetailsService;

    @Resource
    private SmsValidateFilter smsValidateFilter;

    @Override
    public void configure(HttpSecurity http) throws Exception {

        SmsCodeAuthenticationFilter smsCodeAuthenticationFilter = new SmsCodeAuthenticationFilter();
        // AuthenticationManager 使用共享的
        smsCodeAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
        smsCodeAuthenticationFilter.setAuthenticationSuccessHandler(customAuthenticationSuccessHandler);
        smsCodeAuthenticationFilter.setAuthenticationFailureHandler(customAuthenticationFailureHandler);

        // 获取验证码提供者
        SmsCodeAuthenticationProvider smsCodeAuthenticationProvider = new SmsCodeAuthenticationProvider();
        smsCodeAuthenticationProvider.setUserDetailsService(customUserDetailsService);

        //在用户密码过滤器前面加入短信验证码校验过滤器
        http.addFilterBefore(smsValidateFilter, UsernamePasswordAuthenticationFilter.class);
        //在用户密码过滤器后面加入短信验证码认证授权过滤器        
        http.authenticationProvider(smsCodeAuthenticationProvider)
                .addFilterAfter(smsCodeAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    }
}
```

然后在整体配置中
```java

http.apply(smsCodeSecurityConfig)
```

即可

## 3. 账户锁定

### 3.1 实现原理

- 需要记录用户登录失败的次数nLock，和锁定账号的释放时间releaseTime，可存数据库，可存redis
- 若超出阈值，将nLock = 0，设置releaseTime，通过```setAccountNonLocked(false)```告知Spring Security该账户被锁定
- 登录失败可以选择在鉴权失败处理handler进行详细的操作

### 3.2 多次登录失败锁定用户

#### 3.2.1 使用Spring Security中```UserDetails```接口的```accountNonLocked```字段其setter方法来实现

```User```类中

```java
    
    // 简单起见，使用enabled字段
    // 需要注意的是mysql并没有boolean类型，int或tinyint类型，1就是true，0就是false。
    public void setAccountNonLocked(boolean accountNonLocked){
        this.accountNonLocked = accountNonLocked;
        this.enabled = accountNonLocked;
    }
```

#### 3.2.2 使用```ratelimitj```，对API进行限流，即限制访问频率

这里使用基于内存的使用，还有redis其他等等

```xml
<!-- https://mvnrepository.com/artifact/es.moki.ratelimitj/ratelimitj-inmemory -->
<dependency>
    <groupId>es.moki.ratelimitj</groupId>
    <artifactId>ratelimitj-inmemory</artifactId>
    <version>0.5.0</version>
</dependency>

```

#### 3.2.3 修改```CustomAuthenticationFailureHandler```

- 设置节流参数，15分钟3次失败访问则锁定
- 每次失败处理判断是否触发了锁定机制，若触发则锁定用户，并更新数据库
- 将错误信息返回给前端

### 3.3 重置数据库锁定状态的时机

这种实现方式，实际上是有两个锁定状态

1. 锁定状态是由ratelimitj-inmemory控制的窗口，是存储在内存中的，时间不到，这个窗口是打不开的。除非你重启应用，内存被刷新。
2. 锁定状态是数据库sys_user表的enabled字段，即使认为修改。但是第一个锁定不打开，你人为修改是没用的，还是会被更新为账户锁定。

因此，想要解锁，需要重置这俩个状态。

- 可在用户管理提供一个解锁用户
- 实现一个过滤器来做判断是否应该重置
- 使用Spring定时器轮询，较差方案