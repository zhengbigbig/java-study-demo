package com.zbb.basicserver.auth;

import com.zbb.basicserver.auth.token.JwtAuthenticationTokenFilter;
import com.zbb.basicserver.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String[] ignoreAuthUrl =
            {"/authentication", "/refreshtoken", "/auth/**", "/oauth/**",
                    "/favicon.ico","/error","oauth/check_token"};

    @Resource
    CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Resource
    CustomLogoutSuccessHandler customLogoutSuccessHandler;

    @Resource
    CustomUserDetailsService customUserDetailsService;

    @Resource
    private DataSource dataSource; // 对应yml中的数据库配置

    @Resource
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    // http security 配置
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        // 禁用csrf ，否则会把所有请求当做非法请求拦截，后面再处理
//
//        http
//                .formLogin()
//                .loginProcessingUrl("/authentication")
//                .successHandler(customAuthenticationSuccessHandler)
//                .failureUrl("/login.html")
//                .and()
//                .csrf()
//                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
//                .ignoringAntMatchers(ignoreAuthUrl)
//                .and()
//                .cors().and()
//                .logout()
//                .logoutUrl("/signout") // 退出调用，默认是/logout
//                // 退出成功后访问的页面
////                .logoutSuccessUrl("/aftersignout.html")
//                // 删除Cookie
//                .deleteCookies("JSESSIONID")
//                .logoutSuccessHandler(customLogoutSuccessHandler)
//
//                .and().rememberMe()
//                .rememberMeParameter("remember-me") // 接受客户端的参数
//                .rememberMeCookieName("remember-me-cookie") // 返回给浏览器的cookie名称
//                .tokenValiditySeconds(2 * 24 * 60 * 60) // remember-me token有效时间
//                .tokenRepository(persistentTokenRepository())
//
//                .and()
//                .authorizeRequests()
//                // 需要放行的资源
//                .antMatchers(ignoreAuthUrl).permitAll()
//                // 权限表达式的使用和自定义
//                .anyRequest().access("@rbacService.hasPermission(request,authentication)");
//
//        http
//                .sessionManagement()
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
//                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
//    }

    // 配置webSecurity ignore 掉的 是不会走资源拦截器或者过滤器的 也就是FilterSecurityInterceptor等
    // 一般用来配置静态资源
    @Override
    public void configure(WebSecurity web) {
        //将项目中静态资源路径开放出来
        web.ignoring()
                .antMatchers("/css/**", "/fonts/**", "/img/**", "/js/**");
    }

    // 鉴权管理,配置customUserDetailsService为全局的
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 这样写，默认使用DaoAuthenticationProvider
        auth.userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    // 设置加密
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        // jdbc操作数据库
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        return tokenRepository;
    }

    // 配置跨域
//    @Bean
//    CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        //开放哪些ip、端口、域名的访问权限，星号表示开放所有域
//        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8888"));
//        //是否允许发送Cookie信息
//        configuration.setAllowCredentials(true);
//        //开放哪些Http方法，允许跨域访问
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
//        //允许HTTP请求中的携带哪些Header信息
//        //暴露哪些头部信息（因为跨域访问默认不能获取全部头部信息）
//        configuration.applyPermitDefaultValues();
//        //添加映射路径，“/**”表示对所有的路径实行全局跨域访问权限的设置
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
}
