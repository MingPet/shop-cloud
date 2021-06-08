package com.fh.shop.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;


@SpringBootApplication
//注解来启用Euerka注册中心功能resources
@EnableEurekaServer
public class ShopRegCenterApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopRegCenterApplication.class,args);
    }
}
