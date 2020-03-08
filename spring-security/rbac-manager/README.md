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

### 2. 利用权限表达式简化```httpSecurity```

#### 2.1 权限表达式
```java
        http
                .authorizeRequests()
                // 需要放行的资源
                .antMatchers("/login.html", "/login", "/auth/**").permitAll()
                // 权限表达式的使用和自定义
                .antMatchers("/biz1").access("hasRole('ADMIN')")
                .antMatchers("/biz2").hasRole("USER")
                .anyRequest().access("@rbacService.hasPermission(request,authentication)");
```

#### 2.2 ```rbacService```实现
```java
@Service("rbacService")
public class RBACService {
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Resource
    private UserMapper userMapper;

    /**
     * 判断某用户是否具有该request资源的访问权限
     */
    public boolean hasPermission(HttpServletRequest request, Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            // 从数据库动态加载，避免权限不是最新
            List<String> urls = userMapper.getPermissionsByUsername(username);
            return urls.stream().anyMatch(
                    url -> antPathMatcher.match(url, request.getRequestURI())
            );
        }
        return false;
    }
}
```