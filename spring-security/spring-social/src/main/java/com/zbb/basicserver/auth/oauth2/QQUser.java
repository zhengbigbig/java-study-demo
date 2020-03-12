package com.zbb.basicserver.auth.oauth2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true) // 避免映射字段找不到的异常
@Data
public class QQUser {
    private String openId;
    //返回码：0表示获取成功
    private String ret;
    //返回错误信息，如果返回成功，错误信息为空串
    private String msg;
    //用户昵称
    private String nickname;
    //用户的头像30x30
    private String figureurl;
    //性别
    private String gender;
}