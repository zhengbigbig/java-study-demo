package com.zbb.basicserver.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created by zhengzhiheng on 2020/3/10 12:56 下午
 * Description:
 */

@Data
public class SmsCode {
    private String code;

    private LocalDateTime expireTime;

    private String phone; //发送手机号

    public SmsCode(String code, int expireAfterSeconds, String phone) {
        this.code = code;
        this.expireTime = LocalDateTime.now().plusSeconds(expireAfterSeconds);
        this.phone = phone;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireTime);
    }
}
