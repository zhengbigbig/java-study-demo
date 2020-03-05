package com.icoding.controller;

import com.icoding.utils.SSOClientUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
public class LoginController {

    @RequestMapping("/tmall")
    public String index(Model model){
        model.addAttribute("servetLogouUrl",SSOClientUtil.getServerLogOutUrl());

        return "tmall";
    }


    //logOut
    @RequestMapping("/logOut")
    public void logOut(HttpSession session){
        session.invalidate();
    }


}
