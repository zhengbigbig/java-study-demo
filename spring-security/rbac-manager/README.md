# RBAC权限管理模型

### 前置知识：

- h2
- mybatis
- flyway

### 说明： 
- 这里使用mybatis，简单起见，全部使用```@Mapper```，
当然你可以使用mybatis-plus或者jpa，随意
- 启动项目后，请确认h2数据库版本与 依赖一致
- 请修改配置文件.yml中的database url，使用绝对路径
- 连接数据库后，使用mvn flyway:migrate初始化数据库
- 若失败则先执行mvn flyway:clean之后再执行migrate
- 数据库表说明：
    - 用户表
    - 用户角色表、用户角色中间表
    - 访问资源表、角色资源中间表
    - 初始化数据库后，创建用户
    - 请保证第一个注册的用户为admin，第二个为普通用户，因为flyway初始化权限分配写死了

- 一个用户可以拥有多个角色，一个角色可以拥有多个资源权限

## Role-Based Access Control

基于角色的权限控制

- 用户：系统接口及访问的操作者
- 权限：能够访问某接口或者做某操作的授权资格
- 角色：具有一类相同操作权限的用户的总称

### 1. 数据库动态加载用户权限

介绍：
- 前面的demo已经分析了认证的整个源码过程，因此我们需要自定义的东西并不多
- 基于数据库加载用户信息，spring security给我们提供了```DaoAuthenticationProvider```,
因此我们只需要实现```UserDetailsService```的```loadUserByUsername```来加载用户信息
- 从```UserDetailsService```加载出来的是```UserDetails```，
为了方便，我们使用自定义的User来继承该接口并实现，便于后续强转(强制类型转换)

#### 1.1 UserDetails实现
```java
public class User implements UserDetails
```
具体看代码
#### 1.2 UserDetailsService实现
```java
public class CustomUserDetailsService implements UserDetailsService
```
具体看代码
- 主要先用数据库加载用户信息
- 从数据库加载角色信息
- 根据角色信息加载资源权限信息
- 最后返回```UserDetails```

#### 1.3 配置security
```java
    // 鉴权管理
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        daoAuthenticationProvider.setUserDetailsService(customUserDetailsService);
        return daoAuthenticationProvider;
    }
```
当然，也可以简化配置，我这里配置，便于后面添加别的provider

重写之后测试是能通过的

### 2 权限表达式
- SpEL  Spring Expression Language ：Spring表达式语言
- Spring Security 3.0 可使用SpEL来控制授权，表达式返回布尔老赖控制访问
- Spring Security 可用表达式对象基类是```SecurityExpressionRoot```

#### 2.1 常用的权限表达式
```hasRole([role])```	用户拥有指定的角色时返回true （Spring security默认会带有ROLE_前缀）,也可以通过配置移除
```hasAnyRole([role1,role2])```	用户拥有任意一个指定的角色时返回true
```hasAuthority([authority])```	拥有某资源的访问权限时返回true
```hasAnyAuthority([auth1,auth2])```	拥有某些资源其中部分资源的访问权限时返回true
```permitAll```	永远返回true
```denyAll```	永远返回false
```anonymous```	当前用户是anonymous时返回true
```rememberMe```	当前用户是rememberMe用户返回true
```authentication```	当前登录用户的authentication对象
```fullAuthenticated```	当前用户既不是anonymous也不是rememberMe用户时返回true
```hasIpAddress('192.168.1.0/24')```	请求发送的IP匹配时返回true

#### 2.2 SpEL在全局配置中的使用

##### 2.2.1 URL安全表达式

-  ```httpSecurity``` 配置
```java
        http
                .authorizeRequests()
                // 需要放行的资源
                .antMatchers("/login.html", "/login", "/auth/**").permitAll()
                // 权限表达式的使用和自定义
                .antMatchers("/biz1").access("hasRole('ADMIN')")
                .antMatchers("/biz2").hasRole("USER")
                .antMatchers("/person/{id}").access("@rbacService.checkUserId(authentication,#id)")
                .anyRequest().access("@rbacService.hasPermission(request,authentication)");
```

- ```rbacService```实现表达式的Bean
```java
@Service("rbacService")
public class RBACService {

    public boolean hasPermission(HttpServletRequest request, Authentication authentication) {
        // TODO 逻辑处理
        return true;
    }
    public boolean checkUserId(Authentication authentication, int id) {
        // TODO 逻辑处理
        return true;
    }

}
```


##### 2.2.2 Method表达式安全控制

1. 开启方法级别注解配置
```java
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter
```

2. ```PreAuthorize```注解
- 进入方法前进行权限校验，校验未通过，抛出```AccessDeniedException```
```java
@PreAuthorize("hasRole('admin')")
public List<PersonDemo> findAll(){
    return null;
}
```

3. ```PostAuthorize```注解
- 在方法执行后再进行权限验证,适合根据返回值结果进行权限验证
- 返回值判断通过，返回对象，不通过，抛出异常
```java
@PostAuthorize("returnObject.name == authentication.name")
public PersonDemo findOne(){
    String authName =
            SecurityContextHolder.getContext().getAuthentication().getName();
    System.out.println(authName);
    return new PersonDemo("admin");
}
```

4. ```PreFilter```注解
- 对传入的参数进行过滤
```java
@PreFilter(filterTarget="ids", value="filterObject%2==0")
public void delete(List<Integer> ids, List<String> usernames) {

}
```

5. ```PostFilter```注解
- 对返回的结果进行过滤，适用于集合类返回值
```java
@PostFilter("filterObject.name == authentication.name")
public List<PersonDemo> findAllPD(){

    List<PersonDemo> list = new ArrayList<>();
    list.add(new PersonDemo("kobe"));
    list.add(new PersonDemo("admin"));

    return list;
}
```
