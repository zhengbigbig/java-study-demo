package com.zbb.basicserver.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TestController {

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String index() {
        return "index";
    }

    @RequestMapping("/user")
    public String getUser(Authentication authentication) {
        System.out.println(authentication);
        return "Hello Oauth2 Resource Server";
    }

}
