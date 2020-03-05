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
2. WebSecurity配置
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
