package com.zbb.basicserver.controller;

import com.zbb.basicserver.dao.UserMapper;
import com.zbb.basicserver.entity.AuthResult;
import com.zbb.basicserver.entity.SmsCode;
import com.zbb.basicserver.entity.User;
import lombok.extern.java.Log;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Log
@RestController
public class SmsController {

    @Resource
    UserMapper userMapper;


    //获取短信验证码
    @GetMapping("/sms")
    public AuthResult sms(@RequestParam String phone, HttpSession session) throws IOException {

        User user = userMapper.findUserByUsername(phone);
        if (user == null) {
            return AuthResult.failure("您输入的手机号不是系统注册用户");
        }

        SmsCode smsCode = new SmsCode(
                RandomStringUtils.randomNumeric(6), 60, phone);
        //TODO 此处调用验证码发送服务接口
        log.info(smsCode.getCode() + "=》" + phone);

        session.setAttribute("sms_key", smsCode);

        return AuthResult.success("短信息已经发送到您的手机: ",phone);
    }
}