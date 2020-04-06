package com.zbb.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by zhengzhiheng on 2020/4/6 2:45 下午
 * Description:
 */

@Data // getters、setters、hashcode、equals
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Article {
    private Long id;
    private String author;
    private String title;
    private String content;
    private Date createTime;
}
