package com.zbb.basicserver.config;

import com.zbb.basicserver.auth.CustomAuthenticationFailureHandler;
import com.zbb.basicserver.auth.CustomAuthenticationSuccessHandler;
import com.zbb.basicserver.auth.CustomExpiredSessionStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Resource // Resource和Autowired的区别是前者按照名称（byName）来装配，后者按照类型（byType）装配
            CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    @Resource
    CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Resource
    CustomExpiredSessionStrategy customExpiredSessionStrategy;

    // http security 配置
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 禁用csrf ，否则会把所有请求当做非法请求拦截，后面再处理
        http.csrf().disable()
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
                .antMatchers("/login.html", "/login").permitAll()
                // 需要对外暴露的资源路径
                .antMatchers("/biz1", "/biz2")
                // /biz1、/biz2只需要有admin或者user都可访问
                .hasAnyAuthority("ROLE_user", "ROLE_admin")
                .antMatchers("/syslog").hasAnyAuthority("sys:log")
                .antMatchers("/sysuser").hasAnyAuthority("sys:user")
                // admin角色可以访问
                .anyRequest().authenticated();
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
        auth.inMemoryAuthentication()
                .withUser("admin")
                .password(passwordEncoder().encode("123456"))
//                .roles("admin")
                .authorities("sys:log", "sys:user")
                .and()
                .withUser("user")
                .password(passwordEncoder().encode("123456"))
                .roles("user");
    }

    // 设置加密
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
