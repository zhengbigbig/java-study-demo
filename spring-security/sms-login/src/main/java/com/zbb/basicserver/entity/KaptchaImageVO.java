package com.zbb.basicserver.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created by zhengzhiheng on 2020/3/9 7:21 下午
 * Description:
 */

@Data
public class KaptchaImageVO {
    private String code;

    private LocalDateTime expireTime;

    public KaptchaImageVO(String code, int expireAfterSeconds) {
        this.code = code;
        this.expireTime = LocalDateTime.now().plusSeconds(expireAfterSeconds);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireTime);
    }
}
