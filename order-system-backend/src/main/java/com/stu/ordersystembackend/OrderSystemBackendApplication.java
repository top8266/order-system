package com.stu.ordersystembackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.stu.ordersystembackend.mapper") // 必须和你的mapper包名完全一致！
public class OrderSystemBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderSystemBackendApplication.class, args);
    }
}