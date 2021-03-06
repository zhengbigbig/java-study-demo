package com.zbb.basicserver.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhengzhiheng on 2020/3/9 3:52 下午
 * Description:
 */

@Service
@Log
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    @Value("${spring.security.loginType}")
    private String loginType;

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        UserDetails principal = (UserDetails) authentication.getPrincipal();

        if (loginType.equalsIgnoreCase("JSON")) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "ok");
            result.put("msg", principal);

            response.setStatus(200);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(result));
            log.info( principal.getUsername()+" :登出成功！");
        } else {
            // 跳转到登录界面
            response.sendRedirect("/login.html");
        }
    }
}
