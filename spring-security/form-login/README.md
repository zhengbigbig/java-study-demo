# formLogin 登录

## 学习流程

### 1. HttpSecurity配置
#### 1.1 基础配置
```java
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 禁用csrf ，否则会把所有请求当做非法请求拦截，后面再处理
        http.csrf().disable()
                .formLogin()
                .loginPage("/login.html")
                // 重写参数，默认是username,password
                .usernameParameter("uname")
                .passwordParameter("pword")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/index")
                .and()
                .authorizeRequests()
                // 需要放行的资源
                .antMatchers("/login.html","/login").permitAll()
                // 需要对外暴露的资源路径
                .antMatchers("/biz1","/biz2")
                // /biz1、/biz2只需要有admin或者user都可访问
                .hasAnyAuthority("ROLE_user","ROLE_admin")
                .antMatchers("/syslog","/sysuser")
                // admin角色可以访问
                .hasAnyRole("admin") // 等价于.hasAnyAuthority("ROLE_admin")
                .anyRequest().authenticated();
    }
```
#### 1.2 除了可以赋予角色权限还有资源id的权限
```java
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 禁用csrf ，否则会把所有请求当做非法请求拦截，后面再处理
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/syslog").hasAnyAuthority("sys:log")
                .antMatchers("/sysuser").hasAnyAuthority("sys:user")
                // admin角色可以访问
                .anyRequest().authenticated();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("admin")
                .password(passwordEncoder().encode("123456"))
                .authorities("sys:log","sys:user");
    }
```
#### 2. WebSecurity配置
```java
    // 配置webSecurity ignore 掉的 是不会走资源拦截器或者过滤器的 也就是FilterSecurityInterceptor等
    // 一般用来配置静态资源
    @Override
    public void configure(WebSecurity web) {
        //将项目中静态资源路径开放出来
        web.ignoring()
                .antMatchers( "/css/**", "/fonts/**", "/img/**", "/js/**");
    }
```
#### 3. SpringSecurity基本原理解析(想要很好的掌握，必须知道的原理，不然总会踩坑)
##### 3.1 spring security主要一些过滤器链（部分）
- 请求过程中间，会有一个SecurityContext，也就是security上下文
- 请求会依次走一遍过滤器，响应会沿着过滤器链反向走回来，通过```FilterChainProxy```来代理调用
    - 可以看```FilterChainProxy```源码
    还有其静态内部类的```doFilter```方法。
    - ```FilterChainProxy```的```doFilter```除了做一些```context```的清理工作，实际调用```doFilterInternal```
    - 然后new一个其静态内部类调用```VirtualFilterChain```的```doFilter```，调用```additionalFilters```中的filter，实际上是一个List
    - 链式调用完沿着调用链，然后返回

1. ```SecurityContextPersistenceFilter```

- 请求到来时，会在Filter中构建认证主体
2.1 ```BasicAuthenticationFilter```
- 请求到来，将填充```Authentication```至```SecurityContext```，会依次从下面过滤器中过滤，如下为```Authentication```接口
```java
    public interface Authentication extends Principal, Serializable {
        Collection<? extends GrantedAuthority> getAuthorities(); // 权限集合
    
        Object getCredentials(); // 凭证 一般为密码
    
        Object getDetails(); 
    
        Object getPrincipal();  // 主体 一般为username或用户信息
    
        boolean isAuthenticated(); // 是否鉴权成功，初始化为false，不可以手动更改的呢，需要按照规定鉴权通过
    
        void setAuthenticated(boolean var1) throws IllegalArgumentException;
    }
```
- 若不是对应的过滤器，则会被放行

2.2 ```UsernamePasswordAuthenticationFilter```
2.3 ```RememberMeAuthenticationFilter```
2.4 ```SmsCodeAuthenticationFilter```
2.5 ```SocialAuthenticationFilter```
2.6 ```OAuth2AuthenticationProcessingFilter```
2.7 ```OAuth2ClientAuthenticationProcessingFilter```
3. ```AnonymousAuthenticationFilter```
4. ```ExceptionTranslationFilter```
5. ```FilterSecurityInterceptor```
    1. 上面过滤器过滤完毕，若Authentication实体中```isAuthenticated```为```true```，才会经过拦截器资源校验得以放行
    2. 若未通过 或者前面过滤器认证失败，都会直接被```ExceptionTranslationFilter```拦截，然后抛出异常， 401 403等，也可以自定义行为
    3. 请求过滤器链走到最后，响应会沿着过滤器链往回，直到```SecurityContextPersistenceFilter```，会将```SecurityContext```保存到```session```
    4. 下一次请求，会从```session```将```SecurityContext```取出来，持久化到```session```
    下面是```SecurityContext```源码，实际上就是存放```Authentication```
```java
public interface SecurityContext extends Serializable {
    Authentication getAuthentication();

    void setAuthentication(Authentication var1);
}
```

##### 3.2 spring security 权限校验源码分析
3.2.1 ```UsernamePasswordAuthenticationFilter```源码解析
- 该过滤器继承```AbstractAuthenticationProcessingFilter```，其主要作用：
    1. 通过构造器，设置请求的url和请求的method
    ```java
    public UsernamePasswordAuthenticationFilter() {
            super(new AntPathRequestMatcher("/login", "POST"));
        }
    ```
    2. 通过```obtainUsername```和```obtainPassword```设置```username```和```password```
    3. 通过```public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)```对获取到的username和password
    构建成```UsernamePasswordAuthenticationToken```，调用```this.getAuthenticationManager().authenticate(authRequest)```进行鉴权，若鉴权通过则返回```Authentication```，失败则直接被异常处理
        - 别问为什么this调用的是父类，而源码中```AuthenticationManager```这个成员变量及其```getter/setter```方法
        - 当子父类，子类使用```this```调用自己的属性或方法，使用```super```调用父类的属性或者方法，若子类调用的对象不存在，那么就会去父类中找，去调用，若父类没有，就报错喽
    4. ```AuthenticationManager```实际上是一个接口，```ProviderManager```实现了该接口，```ProviderManager```中保存了实现了```AuthenticationProvider```的provider的List
    5. 当过滤器做校验时，会根据```Token```的class类型，来调用对应的```AuthenticationProvider```，校验成功，则返回，校验失败则抛出异常
    6. spring security有一系列的```AuthenticationProvider```的实现，譬如最常用的```DaoAuthenticationProvider```，从数据库加载，当然也可以自定义地去实现，只需要将自定义的provider放入List中，后面再说
3.2.2 ```DaoAuthenticationProvider```源码解析
- 该provider，只需要实现```UserDetailsService```的```loadUserByUsername```方法，并通过setter方法传入给provider
    1. 该provider首先通过```retrieveUser```从```UserDetailsService```检索用户，加载失败则抛出异常，成功执行下一步
    2. 通过```additionalAuthenticationChecks```检查用户的凭证信息，并使用```passwordEncoder```，匹配登录使用的密码和数据库加过salt的密码是否一致，失败抛出异常，成功执行下一步
    3. 调用```createSuccessAuthentication```，判断是否需要重新进行加密提高安全性，若是，需要```setUserDetailsPasswordService```，使用```passwordEncoder```对登录密码进行add salt
    4. 之后调用父类的```createSuccessAuthentication```返回```UsernamePasswordAuthenticationToken```
    5. 出栈回跳到```UsernamePasswordAuthenticationFilter```，返回Token，鉴权成功
3.2.3 ```UserDetailsService```
    1. ```UserDetailsService```实际上 只需要实现加载数据，可以是从数据库也可以是redis等
    2. 加载出来的```UserDetails```如下，继承并实现User类即可
 ```java
public interface UserDetails extends Serializable {
	Collection<? extends GrantedAuthority> getAuthorities(); // 权限集合
	String getPassword();
	String getUsername();
	boolean isAccountNonExpired(); // 账户是否过期
	boolean isAccountNonLocked(); // 账户是否锁定
	boolean isCredentialsNonExpired(); // 凭证是否过期
	boolean isEnabled(); // 是否允许使用
}

```
##### 3.3 校验流程
3.3.1 校验流程
    1. 用户访问```/login```，被```UsernamePasswordAuthenticationFilter```拦截
    2. ```UsernamePasswordAuthenticationFilter```构建```UsernamePasswordAuthenticationToken```调用```AuthenticationManager```校验
    3. ```AuthenticationManager```中根据对应```AuthenticationProvider```中```supports```支持的```Token```类型调用对应的```provider```
    4. ```AuthenticationProvider```加载用户数据，然后校验并返回```Token```给```UsernamePasswordAuthenticationFilter```
    5. ```UsernamePasswordAuthenticationFilter```将调用父类```AbstractAuthenticationProcessingFilter```
        - 将```Token```信息保存到```session```
        - 然后通过```successfulAuthentication```的```SecurityContextHolder.getContext().setAuthentication(authResult)``` 将```Authentication``` 保存到上下文
    6. ```SecurityContext```存入到```session```
    7. 下一次再请求会直接拿到```SecurityContext```中```Authentication```对象
    8. 拿到之后将直接跳过登录认证相关的过滤器到```FilterSecurityInterceptor```
    9. ```FilterSecurityInterceptor```会看当前的```Authentication```中```isAuthentated```是否为```true```
    10. 通过之后将根据资源权限进行访问

总结：熟悉整个过滤器流程后，就可以开始自定义的对自己的实际业务进行处理了。
