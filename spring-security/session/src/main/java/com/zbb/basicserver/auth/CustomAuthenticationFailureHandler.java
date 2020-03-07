package com.zbb.basicserver.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        if (loginType.equalsIgnoreCase("JSON")) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("msg", exception.getMessage());

            response.setStatus(200);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(result));
            log.info( exception.getMessage()+" :登录失败！");
        } else {
            // 跳转到登录之前的页面
            super.onAuthenticationFailure(request, response, exception);
        }
    }
}
