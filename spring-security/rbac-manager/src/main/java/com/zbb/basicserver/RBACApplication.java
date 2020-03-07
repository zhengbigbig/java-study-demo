package com.zbb.basicserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

// springboot 会自动加载数据库配置，若没有使用，需要排除
@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
public class RBACApplication {

    public static void main(String[] args) {
        SpringApplication.run(RBACApplication.class, args);
    }

}
