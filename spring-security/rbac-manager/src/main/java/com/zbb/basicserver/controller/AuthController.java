package com.zbb.basicserver.controller;

import com.zbb.basicserver.dao.UserMapper;
import com.zbb.basicserver.entity.AuthResult;
import com.zbb.basicserver.entity.User;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zhengzhiheng on 2020/3/7 5:46 下午
 * Description:
 */

@RestController
@Log
public class AuthController {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/currentUser")
    @ResponseBody
    public AuthResult current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            User currentUser = (User) authentication.getPrincipal();
            return AuthResult.success("获取成功", currentUser);
        }
        return AuthResult.failure("当前未登录");
    }

    @PostMapping
    @ResponseBody
    public AuthResult register(User registerUser) {
        User user = userMapper.findUserByUsername(registerUser.getUsername());
        if (user != null) {
            return AuthResult.failure("用户已存在");
        }
        try {
            userMapper.createUser(new User(
                    registerUser.getUsername(),
                    passwordEncoder.encode(registerUser.getPassword())));
        } catch (Exception e) {
            log.info("用户注册失败: " + e.getMessage());
            return AuthResult.failure("注册失败");
        }
        return AuthResult.success("注册成功", null);

    }

//    @PostMapping
//    @ResponseBody
//    public AuthResult login(User loginUser){
//
//    }
}
