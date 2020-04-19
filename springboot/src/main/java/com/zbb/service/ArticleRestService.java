package com.zbb.service;

import com.zbb.model.Article;
import org.springframework.stereotype.Service;

/**
 * Created by zhengzhiheng on 2020/4/18 10:48 上午
 * Description:
 */
@Service
public class ArticleRestService {


    public String  save(Article article) {
        // todo 保存到数据库
        return "ok";
    }
}
