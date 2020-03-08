package com.zbb.basicserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// springboot 会自动加载数据库配置，若没有使用，需要排除
@SpringBootApplication

public class RememberMeApplication {

    public static void main(String[] args) {
        SpringApplication.run(RememberMeApplication.class, args);
    }

}
