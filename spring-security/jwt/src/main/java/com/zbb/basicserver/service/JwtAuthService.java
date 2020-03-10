package com.zbb.basicserver.service;

import com.zbb.basicserver.utils.JwtTokenUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by zhengzhiheng on 2020/3/10 9:21 下午
 * Description:
 */

@Service
public class JwtAuthService {
    @Resource
    private AuthenticationManager authenticationManager;
    @Resource
    private CustomUserDetailsService customUserDetailsService;
    @Resource
    private JwtTokenUtil jwtTokenUtil;

    public String login(String username, String password) {
        try {
            // 登录验证
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authenticate = authenticationManager.authenticate(token);
            // 存入上下文
            SecurityContextHolder.getContext().setAuthentication(authenticate);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("用户名或密码错误");
        }

        // 生成jwt
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        return jwtTokenUtil.generateToken(userDetails);
    }

    public String refreshToken(String oldToken) {
        if (!jwtTokenUtil.isTokenExpired(oldToken)) {
            return jwtTokenUtil.refreshToken(oldToken);
        }
        return null;
    }
}
