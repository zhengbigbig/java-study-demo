package com.zbb.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
@JsonPropertyOrder(value = {"content", "title"})
public class Article {
//    @JsonIgnore
    private Long id;
    private String author;
    private String title;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;

}
