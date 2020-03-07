#spring security demo study 

## httpBasic
- spring security 提供的默认登录，无需自己手写登录界面
- 简单的密码认证，可作为企业内部使用，存在被破解风险
核心配置如下：
### 1. 拦截所有
```java
@Configuration
public class SecurityConfig  extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic()
                .and()
                .authorizeRequests().anyRequest()
                .authenticated();
    }
}
```
### 2. 简单的配置权限，当然，后续可以通过数据库来动态地鉴权
```java
        http
                .authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/home").access("hasRole('USER')")
                .antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
                .and()
                .httpBasic();
```
### 3. 基于内存的鉴权，简单的配置用户信息及权限，简单的密码认证
#### 3.1 可在```.yml```或者```.properties```中配置，请注意后者优先级更高
```xml
spring:
  security:
    loginType: JSON
    user:
      name: admin
      password: password
      roles: ADMIN,USER
```
#### 3.2 可在```SecurityConfig```中配置，需要对密码的存储添加存储格式，若不，则会报错呗
  - 说明： 在此之前的Spring Security 5.0的默认PasswordEncoder是NoOpPasswordEncoder其所需的明文密码。在Spring Security 5中，默认值为DelegatingPasswordEncoder，需要密码存储格式
##### 解决方案1 –添加密码存储格式，对于纯文本，添加{noop}
```java
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("user").password("{noop}password").roles("USER")
                .and()
                .withUser("admin").password("{noop}password").roles("USER", "ADMIN");
    }
```
##### 解决方案2 - User.withDefaultPasswordEncoder()为UserDetailsService
```java
    @Bean
    public UserDetailsService userDetailsService() {

        User.UserBuilder users = User.withDefaultPasswordEncoder();
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(users.username("user").password("password").roles("USER").build());
        manager.createUser(users.username("admin").password("password").roles("USER", "ADMIN").build());
        return manager;

    }
```
#### 3.3 基于内存的方式的坑
- 在使用基于内存的用户时，在第一次登录后```SecurityContextHolder.getContext()```会保留你的登录信息，即使你重启了应用，也还是在
- 除非你主动```SecurityContextHolder.clearContext()```，清除上下文，当然还有别的方式，暂时先不提

### 4. 基于JDBC 鉴权
```java
@Autowired
private DataSource dataSource;
 
@Autowired
public void configureGlobal(AuthenticationManagerBuilder auth) 
  throws Exception {
    auth.jdbcAuthentication().dataSource(dataSource)
      .withDefaultSchema()
      .withUser("user").password("password").roles("USER")
      .and()
      .withUser("admin").password("password").roles("USER", "ADMIN");
}
```