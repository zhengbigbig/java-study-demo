# Spring Security JWT && CORS && CSRF && JWT集群

## 1. JWT介绍

### 1.1 基于Session的应用存在的问题（不适用的场景）

cookies和sessionid是服务端和浏览器端自行维护，编码层面开发者只能感知session的存取

- 无浏览器的应用
- 集群应用

当然集群应用也可以通过，共享session集群来实现，譬如redis共享session等

### 1.2 JWT（JSON web tokens）认证

- 客户端登录服务端申请JWT令牌，服务端生成令牌并返回给客户端
- 客户端再次访问后续需要鉴权接口时，请求时在请求头中携带JWT
- 服务端校验客户端请求的JWT，通过则授权访问，响应资源请求结果

### 1.3 JWT结构

- Header，通常用于说明JWT使用的算法信息
- payload，通常用户携带自定义的信息，一般采用对等加密解密，可以进行明文解码
- signature，对前两部分数据的签名，防止被篡改。需要制定secret，进行签名和解签

## 2. JWT在Spring Security中的认证流程

1. 定义JWT工具类，用于令牌的生成，校验，刷新等
2. 定义接口，```/authentication```用于登录验证，通过后生成JWT返回客户端，
```refreshtoken```用于刷新JWT，更新JWT有效期，避免用户使用时过期，避免不好的体验
3. 定义服务类```JwtAuthService```，登录、刷新相关的实现
4. 定义Token的过滤器：```JwtAuthenticationTokenFilter```，对需要鉴权的url进行鉴权

## 3. 在上一节代码基础上进行实现

1. JWT完全使用JSON接口，没有使用表单提交，去除```formLogin```登录模式
2. JWT用于开发前后端分离的无状态应用，所以项目中去掉与session相关的内容，去掉页面视图相关的内容
3. 引入JWT工具包并定义工具类```JwtTokenUtil```

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.0</version>
</dependency>
```

4. 实现```JwtAuthService```，核心业务逻辑
- 登录用户鉴权
- 通过后，将```Authentication```保存到上下文
- 加载用户信息，生成token
- token过期，刷新token

5. 实现```JwtAuthenticationTokenFilter```对需要鉴权的接口过滤

- 先确定请求头有无token
- 若无，执行下一个过滤器，若有，解签token，查看是否有用户信息，和判断当前上下文有没有保存用户信息
- 若token中有用户信息且上下文没有保存，则从数据库加载用户信息
- 校验token的有效性，若有效，则组装```UsernamePasswordAuthenticationToken```，并设置到上下文
- 最后执行下一个过滤器

6. 总结流程

- 用户访问权限接口，若没有有效token，则提示先登录
- 登录后，返回token，这个token是保存在客户端的，服务器下发后，无法控制
- 再次访问权限接口，若token校验通过，则可访问，若失败，则被后续过滤器异常捕获
- 抛出异常，客户端刷新token


## 使用postman进行测试

## 4. CORS

### 4.1 在Spring或Spring Boot实现跨域资源共享

主要四种实现方式：

1. @CrossOrigin注解，这个注解是作用于Controller类或者请求方法上的，实现局部接口的跨域资源共享。
2. 实现WebMvcConfigurer接口addCorsMappings方法，实现全局配置的跨域资源共享。
3. 注入CorsFilter过滤器，实现全局配置的跨域资源共享。推荐使用。
4. 使用HttpServletResponse设置响应头(局部跨域配置)，不推荐

### 4.2 Spring Security 中的配置CORS

首先配置http.cors()
```java
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and()
        ...
    }
}
```

再配置Spring Security提供的```CorsConfigurationSource```，等同于注入```CorsFilter```过滤器

```java
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        //开放哪些ip、端口、域名的访问权限，星号表示开放所有域
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8888"));
        //是否允许发送Cookie信息
        configuration.setAllowCredentials(true);
        //开放哪些Http方法，允许跨域访问
        configuration.setAllowedMethods(Arrays.asList("GET","POST", "PUT", "DELETE"));
        //允许HTTP请求中的携带哪些Header信息
        configuration.addAllowedHeader("*");
        //暴露哪些头部信息（因为跨域访问默认不能获取全部头部信息）
        configuration.addExposedHeader("*");
        configuration.applyPermitDefaultValues();
        //添加映射路径，“/**”表示对所有的路径实行全局跨域访问权限的设置
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
```

## 5. CSRF跨站攻击防护

### 5.1 如何防御CSRF攻击

- 为系统中的每一个连接请求加上一个token，这个token是随机的，服务端对该token进行验证。
- 跳转提醒，点击第三方连接，提醒用户

### 5.2 Spring Security的CSRF token攻击防护

```java
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf()
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .ignoringAntMatchers("/authentication");
        .and()
        ...
    }
}
```

- 使用CookieCsrfTokenRepository生成CSRF Token放入cookie，并设置cookie的HttpOnly=false，允许读取该cookie。
这样非浏览器等无法自动维护cookie的客户端可以读取cookie中的CSRF Token，以供后续资源请求中使用
- 使用ignoringAntMatchers开放一些不需要进行CSRF防护的访问路径，比如：登录授权。

至此，我们生成了CSRF token保存在了cookies中，浏览器向服务端发送的HTTP请求，都要将CSRF token带上，服务端校验通过才能正确的响应。
这个校验的过程Spring Security会自动处理。

### 5.3 前端请求携带CSRF Token的方式

- 在thymeleaf模板中可以使用如下方式，其他类似

#### 5.3.1 在Header中携带CSRF token

```js
var headers = {};
headers['X-CSRF-TOKEN'] = "${_csrf.token}";
$.ajax({    
    headers: headers,    
});
```
#### 5.3.2 直接作为参数提交

```js
$.ajax({    
    data: {      
       "_csrf": "${_csrf.token}"        
    }
});
```

#### 5.3.3 form表单的隐藏字段

```html
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
```

## 6. JWT集群应用方案

- 上文使用JWT做授权服务，实际上，token是没用保存在服务端的，因此，分布式部署做集群时，
只需要保证使用同一套代码和同一套签名、同一个数据库
- 当然若是要实现其他的一些功能，譬如token的管理，则需要服务端保存token，可以使用redis/数据库等等