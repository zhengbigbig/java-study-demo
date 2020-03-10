package com.zbb.basicserver.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zbb.basicserver.dao.UserMapper;
import com.zbb.basicserver.entity.User;
import com.zbb.basicserver.service.CustomUserDetailsService;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.inmemory.request.InMemorySlidingWindowRequestRateLimiter;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * Created by zhengzhiheng on 2020/3/6 5:47 下午
 * Description:
 */
@Service
@Log
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${spring.security.loginType}")
    private String loginType;

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    CustomUserDetailsService customUserDetailsService;

    @Resource
    UserMapper userMapper;

    // 定义规则： 15分钟内输入3次，就触发限流
    Set<RequestLimitRule> rules =
            Collections.singleton(RequestLimitRule.of(Duration.of(15 * 60, SECONDS), 3));
    RequestRateLimiter limiter = new InMemorySlidingWindowRequestRateLimiter(rules);

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        //从request或request.getSession中获取登录用户名
        String username = request.getParameter("uname");
        //默认提示信息
        String errorMsg = "用户名或者密码输入错误!";

        if (exception instanceof LockedException) {
            errorMsg = "您已经多次登陆失败，账户已被锁定，请稍后再试！";
        } else {
            //计数器加1，并判断该用户是否已经到了触发了锁定规则
            boolean reachLimit = limiter.overLimitWhenIncremented(username);
            if (reachLimit) { //如果触发了锁定规则，通过UserDetails告知Spring Security锁定账户
                User user = (User) customUserDetailsService.loadUserByUsername(username);
                user.setAccountNonLocked(false);
                userMapper.updateEnabledByUsername(user);
            }
        }

        if (exception instanceof SessionAuthenticationException) {
            errorMsg = exception.getMessage();
        }


        if (loginType.equalsIgnoreCase("JSON")) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("msg", errorMsg);

            response.setStatus(200);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(result));
            log.info(errorMsg + " :登录失败！");
        } else {
            // 跳转到登录之前的页面
            super.onAuthenticationFailure(request, response, exception);
        }
    }
}
