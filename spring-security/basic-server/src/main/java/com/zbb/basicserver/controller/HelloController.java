package com.zbb.basicserver.controller;

import io.swagger.annotations.Authorization;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return "welcome to home";
    }

    @GetMapping("/admin")
    public String admin() {
        return "welcome to admin";
    }
}
