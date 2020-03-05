package com.icoding.vo;

import lombok.Data;

@Data
public class ClientInfoVo {
    private String clientUrl; //客户单的注销地址
    private String jsessionid; // 当前的用的session 信息
}
