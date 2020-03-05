package com.zbb.basicserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Controller
public class BizpageController {


    // 登录
    /*@PostMapping("/login")
    public String index(String username,String password) {
        return "index";
    }*/

    // 登录成功之后的首页
    @GetMapping("/index")
    public String index() {
        System.out.println(111);
        return "index";
    }

    // 日志管理
    @GetMapping("/syslog")
    public String showOrder() {
        return "syslog";
    }

    // 用户管理
    @GetMapping("/sysuser")
    public String addOrder() {
        return "sysuser";
    }

    // 具体业务一
    @GetMapping("/biz1")
    public String updateOrder() {
        //methodELService.findAll();
        //methodELService.findOne();

        /*List<Integer> ids = new ArrayList<>();
        ids.add(1);
        ids.add(2);
        methodELService.delete(ids,null);*/

        //List<PersonDemo> pds = methodELService.findAllPD();

        return "biz1";
    }

    // 具体业务二
    @GetMapping("/biz2")
    public String deleteOrder() {
        return "biz2";
    }

    @GetMapping("/signout")
    public String logout(){
        return "aftersignout.html";
    }

}