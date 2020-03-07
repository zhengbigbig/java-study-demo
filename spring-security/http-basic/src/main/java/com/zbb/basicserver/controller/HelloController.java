package com.zbb.basicserver.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * Created by zhengzhiheng on 2020/3/5 3:48 下午
 * Description:
 */
@RestController
public class HelloController {

    @GetMapping("/")
    public String index() {
        return "哦豁，你可以选择访问 /home 或者/admin来测试权限";
    }

    @GetMapping("/home")
    public String home() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        SecurityContextHolder.clearContext();
        return "welcome to home";
    }

    @GetMapping("/admin")
    public String admin() {
        return "welcome to admin";
    }

    @GetMapping("/logout")
    public void logout(HttpSession httpSession){
        httpSession.invalidate();
    }
}
