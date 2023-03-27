package com.lts;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
@MapperScan("com.lts.mapper") //扫描mapper包
@ServletComponentScan //将监听器，过滤器注册到Spring容器
@EnableTransactionManagement //开启事务管理
public class MyReggieApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyReggieApplication.class, args);
    }

}
