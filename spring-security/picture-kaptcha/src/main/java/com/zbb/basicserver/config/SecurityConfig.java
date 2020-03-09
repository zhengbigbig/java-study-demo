package com.zbb.basicserver.config;

import com.zbb.basicserver.auth.*;
import com.zbb.basicserver.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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

import javax.annotation.Resource;
import javax.sql.DataSource;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Resource // Resource和Autowired的区别是前者按照名称（byName）来装配，后者按照类型（byType）装配
            CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    @Resource
    CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Resource
    CustomExpiredSessionStrategy customExpiredSessionStrategy;

    @Resource
    CustomLogoutSuccessHandler customLogoutSuccessHandler;

    @Resource
    CustomUserDetailsService customUserDetailsService;

    @Resource
    private DataSource dataSource; // 对应yml中的数据库配置

    @Resource
    private KaptchaFilter kaptchaFilter;

    // http security 配置
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 禁用csrf ，否则会把所有请求当做非法请求拦截，后面再处理
        http
                .addFilterBefore(kaptchaFilter, UsernamePasswordAuthenticationFilter.class)
                .logout()
                .logoutUrl("/signout") // 退出调用，默认是/logout
                // 退出成功后访问的页面
//                .logoutSuccessUrl("/aftersignout.html")
                // 删除Cookie
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler(customLogoutSuccessHandler)

                .and()
                .rememberMe()
                .userDetailsService(customUserDetailsService)
                .rememberMeParameter("remember-me") // 接受客户端的参数
                .rememberMeCookieName("remember-me-cookie") // 返回给浏览器的cookie名称
                .tokenValiditySeconds(2 * 24 * 60 * 60) // remember-me token有效时间
                .tokenRepository(persistentTokenRepository())

                .and()
                .csrf().disable()
                .formLogin()
                .loginPage("/login.html")
                .usernameParameter("uname")
                .passwordParameter("pword")
                .loginProcessingUrl("/login")
//                .defaultSuccessUrl("/index")
                .successHandler(customAuthenticationSuccessHandler)
//                .failureUrl("/login.html")
                .failureHandler(customAuthenticationFailureHandler)

                .and()
                .authorizeRequests()
                // 需要放行的资源
                .antMatchers("/login.html", "/login", "/auth/**", "/aftersignout.html", "/kaptcha").permitAll()
                // 权限表达式的使用和自定义
                .antMatchers("/biz1").access("hasRole('ADMIN')")
                .antMatchers("/biz2").hasRole("USER")
                .anyRequest().access("@rbacService.hasPermission(request,authentication)");

        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .invalidSessionUrl("/login.html") // 非法session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .expiredSessionStrategy(customExpiredSessionStrategy);
    }

    // 配置webSecurity ignore 掉的 是不会走资源拦截器或者过滤器的 也就是FilterSecurityInterceptor等
    // 一般用来配置静态资源
    @Override
    public void configure(WebSecurity web) {
        //将项目中静态资源路径开放出来
        web.ignoring()
                .antMatchers("/css/**", "/fonts/**", "/img/**", "/js/**");
    }

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
}
