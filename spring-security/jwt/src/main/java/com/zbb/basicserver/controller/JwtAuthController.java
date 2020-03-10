package com.zbb.basicserver.controller;

import com.zbb.basicserver.entity.AuthResult;
import com.zbb.basicserver.service.JwtAuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Created by zhengzhiheng on 2020/3/10 9:04 下午
 * Description:
 */

@RestController
public class JwtAuthController {

    @Resource
    private JwtAuthService jwtAuthService;

    @PostMapping("/authentication")
    public AuthResult authentication(@RequestBody Map<String, String> map){
        String username = map.get("username");
        String password = map.get("password");
        if(StringUtils.isBlank(username) || StringUtils.isBlank(password)){
            return AuthResult.failure("用户名或密码不能为空");
        }

        try{
            return AuthResult.success("登录成功",jwtAuthService.login(username,password));
        }catch (A)
    }

}
