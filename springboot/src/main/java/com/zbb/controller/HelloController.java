package com.zbb.controller;

import com.zbb.model.Article;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zhengzhiheng on 2020/3/15 2:18 下午
 * Description:
 */
@RestController
public class HelloController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        Article article = new Article(1L, "xxx");
        article.setAuthor("yyy");
        Article.builder().id(2L).author("zzz").build();
        return "Hello World";
    }

}
