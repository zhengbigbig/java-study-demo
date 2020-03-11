package com.zbb.basicserver.entity;

import lombok.Data;

/**
 * Created by zhengzhiheng on 2020/3/7 5:02 下午
 * Description:
 */

@Data
public class Permission {
    private Integer id;
    private String name;
    private String description;
    private String url;
    private Long pid;
}
